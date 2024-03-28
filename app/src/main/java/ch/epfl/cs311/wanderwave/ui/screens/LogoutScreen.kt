package ch.epfl.cs311.wanderwave.ui.screens

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.navigation.Route
import ch.epfl.cs311.wanderwave.viewmodel.LogoutScreenViewModel
import com.spotify.sdk.android.auth.AuthorizationClient

@Composable
fun LogoutScreen(
    navigationActions: NavigationActions,
    showMessage: (String) -> Unit,
    viewModel: LogoutScreenViewModel = hiltViewModel()
) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()

  val launcher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.StartActivityForResult()) {
            val response = AuthorizationClient.getResponse(it.resultCode, it.data)
            viewModel.handleAuthorizationResponse(response)
          }
  val context = LocalContext.current

  LaunchedEffect(state) {
    state.let {
      if (it.hasResult) {
        if (it.message != null) {
          showMessage(it.message)
        }
        if (it.success) {
          navigationActions.navigateTo(Route.LOGIN)
        }
      } else {

        val intent =
            AuthorizationClient.createLoginActivityIntent(
                context.getActivity(), viewModel.getAuthorizationRequest())
        launcher.launch(intent)
      }
    }
  }

  Box(
      modifier = Modifier.fillMaxSize().testTag("logoutScreen"),
      contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.width(64.dp).testTag("logoutProgressIndicator"),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant)
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
