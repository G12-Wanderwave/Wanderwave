package ch.epfl.cs311.wanderwave.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.navigation.Route
import ch.epfl.cs311.wanderwave.ui.components.AppBottomBar
import ch.epfl.cs311.wanderwave.ui.screens.AboutScreen
import ch.epfl.cs311.wanderwave.ui.screens.LoginScreen
import ch.epfl.cs311.wanderwave.ui.screens.MainPlaceHolder
import ch.epfl.cs311.wanderwave.ui.screens.MapScreen
import ch.epfl.cs311.wanderwave.ui.screens.SpotifyConnectScreen
import ch.epfl.cs311.wanderwave.ui.screens.TrackListScreen
import ch.epfl.cs311.wanderwave.ui.theme.WanderwaveTheme
import kotlinx.coroutines.launch

@Composable
fun App(navController: NavHostController) {
  WanderwaveTheme {
    Surface(
        modifier = Modifier.fillMaxSize().testTag("appScreen"),
        color = MaterialTheme.colorScheme.background) {
          AppScaffold(navController)
        }
  }
}

@Composable
fun AppScaffold(navController: NavHostController) {
  val navActions = NavigationActions(navController)
  var showBottomBar by remember { mutableStateOf(false) }
  val currentRouteState by navActions.currentRouteFlow.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }

  val scope = rememberCoroutineScope()
  val showSnackbar = { message: String ->
    scope.launch { snackbarHostState.showSnackbar(message) }
    Unit
  }

  LaunchedEffect(currentRouteState) { showBottomBar = currentRouteState?.showBottomBar ?: false }

  Scaffold(
      bottomBar = {
        if (showBottomBar) {
          AppBottomBar(
              navActions = navActions,
          )
        }
      },
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.SPOTIFY_CONNECT.routeString,
            modifier = Modifier.padding(innerPadding)) {
              composable(Route.SPOTIFY_CONNECT.routeString) { SpotifyConnectScreen(navActions) }
              composable(Route.LOGIN.routeString) { LoginScreen(navActions, showSnackbar) }
              composable(Route.ABOUT.routeString) { AboutScreen(navActions) }
              composable(Route.MAIN.routeString) { MainPlaceHolder(navActions) }
              composable(Route.TRACK_LIST.routeString) { TrackListScreen(showSnackbar) }
              composable(Route.MAP.routeString) { MapScreen() }
            }
      }
}
