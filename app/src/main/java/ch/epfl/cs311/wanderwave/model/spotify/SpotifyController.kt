package ch.epfl.cs311.wanderwave.model.spotify

import android.content.Context
import android.os.Handler
import android.os.Looper
import ch.epfl.cs311.wanderwave.BuildConfig
import ch.epfl.cs311.wanderwave.model.data.Track
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.ContentApi
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.NotLoggedInException
import com.spotify.protocol.types.ListItem
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

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

  private val connectionParams =
      ConnectionParams.Builder(CLIENT_ID).setRedirectUri(REDIRECT_URI).showAuthView(true).build()

  var appRemote: SpotifyAppRemote? = null

  @Volatile private var onTrackEndCallback: (() -> Unit)? = null

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
                startPeriodicCheck()
                println("Connected to Spotify App Remote")
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
    stopPeriodicCheck()
    appRemote?.let { SpotifyAppRemote.disconnect(it) }
  }

  fun playTrack(track: Track): Flow<Boolean> {
    return callbackFlow {
      val callResult =
          appRemote?.let {
            it.playerApi
                .play(track.id)
                .setResultCallback { trySend(true) }
                .setErrorCallback { trySend(false) }
          }
      awaitClose { callResult?.cancel() }
    }
  }

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

  var handler = Handler(Looper.getMainLooper())

  fun startPeriodicCheck() {
    val updateRunnable =
        object : Runnable {
          override fun run() {
            appRemote?.playerApi?.playerState?.setResultCallback { playerState ->
              playerState.track?.let { track ->
                val trackDuration = track.duration
                val currentPosition = playerState.playbackPosition
                if (trackDuration - currentPosition <= 1500) {
                  onTrackEndCallback?.invoke()
                }
              }
              handler.postDelayed(this, 1000)
            }
          }
        }

    handler.post(updateRunnable)
  }

  fun stopPeriodicCheck() {
    handler.removeCallbacksAndMessages(null)
  }

  fun setOnTrackEndCallback(callback: () -> Unit) {
    onTrackEndCallback = callback
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
