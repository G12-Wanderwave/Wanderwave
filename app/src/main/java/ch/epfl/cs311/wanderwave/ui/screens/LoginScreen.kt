package ch.epfl.cs311.wanderwave.ui.screens

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.navigation.Route
import ch.epfl.cs311.wanderwave.ui.components.login.LoginAppLogo
import ch.epfl.cs311.wanderwave.ui.components.login.SignInButton
import ch.epfl.cs311.wanderwave.ui.components.login.WelcomeTitle
import ch.epfl.cs311.wanderwave.viewmodel.LoginScreenViewModel
import com.spotify.sdk.android.auth.AuthorizationClient

@Composable
fun LoginScreen(
    navigationActions: NavigationActions,
    showMessage: (String) -> Unit,
    viewModel: LoginScreenViewModel = hiltViewModel()
) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()

  LaunchedEffect(state) {
    state.let {
      if (it.hasResult) {
        if (it.message != null) {
          showMessage(it.message)
        }
        if (it.success) {
          navigationActions.navigateTo(Route.SPOTIFY_CONNECT)
        }
      }
    }
  }

  val launcher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.StartActivityForResult()) {
            val response = AuthorizationClient.getResponse(it.resultCode, it.data)
            viewModel.handleAuthorizationResponse(response)
          }
  val context = LocalContext.current

  Column(modifier = Modifier.testTag("loginScreen")) {
    LoginAppLogo(modifier = Modifier.weight(1f))
    WelcomeTitle(modifier = Modifier.weight(4f))
    SignInButton(modifier = Modifier.weight(1f)) {
            val intent =
                AuthorizationClient.createLoginActivityIntent(
                    context.getActivity(), viewModel.getAuthorizationRequest())
            launcher.launch(intent)
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
