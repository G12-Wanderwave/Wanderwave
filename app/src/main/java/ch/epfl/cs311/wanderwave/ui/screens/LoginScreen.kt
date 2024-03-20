package ch.epfl.cs311.wanderwave.ui.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat.startActivity
import ch.epfl.cs311.wanderwave.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.io.IOException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

@Composable
fun LoginScreen() {
  SpotifySignIn()
}

@Composable
fun SpotifySignIn() {

  val context = LocalContext.current

  Button(
      onClick = {
        val authUrl =
            "https://accounts.spotify.com/authorize?client_id=TODO&response_type=code&redirect_uri=wanderwave://spotifycallback"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
        startActivity(context, intent, null)
        val data: Uri? = intent.data
        if (data != null && data.toString().startsWith("wanderwave://spotifycallback")) {
          val code = data.getQueryParameter("code")

          requestAccessToken(code)
        }
      }) {
        Text(text = "Sign in with Spotify")
      }
}

private fun requestAccessToken(code: String?) {

  val client = OkHttpClient()
  val requestBody =
      code?.let {
        FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("code", it)
            .add("redirect_uri", "wanderwave://spotifycallback")
            .add("client_id", "")
            .add("client_secret", "")
            .build()
      }

  val request =
      requestBody?.let {
        Request.Builder().url("https://accounts.spotify.com/api/token").post(it).build()
      }

  if (request != null) {
    client
        .newCall(request)
        .enqueue(
            object : Callback {
              override fun onFailure(call: Call, e: IOException) {
                // Handle failure
                Log.d("SPOTIFY_ACCESS", "Failed to get access token")
              }

              override fun onResponse(call: Call, response: Response) {
                // Handle success
                // Extract the access token from the response
                Log.d("SPOTIFY_ACCESS", "Successfully got access token")
              }
            })
  }
}

@Composable
fun GoogleSignIn() {
  var user by remember { mutableStateOf(Firebase.auth.currentUser) }
  val launcher =
      rememberFirebaseAuthLauncher(
          onAuthComplete = { result ->
            user = result.user
            // TODO Navigation to next screen
          },
          onAuthError = { user = null })
  val token = stringResource(R.string.default_web_client_id)
  val context = LocalContext.current
  val gso =
      GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
          .requestIdToken(token)
          .requestEmail()
          .build()
  val googleSignInClient = GoogleSignIn.getClient(context, gso)
  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = { launcher.launch(googleSignInClient.signInIntent) },
            modifier = Modifier.wrapContentSize()) {
              Text(text = "Sign in with Google")
            }
      }
}

@Composable
fun rememberFirebaseAuthLauncher(
    onAuthComplete: (AuthResult) -> Unit,
    onAuthError: (ApiException) -> Unit
): ManagedActivityResultLauncher<Intent, ActivityResult> {
  val scope = rememberCoroutineScope()
  return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
      result ->
    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
    try {
      val account = task.getResult(ApiException::class.java)!!
      val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
      scope.launch {
        val authResult = Firebase.auth.signInWithCredential(credential).await()
        onAuthComplete(authResult)
      }
    } catch (e: ApiException) {
      onAuthError(e)
    }
  }
}
