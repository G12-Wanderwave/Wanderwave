package ch.epfl.cs311.wanderwave.model.spotify

import android.content.Context
import ch.epfl.cs311.wanderwave.BuildConfig
import ch.epfl.cs311.wanderwave.model.data.Track
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.PlayerApi.StreamType
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.NotLoggedInException
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.timeout
import java.time.Duration
import kotlin.time.Duration.Companion.seconds

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
    builder.setScopes(SCOPES.toTypedArray())
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
              }

              override fun onFailure(throwable: Throwable) {
                when (throwable) {
                  is NotLoggedInException -> trySend(ConnectResult.NOT_LOGGED_IN)
                  else -> trySend(ConnectResult.FAILED)
                }
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
      val callResult = appRemote?.let {
        it.playerApi.play(track.id)
          .setResultCallback { trySend(true) }
          .setErrorCallback { trySend(false) }
      }
      awaitClose {callResult?.cancel()}
    }
  }

  data class State(
      val isAuthorized: Boolean = false,
      val token: String = "",
      val isRemoteConnected: Boolean = false,
      val spotifyAppRemote: SpotifyAppRemote? = null
  )

  enum class ConnectResult {
    SUCCESS,
    NOT_LOGGED_IN,
    FAILED
  }
}
