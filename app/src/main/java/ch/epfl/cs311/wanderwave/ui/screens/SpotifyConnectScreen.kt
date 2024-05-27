package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.navigation.Route
import ch.epfl.cs311.wanderwave.ui.components.utils.LoadingScreen
import ch.epfl.cs311.wanderwave.viewmodel.SpotifyConnectScreenViewModel


@Composable
fun SpotifyConnectScreen(
  navigationActions: NavigationActions,
  viewModel: SpotifyConnectScreenViewModel = hiltViewModel()
) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  val isFirstTime by viewModel.isFirstTime.collectAsState()
  val coroutineScope = rememberCoroutineScope()

  LaunchedEffect(state.hasResult, state.success) {
    if (state.hasResult) {
      if (state.success) {
          viewModel.checkIfFirstTime()
      } else {
        navigationActions.navigateToTopLevel(Route.LOGIN)
      }
    }// when I add this line the test spotifyConnectScreenTriesToConnect pass but the test
    //for app bottom bar loops
  // else{
      //viewModel.connectRemote()
    //}
  }

  LaunchedEffect(isFirstTime) {
    if (isFirstTime) {
      navigationActions.navigateToTopLevel(Route.EDIT_PROFILE)
    } else if (state.hasResult && state.success) {
      navigationActions.navigateToTopLevel(Route.MAP)
    }
  }

  LoadingScreen(Modifier.testTag("spotifyConnectScreen"))
}