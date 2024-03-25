package ch.epfl.cs311.wanderwave.ui.components.login

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import ch.epfl.cs311.wanderwave.BuildConfig
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse

private val CLIENT_ID = BuildConfig.SPOTIFY_CLIENT_ID
private val REDIRECT_URI = "wanderwave-auth://callback"
private val DEFAULT_SCOPES =
  listOf(
    "app-remote-control",
    "playlist-read-private",
    "playlist-read-collaborative",
    "user-library-read",
    "user-read-email",
    "user-read-private"
  )

/**
 * When first called, this function will open the Spotify login activity.
 *
 * @param onSuccess Callback that will be called when the user successfully logs in.
 *    Parameters are the token and the "expiresIn" value.
 * @param onError Callback that will be called when the user fails to log in.
 *    Parameter is the error message.
 * @param onCancel Callback that will be called when the user cancels the login process.
 * @param scopes The scopes that the app will request from the user. By default asks for mostly
 *   everything we might need.
 */
@Composable
fun SpotifyAuthentication(
  onSuccess: (String, Int) -> Unit,
  onError: (String) -> Unit,
  onCancel: () -> Unit,
  scopes: List<String> = DEFAULT_SCOPES
) {
  val launcher =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.StartActivityForResult()
    ) {
      val response = AuthorizationClient.getResponse(it.resultCode, it.data)
      when (response.type) {
        AuthorizationResponse.Type.TOKEN -> {
          onSuccess(response.accessToken, response.expiresIn)
        }

        AuthorizationResponse.Type.ERROR -> {
          onError(response.error)
        }

        else -> {
          onCancel()
        }
      }
    }

  val context = LocalContext.current

  LaunchedEffect(Unit) {
    val builder =
      AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI)
    builder.setScopes(scopes.toTypedArray())
    val request = builder.build()

    val activity = context.getActivity()
    val intent = AuthorizationClient.createLoginActivityIntent(activity, request)

    launcher.launch(intent)
  }
}

private fun Context.getActivity(): ComponentActivity? {
  var currentContext = this
  while (currentContext is ContextWrapper) {
    if (currentContext is ComponentActivity) {
      return currentContext
    }
    currentContext = currentContext.baseContext
  }
  return null
}
