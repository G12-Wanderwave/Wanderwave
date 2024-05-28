package ch.epfl.cs311.wanderwave.model.spotify

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.mutableFloatStateOf
import ch.epfl.cs311.wanderwave.BuildConfig
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.RecentlyPlayedRepository
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.ContentApi
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.NotLoggedInException
import com.spotify.protocol.types.ImageUri
import com.spotify.protocol.types.ListItem
import com.spotify.protocol.types.PlayerState
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import java.net.URL
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
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
import kotlinx.coroutines.withContext
import org.json.JSONObject

class SpotifyController
@Inject
constructor(
    private val context: Context,
    private val authenticationController: AuthenticationController,
    private val ioDispatcher: CoroutineDispatcher,
    private val recentlyPlayedRepository: RecentlyPlayedRepository
) {

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
  private var trackListShuffled: List<Track>? = null
  private var onTrackEndCallback: (() -> Unit)? = null
  val shuffling = MutableStateFlow(false)
  val looping = MutableStateFlow(RepeatMode.OFF)

  val trackProgress: MutableFloatState = mutableFloatStateOf(0f)

  fun getAuthorizationRequest(): AuthorizationRequest {
    val builder =
        AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.CODE, REDIRECT_URI)
            .setScopes(SCOPES.toTypedArray())
    return builder.build()
  }

  suspend fun getAlbumImage(albumId: String): Bitmap? {
    return try {
      val url = "https://api.spotify.com/v1/albums/$albumId"
      val jsonResponse = spotifyGetFromURL(url)
      val imageUrl = extractImageUrlFromJson(jsonResponse)
      imageUrl?.let { fetchImageFromUrl(context, it) }
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  // Helper method to extract image URL from JSON response
  fun extractImageUrlFromJson(jsonResponse: String): String? {
    val jsonObject = JSONObject(jsonResponse)
    val images = jsonObject.getJSONArray("images")
    if (images.length() > 0) {
      return images.getJSONObject(0).getString("url")
    }
    return null
  }

  // Helper method to fetch image from URL using Glide
  suspend fun fetchImageFromUrl(context: Context, url: String): Bitmap? {
    return withContext(Dispatchers.IO) {
      try {
        val x =
            Glide.with(context)
                .asBitmap()
                .load(url)
                .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .get()

        x
      } catch (e: Exception) {
        e.printStackTrace()
        null
      }
    }
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

  fun addRecentlyPlayedTrack(track: com.spotify.protocol.types.Track) {
    val wanderwaveTrack = track.toWanderwaveTrack()
    recentlyPlayedRepository.addRecentlyPlayed(wanderwaveTrack, Instant.now())
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
          .setResultCallback {
            appRemote.value?.let {
              it.playerApi.subscribeToPlayerState().setEventCallback {
                startPlaybackTimer(it.track.duration)
              }
            }
            onSuccess()
          }
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
    playTrack(
        track = trackToPlay,
        onSuccess = {
          this.trackList = trackList
          this.trackListShuffled = trackList.shuffled()
          onSuccess()
        },
        onFailure = onFailure)
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
    val playerState = playerState()
    val currentTrack = playerState.firstOrNull()?.track
    val currentIndex = trackList?.indexOfFirst { track -> track.id == currentTrack?.uri } ?: -1
    if (currentIndex != -1) {
      var nextIndex = (currentIndex + direction)
      if (looping.value == RepeatMode.ONE) {
        nextIndex = currentIndex
      } else if (looping.value == RepeatMode.ALL) {
        nextIndex %= trackList!!.size
      } else if (nextIndex < 0 || nextIndex >= trackList!!.size) {
        onFailure(Throwable("Cannot skip out of bounds"))
        return
      }
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
                  Log.d("SpotifyController", "Player state: $it")
                  startPlaybackTimer(it.track.duration)
                }
                ?.setErrorCallback { Log.e("SpotifyController", "Error in player state flow") }
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
          val checkInterval = 50L // Check every second
          var elapsedTime = 0L
          while (elapsedTime < trackDuration) {
            delay(checkInterval)
            elapsedTime += checkInterval
            trackProgress.value = elapsedTime.toFloat() / trackDuration
            appRemote.value?.playerApi?.playerState?.setResultCallback { playerState ->
              if ((trackDuration - playerState.playbackPosition) <= 1000) {
                onTrackEndCallback?.invoke()
              }
            }
          }
        }
  }

  fun stopPlaybackTimer() {
    trackProgress.value = 0f
    playbackTimer?.cancel()
    playbackTimer = null
  }

  fun onPlayerStateUpdate() { // TODO: Coverage
    appRemote.value?.let {
      it.playerApi.subscribeToPlayerState().setEventCallback { playerState: PlayerState ->
        if (playerState.track != null) {
          startPlaybackTimer(playerState.track.duration - playerState.playbackPosition)
        }
        addRecentlyPlayedTrack(playerState.track)
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

  // Look at the reference section of the documentation to know how to format the URL
  // https://developer.spotify.com/documentation/web-api
  suspend fun spotifyGetFromURL(url: String): String {
    var answer: String
    withContext(Dispatchers.IO) { answer = authenticationController.makeApiRequest(URL(url)) }
    return answer
  }

  enum class ConnectResult {
    SUCCESS,
    NOT_LOGGED_IN,
    FAILED
  }

  enum class RepeatMode {
    OFF,
    ALL,
    ONE
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
  val track = spotifyController.getAllElementFromSpotify()
  checkIfNullAndAddToAList(track, spotifySubsectionList, scope)
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
  val children = spotifyController.getAllChildren(item)
  checkIfNullAndAddToAList(children, childrenPlaylistTrackList, scope)
}

fun checkIfNullAndAddToAList(
    items: Flow<List<ListItem>>,
    list: MutableStateFlow<List<ListItem>>,
    scope: CoroutineScope
) {
  scope.launch {
    val value = items.firstOrNull()
    if (value != null) {
      for (child in value) {
        list.value += child
      }
    }
  }
}

fun com.spotify.protocol.types.Track.toWanderwaveTrack(): Track {
  return Track(this.uri, this.name, this.artist.name)
}

/**
 * Get all the liked tracks of the user and add them to the likedSongs list.
 *
 * @param likedSongsTrackList the list of liked songs
 * @param spotifyController the SpotifyController
 * @param scope the CoroutineScope
 * @author Menzo Bouaissi
 * @since 3.0
 * @last update 3.0
 */
suspend fun getLikedTracksFromSpotify(
    likedSongsTrackList: MutableStateFlow<List<ListItem>>,
    spotifyController: SpotifyController,
    scope: CoroutineScope
) {
  scope.launch {
    val url = "https://api.spotify.com/v1/me/tracks"
    try {
      val jsonResponse =
          spotifyController.spotifyGetFromURL("$url?limit=50") // TODO : revoir la limite
      parseTracks(jsonResponse, likedSongsTrackList)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
}

fun getTracksFromSpotifyPlaylist(
    playlistId: String,
    playlist: MutableStateFlow<List<ListItem>>,
    spotifyController: SpotifyController,
    scope: CoroutineScope
) {
  scope.launch {
    val url = "https://api.spotify.com/v1/playlists/$playlistId/tracks"
    try {
      val json = spotifyController.spotifyGetFromURL(url)
      parseTracks(json, playlist)
    } catch (e: Exception) {
      Log.e("SpotifyController", "Failed to get songs from playlist")
      e.printStackTrace()
    }
  }
}

/**
 * Parse the JSON response from the Spotify API to get the liked songs of the user.
 *
 * @param jsonResponse the JSON response from the Spotify API
 * @param songsTrackList the list of liked songs
 * @author Menzo Bouaissi
 * @since 3.0
 * @last update 3.0
 */
fun parseTracks(
    jsonResponse: String,
    songsTrackList: MutableStateFlow<List<ListItem>>,
) {
  val jsonObject = JSONObject(jsonResponse)
  val items = jsonObject.getJSONArray("items")
  songsTrackList.value = emptyList()
  for (i in 0 until items.length()) {

    val track = items.getJSONObject(i).getJSONObject("track")
    val id = track.getString("id")
    val name = track.getString("name")
    val artistsArray = track.getJSONArray("artists")
    val artist = artistsArray.getJSONObject(0).getString("name") // Gets the primary artist
    songsTrackList.value += ListItem(id, "", ImageUri(""), name, artist, false, false)
  }
}
