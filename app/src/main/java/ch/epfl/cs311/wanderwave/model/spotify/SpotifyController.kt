package ch.epfl.cs311.wanderwave.model.spotify

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.mutableFloatStateOf
import ch.epfl.cs311.wanderwave.BuildConfig
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.ui.theme.spotify_green
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
import javax.inject.Inject
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
    private val authenticationController: AuthenticationController
) {

  private val PLAYLIST_NAME = "Wanderwave"
  private val PLAYLIST_DESCRIPTION = "Liked songs from Wanderwave"
  private val PLAYLIST_ICON = "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"
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

  private val MAX_RECENT_TRACKS = 10
  val recentlyPlayedTracks = MutableStateFlow(emptyList<Track>())

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

      val url1 = "https://api.spotify.com/v1/me"
      Log.d( "SpotifyController", spotifyGetFromURL(url1))

      Log.d("SpotifyController", "Getting all playlists")
      val url = "https://api.spotify.com/v1/me/playlists"
      val playlists = spotifyGetFromURL(url)
      val jsonObject = JSONObject(playlists)
      val items = jsonObject.getJSONArray("items")
      //Convert items to list of ListItem
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
        val data = """
        {
            "name": "$PLAYLIST_NAME",
            "description": "$PLAYLIST_DESCRIPTION",
            "public": false
        }
    """.trimIndent()
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

    private suspend fun getCurrentUserId(): String {
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
        val data = """
        {
            "uris": ["${track.id}"]
        }
    """.trimIndent()
        val response = withContext(Dispatchers.IO) { authenticationController.makeApiRequest(URL(url), "POST", data) }

        if (response == "FAILURE") {
            throw Exception("Failed to add track to playlist")
        }
    }

    suspend fun removeFromPlaylist(track: Track) {
        val playlistId = createPlaylistIfNotExist()
        Log.d("SpotifyController", "Removing track from playlist: $playlistId")
        val url = "https://api.spotify.com/v1/playlists/$playlistId/tracks"
        val data = "{\n" +
                "    \"tracks\": [\n" +
                "        {\n" +
                "            \"uri\": \"${track.id}\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"snapshot_id\": \"$playlistId\"\n" +
                "}"
        withContext(Dispatchers.IO) { authenticationController.makeApiRequest(URL(url), "DELETE", data) }
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
            // prepend to the start of the recently played tracks list
            recentlyPlayedTracks.value =
                (listOf(track) + recentlyPlayedTracks.value.filterNot { it.id == track.id }).take(
                    MAX_RECENT_TRACKS)
            recentlyPlayedTracks.value
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
