package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.navigation.Route
import ch.epfl.cs311.wanderwave.ui.components.utils.LoadingScreen
import ch.epfl.cs311.wanderwave.viewmodel.ProfileViewModel
import ch.epfl.cs311.wanderwave.viewmodel.SpotifyConnectScreenViewModel

@Composable
fun SpotifyConnectScreen(
    navigationActions: NavigationActions,
    viewModel: SpotifyConnectScreenViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()

  LaunchedEffect(state) {
    if (state.hasResult) {
      if (state.success) {
        profileViewModel.getProfileOfCurrentUser(false)
        val profileResult = profileViewModel.uiState.value.error
        if (profileResult == "Document does not exist") {
          viewModel.updateState(success = true, isFirstTime = true)
        } else {
          viewModel.updateState(success = true, isFirstTime = false)
        }
      } else {
        navigationActions.navigateToTopLevel(Route.LOGIN)
      }
    } else {
      viewModel.connectRemote()
    }
  }

  if (state.hasResult && state.success) {
    if (state.isFirstTime) {
      navigationActions.navigateToTopLevel(Route.EDIT_PROFILE)
    } else {
      navigationActions.navigateToTopLevel(Route.MAP)
    }
  }

  LoadingScreen(Modifier.testTag("spotifyConnectScreen"))
}
