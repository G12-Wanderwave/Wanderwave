package ch.epfl.cs311.wanderwave.model.spotify

import android.content.Context
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
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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

  var appRemote: SpotifyAppRemote? = null
  private var onTrackEndCallback: (() -> Unit)? = null

  fun getAuthorizationRequest(): AuthorizationRequest {
    val builder =
        AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI)
            .setScopes(SCOPES.toTypedArray())
    return builder.build()
  }

  fun getLogoutRequest(): AuthorizationRequest {
    val builder =
        AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI)
            .setScopes(emptyArray())
            .setShowDialog(true)
    return builder.build()
  }

  fun isConnected(): Boolean {
    return appRemote?.isConnected ?: false
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
                appRemote = spotifyAppRemote
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
    appRemote?.let { SpotifyAppRemote.disconnect(it) }
  }

  fun playTrack(track: Track): Flow<Boolean> {
    return callbackFlow {
      appRemote?.let { remote ->
        remote.playerApi
            .play(track.id)
            .setResultCallback {
              remote.playerApi.subscribeToPlayerState().setEventCallback {
                startPlaybackTimer(it.track.duration - it.playbackPosition)
              }
              trySend(true)
            }
            .setErrorCallback { trySend(false) }
      }
      awaitClose { stopPlaybackTimer() }
    }
  }

  fun startPlaybackTimer(trackDuration: Long) {
    stopPlaybackTimer() // Ensure no previous timers are running
    playbackTimer =
        CoroutineScope(Dispatchers.IO).launch {
          val checkInterval = 1000L // Check every second
          var elapsedTime = 0L
          while (elapsedTime < trackDuration) {
            delay(checkInterval)
            elapsedTime += checkInterval
            appRemote?.playerApi?.playerState?.setResultCallback { playerState ->
              if (playerState.playbackPosition >= trackDuration - 1000) {
                onTrackEndCallback?.invoke()
                stopPlaybackTimer()
              }
            }
          }
        }
  }

  fun stopPlaybackTimer() {
    playbackTimer?.cancel()
    playbackTimer = null
  }
  /**
   * Skip to the next track in the queue.
   *
   * @return a Flow of Boolean which is true if the operation was successful, false otherwise.
   */
  fun pauseTrack(): Flow<Boolean> {
    return callbackFlow {
      val callResult =
          appRemote?.let {
            it.playerApi
                .pause()
                .setResultCallback { trySend(true) }
                .setErrorCallback { trySend(false) }
          }
      awaitClose { callResult?.cancel() }
    }
  }

  fun resumeTrack(): Flow<Boolean> {
    return callbackFlow {
      val callResult =
          appRemote?.let {
            it.playerApi
                .resume()
                .setResultCallback { trySend(true) }
                .setErrorCallback { trySend(false) }
          }
      awaitClose { callResult?.cancel() }
    }
  }

  // Detect when currently playing song has ended (or is going to end) and run onTrackEndCallback
  fun onPlayerStateUpdate() {
    appRemote?.let {
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
          appRemote?.let { it ->
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
  @OptIn(FlowPreview::class)
  fun getChildren(listItem: ListItem): Flow<ListItem> {

    return callbackFlow {
      val callResult =
          appRemote?.let { it ->
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
  @OptIn(FlowPreview::class)
  fun getAllChildren(listItem: ListItem): Flow<List<ListItem>> {
    val list: MutableList<ListItem> = emptyList<ListItem>().toMutableList()

    return callbackFlow {
      val callResult =
          appRemote?.let { it ->
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
