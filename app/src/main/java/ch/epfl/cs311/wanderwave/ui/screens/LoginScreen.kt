package ch.epfl.cs311.wanderwave.ui.screens

import android.content.Intent
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
import ch.epfl.cs311.wanderwave.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun LoginScreen() {
  GoogleSignIn()
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
