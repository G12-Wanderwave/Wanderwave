package ch.epfl.cs311.wanderwave.model.spotify

import android.content.Context
import android.util.Log
import ch.epfl.cs311.wanderwave.BuildConfig
import ch.epfl.cs311.wanderwave.model.data.Track
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.ContentApi
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.NotLoggedInException
import com.spotify.protocol.types.ListItem
import com.spotify.protocol.types.PlayerState
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class SpotifyController(private val context: Context) {

  private val CLIENT_ID = BuildConfig.SPOTIFY_CLIENT_ID
  private val REDIRECT_URI = "wanderwave-auth://callback"
  private val SCOPES =
      listOf(
          "app-remote-control",
          "playlist-read-private",
          "playlist-read-collaborative",
          "user-library-read",
          "user-read-email",
          "user-read-private")

  var playbackTimer: Job? = null

  private val connectionParams =
      ConnectionParams.Builder(CLIENT_ID).setRedirectUri(REDIRECT_URI).showAuthView(true).build()

  val appRemote: MutableStateFlow<SpotifyAppRemote?> = MutableStateFlow(null)
  private var trackList: List<Track>? = null
  private var onTrackEndCallback: (() -> Unit)? = null

  fun getAuthorizationRequest(): AuthorizationRequest {
    val builder =
        AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.CODE, REDIRECT_URI)
            .setScopes(SCOPES.toTypedArray())
    return builder.build()
  }

  fun getLogoutRequest(): AuthorizationRequest {
    val builder =
        AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.CODE, REDIRECT_URI)
            .setScopes(emptyArray())
            .setShowDialog(true)
    return builder.build()
  }

  fun isConnected(): Boolean {
    return appRemote.value?.isConnected ?: false
  }

  fun connectRemote(): Flow<ConnectResult> {
    return callbackFlow {
      if (isConnected()) {
        trySend(ConnectResult.SUCCESS)
      } else {
        disconnectRemote()
        SpotifyAppRemote.connect(
            context,
            connectionParams,
            object : Connector.ConnectionListener {
              override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                appRemote.value = spotifyAppRemote
                println("Connected to Spotify App Remote")
                onPlayerStateUpdate()
                trySend(ConnectResult.SUCCESS)
                channel.close()
              }

              override fun onFailure(throwable: Throwable) {
                when (throwable) {
                  is NotLoggedInException -> trySend(ConnectResult.NOT_LOGGED_IN)
                  else -> trySend(ConnectResult.FAILED)
                }
                channel.close()
              }
            })
      }
      awaitClose {}
    }
  }

  fun disconnectRemote() {
    appRemote.value.let { SpotifyAppRemote.disconnect(it) }
    appRemote.value = null
  }

  fun playTrack(track: Track, onSuccess: () -> Unit = {}, onFailure: (Throwable) -> Unit = {}) {
    appRemote.value?.let {
      it.playerApi
          .play(track.id)
          .setResultCallback { onSuccess() }
          .setErrorCallback { error -> onFailure(error) }
    }
  }

  fun playTrackList(
      trackList: List<Track>,
      track: Track? = null,
      onSuccess: () -> Unit = {},
      onFailure: (Throwable) -> Unit = {}
  ) {
    if (trackList.isEmpty()) {
      onFailure(Throwable("Empty track list"))
      return
    }
    val trackToPlay = track ?: trackList[0]
    appRemote.value?.let {
      it.playerApi
          .play(trackToPlay.id)
          .setResultCallback {
            this.trackList = trackList
            onSuccess()
          }
          .setErrorCallback { error -> onFailure(error) }
    }
  }

  fun pauseTrack(onSuccess: () -> Unit = {}, onFailure: (Throwable) -> Unit = {}) {
    appRemote.value?.let {
      it.playerApi
          .pause()
          .setResultCallback { onSuccess() }
          .setErrorCallback { error -> onFailure(error) }
    }
  }

  fun resumeTrack(onSuccess: () -> Unit = {}, onFailure: (Throwable) -> Unit = {}) {
    appRemote.value?.let {
      it.playerApi
          .resume()
          .setResultCallback { onSuccess() }
          .setErrorCallback { error -> onFailure(error) }
    }
  }

  suspend fun skip(
      direction: Int,
      onSuccess: () -> Unit = {},
      onFailure: (Throwable) -> Unit = {}
  ) {
    val currentTrack = playerState().firstOrNull()?.track
    val currentIndex = trackList?.indexOfFirst { track -> track.id == currentTrack?.uri } ?: -1
    Log.d("SpotifyController", "Skipping $direction")
    Log.d("SpotifyController", "Track list: $trackList")
    Log.d("SpotifyController", "Current index: $currentIndex")
    if (currentIndex != -1) {
      val nextIndex = (currentIndex + direction) % trackList!!.size
      val nextTrack = trackList!![nextIndex]
      playTrack(nextTrack, onSuccess, onFailure)
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  fun playerState(): Flow<PlayerState?> {
    return appRemote.flatMapLatest { appRemote ->
      callbackFlow {
        val callbackResult =
            appRemote
                ?.playerApi
                ?.subscribeToPlayerState()
                ?.setEventCallback {
                  trySend(it)
                  startPlaybackTimer(it.track.duration)
                }
                ?.setErrorCallback { Log.d("SpotifyController", "Error in player state flow") }
        awaitClose {
          callbackResult?.cancel()
          stopPlaybackTimer()
        }
      }
    }
  }

  fun startPlaybackTimer(
      trackDuration: Long,
      scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
  ) {
    stopPlaybackTimer() // Ensure no previous timers are running
    playbackTimer =
        scope.launch {
          val checkInterval = 1000L // Check every second
          var elapsedTime = 0L
          while (elapsedTime < trackDuration) {
            delay(checkInterval)
            appRemote.value?.playerApi?.playerState?.setResultCallback { playerState ->
              if ((trackDuration - playerState.playbackPosition) <= 1000) {
                onTrackEndCallback?.invoke()
              }
            }
          }
        }
  }

  fun stopPlaybackTimer() {
    playbackTimer?.cancel()
    playbackTimer = null
  }

  fun onPlayerStateUpdate() { // TODO: Coverage
    appRemote.value?.let {
      it.playerApi.subscribeToPlayerState().setEventCallback { playerState: PlayerState ->
        if (playerState.track != null) {
          startPlaybackTimer(playerState.track.duration - playerState.playbackPosition)
        }
      }
    }
  }

  fun setOnTrackEndCallback(callback: () -> Unit) {
    onTrackEndCallback = callback
  }

  fun getOnTrackEndCallback(): (() -> Unit)? {
    return onTrackEndCallback
  }
  /**
   * Get all the playlist, title, ... from spotify from the home page of the user.
   *
   * @return a Flow of ListItem which has all the playlist, title, ... from the home page of the
   *   user.
   * @author Menzo Bouaissi
   * @since 2.0
   * @last update 2.0
   */
  fun getAllElementFromSpotify(): Flow<List<ListItem>> {
    val list: MutableList<ListItem> = emptyList<ListItem>().toMutableList()
    return callbackFlow {
      val callResult =
          appRemote.value?.let {
            it.contentApi
                .getRecommendedContentItems(ContentApi.ContentType.DEFAULT)
                .setResultCallback {
                  for (i in it.items) list += i
                  trySend(list)
                }
                .setErrorCallback { trySend(list + ListItem("", "", null, "", "", false, false)) }
          }
      awaitClose { callResult?.cancel() }
    }
  }
  /**
   * Get the children of a ListItem. In our case, the children is either a playlist or an album
   *
   * @param listItem the ListItem to get the children from
   * @return a Flow of ListItem
   * @author Menzo Bouaissi
   * @since 2.0
   * @last update 2.0
   */
  fun getChildren(listItem: ListItem): Flow<ListItem> {
    return callbackFlow {
      val callResult =
          appRemote.value?.let {
            it.contentApi
                .getChildrenOfItem(listItem, 50, 0)
                .setResultCallback {
                  for (i in it.items) if (i.id.contains("album") || i.id.contains("playlist"))
                      trySend(i)
                }
                .setErrorCallback { trySend(ListItem("", "", null, "", "", false, false)) }
          }
      awaitClose { callResult?.cancel() }
    }
  }

  /**
   * Get the all the children of a ListItem. In our case, the children is either a playlist or an
   * album
   *
   * @param listItem the ListItem to get the childrens from
   * @return a Flow of List<ListItem> which contains all the children of the ListItem
   * @author Menzo Bouaissi
   * @since 2.0
   * @last update 2.0
   */
  fun getAllChildren(listItem: ListItem): Flow<List<ListItem>> {
    val list: MutableList<ListItem> = emptyList<ListItem>().toMutableList()

    return callbackFlow {
      val callResult =
          appRemote.value?.let {
            it.contentApi
                .getChildrenOfItem(listItem, 50, 0)
                .setResultCallback {
                  for (i in it.items) list += i
                  trySend(list)
                }
                .setErrorCallback { trySend(list + ListItem("", "", null, "", "", false, false)) }
          }
      awaitClose { callResult?.cancel() }
    }
  }

  enum class ConnectResult {
    SUCCESS,
    NOT_LOGGED_IN,
    FAILED
  }
}
/**
 * Get all the element of the main screen and add them to the top list
 *
 * @author Menzo Bouaissi
 * @since 2.0
 * @last update 3.0
 */
fun retrieveAndAddSubsectionFromSpotify(
    spotifySubsectionList: MutableStateFlow<List<ListItem>>,
    spotifyController: SpotifyController,
    scope: CoroutineScope
) {
  scope.launch {
    val track = spotifyController.getAllElementFromSpotify().firstOrNull()
    if (track != null) {
      for (i in track) {
        spotifySubsectionList.value += i
      }
    }
  }
}

/**
 * Get all the element of the main screen and add them to the top list
 *
 * @author Menzo Bouaissi
 * @since 2.0
 * @last update 3.0
 */
fun retrieveChildFromSpotify(
    item: ListItem,
    childrenPlaylistTrackList: MutableStateFlow<List<ListItem>>,
    spotifyController: SpotifyController,
    scope: CoroutineScope
) {
  scope.launch {
    val children = spotifyController.getAllChildren(item).firstOrNull()
    if (children != null) {
      for (child in children) {
        childrenPlaylistTrackList.value += child
      }
    }
  }
}

fun com.spotify.protocol.types.Track.toWanderwaveTrack(): Track {
  return Track(this.uri, this.name, this.artist.name)
}
