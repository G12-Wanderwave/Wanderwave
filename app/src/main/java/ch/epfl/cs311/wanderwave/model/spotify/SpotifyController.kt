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
  private val PLAYLIST_NAME = "Wanderwave"
  private val PLAYLIST_DESCRIPTION = "Liked songs from Wanderwave"
  private val CLIENT_ID = BuildConfig.SPOTIFY_CLIENT_ID
  private val REDIRECT_URI = "wanderwave-auth://callback"
  private val SCOPES =
      listOf(
          "app-remote-control",
          "playlist-read-private",
          "playlist-read-collaborative",
          "user-library-read",
          "user-read-email",
          "user-read-private",
          "playlist-modify-public",
          "playlist-modify-private",
          "ugc-image-upload")

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

  suspend fun getAllPlaylists(): List<ListItem> {
    val url = "https://api.spotify.com/v1/me/playlists"
    val playlists = spotifyGetFromURL(url)
    val jsonObject = JSONObject(playlists)
    val items = jsonObject.getJSONArray("items")
    // Convert items to list of ListItem
    val list: MutableList<ListItem> = emptyList<ListItem>().toMutableList()
    for (i in 0 until items.length()) {
      val item = items.getJSONObject(i)
      val id = item.getString("id")
      val name = item.getString("name")
      list += ListItem(id, "", null, name, "", false, false)
    }
    Log.d("SpotifyController", "Got all playlists: $list")
    return list
  }

  suspend fun createPlaylistIfNotExist(): String {
    val list = getAllPlaylists()
    if (list.any { it.title == PLAYLIST_NAME }) {
      return list.first { it.title == PLAYLIST_NAME }.id
    }
    val url = "https://api.spotify.com/v1/users/${getCurrentUserId()}/playlists"
    val data =
        """
        {
            "name": "$PLAYLIST_NAME",
            "description": "$PLAYLIST_DESCRIPTION",
            "public": false
        }
    """
            .trimIndent()
    var playlist: String
    withContext(Dispatchers.IO) {
      playlist = authenticationController.makeApiRequest(URL(url), "POST", data)
    }

    if (playlist == "FAILURE") {
      throw Exception("Failed to create playlist")
    }

    val jsonObject = JSONObject(playlist)
    val playlistId = jsonObject.getString("id")
    authenticationController.uploadPlaylistImage(context, playlistId)
    return playlistId
  }

  suspend fun getCurrentUserId(): String {
    val url = "https://api.spotify.com/v1/me"
    val response = authenticationController.makeApiRequest(URL(url))
    if (response == "FAILURE") {
      throw Exception("Failed to get current user id")
    }
    val jsonObject = JSONObject(response)
    return jsonObject.getString("id")
  }

  suspend fun addToPlaylist(track: Track) {
    val playlistId = createPlaylistIfNotExist()
    Log.d("SpotifyController", "Adding track to playlist: $playlistId")
    val url = "https://api.spotify.com/v1/playlists/$playlistId/tracks"
    val data =
        """
        {
            "uris": ["${track.id}"]
        }
    """
            .trimIndent()
    val response =
        withContext(Dispatchers.IO) {
          authenticationController.makeApiRequest(URL(url), "POST", data)
        }

    if (response == "FAILURE") {
      throw Exception("Failed to add track to playlist")
    }
  }

  suspend fun removeFromPlaylist(track: Track) {
    val playlistId = createPlaylistIfNotExist()
    Log.d("SpotifyController", "Removing track from playlist: $playlistId")
    val url = "https://api.spotify.com/v1/playlists/$playlistId/tracks"
    val data =
        "{\n" +
            "    \"tracks\": [\n" +
            "        {\n" +
            "            \"uri\": \"${track.id}\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"snapshot_id\": \"$playlistId\"\n" +
            "}"
    withContext(Dispatchers.IO) {
      authenticationController.makeApiRequest(URL(url), "DELETE", data)
    }
  }

  suspend fun getTrackImage(trackId: String): Bitmap? {
    return try {
      val albumId = getAlbumIdFromTrackId(this, trackId)
      getAlbumImage(albumId)
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  suspend fun getAlbumIdFromTrackId(spotifyController: SpotifyController, trackId: String): String {
    val trackId = trackId.split(":")[2]
    val jsonResponse =
        spotifyController.spotifyGetFromURL("https://api.spotify.com/v1/tracks/$trackId")
    val jsonObject = JSONObject(jsonResponse)
    val album = jsonObject.getJSONObject("album")
    return album.getString("id")
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

  /**
   * Connect to the Spotify app remote
   *
   * @return a Flow of ConnectResult that can be either SUCCESS, NOT_LOGGED_IN, or FAILED
   */
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

  /**
   * Disconnect from the spotify app remote
   */
  fun disconnectRemote() {
    appRemote.value.let { SpotifyAppRemote.disconnect(it) }
    appRemote.value = null
  }

  /**
   * Play a track on the Spotify player. Leaves the playing tracks list untouched.
   *
   * @param track the track to play
   * @param onSuccess the function to call on success
   * @param onFailure the function to call on failure
   */
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

  /**
   * Play a list of tracks. If a track is provided, it will start at that track.
   * If no track is provided, it will start at the first track in the list.
   *
   * @throws Throwable("Empty track list") if the track list is empty
   *
   * @param trackList the list of tracks to play
   * @param track the track to start at, or null to start at the beginning of the list
   * @param onSuccess the function to call on success
   * @param onFailure the function to call on failure
   */
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

  /**
   * Pause the currently playing track. Does nothing if already paused.
   *
   * @param onSuccess the function to call on success
   * @param onFailure the function to call on failure
   */
  fun pauseTrack(onSuccess: () -> Unit = {}, onFailure: (Throwable) -> Unit = {}) {
    appRemote.value?.let {
      it.playerApi
          .pause()
          .setResultCallback { onSuccess() }
          .setErrorCallback { error -> onFailure(error) }
    }
  }

  /**
   * Resume the track that is currently playing (paused). Does nothing if not paused.
   *
   * @param onSuccess the function to call on success
   * @param onFailure the function to call on failure
  */
  fun resumeTrack(onSuccess: () -> Unit = {}, onFailure: (Throwable) -> Unit = {}) {
    appRemote.value?.let {
      it.playerApi
          .resume()
          .setResultCallback { onSuccess() }
          .setErrorCallback { error -> onFailure(error) }
    }
  }

  /**
   * Skip to the next or previous track in the track list.
   * If the current track is not in the track list, this function does nothing.
   *
   * @param direction the direction to skip in. 1 for next, -1 for previous
   * @param onSuccess the function to call on success
   * @param onFailure the function to call on failure
   */
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

  /**
   * Get the current player state of the Spotify player.
   * The player state contains information about the current track, playback position, and playback
   * status.
   * @return a Flow of PlayerState
   */
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

  /**
   * Start a timer that updates the track progress every second until the track ends
   *
   * @param trackDuration the duration of the track in milliseconds
   * @param scope the CoroutineScope to run the timer in
   */
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

  internal fun onPlayerStateUpdate() {
    appRemote.value?.let {
      it.playerApi.subscribeToPlayerState().setEventCallback { playerState: PlayerState ->
        if (playerState.track != null) {
          startPlaybackTimer(playerState.track.duration - playerState.playbackPosition)
        }
        addRecentlyPlayedTrack(playerState.track)
      }
    }
  }

  /**
   * Set the function that is called when a track ends
   *
   * @param callback the function that is called when a track ends
   */
  fun setOnTrackEndCallback(callback: () -> Unit) {
    onTrackEndCallback = callback
  }

  /**
   * Get the function that is called when a track ends
   */
  fun getOnTrackEndCallback(): (() -> Unit)? {
    return onTrackEndCallback
  }
  /**
   * Get all the playlist, title, ... from spotify from the home page of the user.
   *
   * @return a Flow of ListItem which has all the playlist, title, ... from the home page of the
   *   user.
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
    withContext(Dispatchers.IO) {
      val urlString =
          if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "http://$url"
          } else {
            url
          }
      answer = authenticationController.makeApiRequest(URL(urlString))
    }
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

var totalLikes = -1
/**
 * Convert a Spotify Track to a Wanderwave Track
 *
 * @return the Wanderwave Track
 */
fun com.spotify.protocol.types.Track.toWanderwaveTrack(): Track {
  return Track(this.uri, this.name, this.artist.name)
}

suspend fun getTotalLikedTracksFromSpotity(spotifyController: SpotifyController): Int {
  val url = "https://api.spotify.com/v1/me/tracks"
  return try {
    val jsonResponse = spotifyController.spotifyGetFromURL("$url?limit=1")
    val jsonObject = JSONObject(jsonResponse)
    Log.d("SpotifyController", "Total liked tracks: ${jsonObject.getInt("total")}")
    jsonObject.getInt("total")
  } catch (e: Exception) {
    e.printStackTrace()
    0
  }
}
/**
 * Get all the liked tracks of the user and add them to the likedSongs list.
 *
 * @param likedSongsTrackList the list of liked songs
 * @param spotifyController the SpotifyController
 * @param scope the CoroutineScope
 */
fun getLikedTracksFromSpotify(
    likedSongsTrackList: MutableStateFlow<List<ListItem>>,
    spotifyController: SpotifyController,
    scope: CoroutineScope,
    page: Int = 0
) {
  scope.launch {
    val url = "https://api.spotify.com/v1/me/tracks"
    try {
      if (totalLikes == -1) totalLikes = getTotalLikedTracksFromSpotity(spotifyController)
      val limit = (totalLikes - 50 * page).coerceAtMost(50)
      val jsonResponse = spotifyController.spotifyGetFromURL("$url?limit=$limit&offset=${page* 50}")
      parseTracks(jsonResponse, likedSongsTrackList)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
}

/**
 * Get all the tracks from a Spotify playlist and add them to the playlist list.
 *
 * @param playlistId the id of the playlist
 * @param playlist the list of tracks
 * @param spotifyController the SpotifyController
 * @param scope the CoroutineScope
 */
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
