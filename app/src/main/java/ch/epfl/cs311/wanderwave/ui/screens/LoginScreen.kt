package ch.epfl.cs311.wanderwave.ui.screens

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import ch.epfl.cs311.wanderwave.ui.components.login.LoginScreenHeader
import ch.epfl.cs311.wanderwave.ui.components.login.SignInButton
import ch.epfl.cs311.wanderwave.ui.components.login.WelcomeTitle
import ch.epfl.cs311.wanderwave.ui.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.navigation.Route
import ch.epfl.cs311.wanderwave.viewmodel.LoginScreenViewModel
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationResponse
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navigationActions: NavigationActions,
    showMessage: (String) -> Unit,
    viewModel: LoginScreenViewModel = hiltViewModel()
) {
  val scope = rememberCoroutineScope()
  val launcher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.StartActivityForResult()) {
            handleLoginActivityResult(
                it,
                { token, expiresIn ->
                  scope.launch { viewModel.handleTokenResponse(token, expiresIn) }
                },
                showMessage,
                navigationActions)
          }
  val context = LocalContext.current

  Column(modifier = Modifier.testTag("loginScreen")) {
    LoginScreenHeader(modifier = Modifier.weight(1.5f))
    WelcomeTitle(modifier = Modifier.weight(4f))
    SignInButton(modifier = Modifier.weight(1f)) {
      val intent =
          AuthorizationClient.createLoginActivityIntent(
              context.getActivity(), viewModel.getAuthorizationRequest())
      launcher.launch(intent)
    }
  }
}

fun handleLoginActivityResult(
    result: ActivityResult,
    handleToken: (String, Int) -> Unit,
    showMessage: (String) -> Unit,
    navigationActions: NavigationActions
) {
  val response = AuthorizationClient.getResponse(result.resultCode, result.data)
  when (response.type) {
    AuthorizationResponse.Type.TOKEN -> {
      handleToken(response.accessToken, response.expiresIn)
      showMessage("Logged in with token: ${response.accessToken}, expiresIn: ${response.expiresIn}")
      navigationActions.navigateTo(Route.MAIN)
    }
    AuthorizationResponse.Type.ERROR -> {
      showMessage("Error logging in: ${response.error}")
    }
    else -> {
      showMessage("User cancelled login")
    }
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
