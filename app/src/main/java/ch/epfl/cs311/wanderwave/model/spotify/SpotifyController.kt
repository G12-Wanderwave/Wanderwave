package ch.epfl.cs311.wanderwave.model.spotify

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
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

  public var appRemote: SpotifyAppRemote? = null

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

  @OptIn(FlowPreview::class)
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

  /**
   * get the element under the tab "listen recently"
   *
   * @return a Flow of ListItem
   * @author Menzo Bouaissi
   * @since 2.0
   * @last update 2.0
   */
  @OptIn(FlowPreview::class)
  fun getTrack(): Flow<ListItem> {
    return callbackFlow {
      val callResult =
          appRemote?.let { it ->
            it.contentApi
                .getRecommendedContentItems(ContentApi.ContentType.DEFAULT)
                .setResultCallback {
                  for (i in it.items) if (i.uri == "spotify:section:0JQ5DAroEmF9ANbLaiJ7Wx")
                      trySend(i)
                  // TODO checkt if "listen recently playlist" the same for everyone
                }
                .setErrorCallback { trySend(ListItem("", "", null, "", "", false, false)) }
          }
      awaitClose { callResult?.cancel() }
    }
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
    val spotifyContent = "https://open.spotify.com/album/0sNOF9WDwhWunNAHPD3Baj"
    val branchLink =
        "https://spotify.link/content_linking?~campaign=" +
            context.packageName +
            "&\$deeplink_path=" +
            spotifyContent +
            "&\$fallback_url=" +
            spotifyContent
    val intent = Intent(Intent.ACTION_VIEW)
    intent.setData(Uri.parse(branchLink))
    // startActivity(intent)
    // val test = createAppAuthRequest(context)
    // Log.d("show test",test.toString())
    val list: MutableList<ListItem> = emptyList<ListItem>().toMutableList()
    return callbackFlow {
      val callResult =
          appRemote?.let { it ->
            it.contentApi
                .getRecommendedContentItems(ContentApi.ContentType.DEFAULT)
                .setResultCallback {
                  Log.d("all the items", it.items.toString())
                  for (i in it.items) list +=
                      i // Log.d("show me all",i.toString())//if (i.uri ==
                        // "spotify:section:0JQ5DAroEmF9ANbLaiJ7Wx")
                  for (i in list) Log.d("show me all", i.toString())
                  trySend(list)
                  // TODO checkt if "listen recently playlist" the same for everyone
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
