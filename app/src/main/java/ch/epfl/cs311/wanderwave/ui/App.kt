package ch.epfl.cs311.wanderwave.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
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
import ch.epfl.cs311.wanderwave.ui.screens.ProfileScreen
import ch.epfl.cs311.wanderwave.ui.screens.TrackListScreen
import ch.epfl.cs311.wanderwave.ui.theme.WanderwaveTheme
import ch.epfl.cs311.wanderwave.viewmodel.ProfileViewModel

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

  val profileViewModel: ProfileViewModel = hiltViewModel()
  var showBottomBar by remember { mutableStateOf(false) }

  val currentRouteState by navActions.currentRouteFlow.collectAsStateWithLifecycle()

  LaunchedEffect(currentRouteState) { showBottomBar = currentRouteState?.showBottomBar ?: false }

  Scaffold(
      bottomBar = {
        if (showBottomBar) {
          AppBottomBar(
              navActions = navActions,
          )
        }
      }) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.LOGIN.routeString,
            modifier = Modifier.padding(innerPadding)) {
              composable(Route.LOGIN.routeString) { LoginScreen(navActions) }
              composable(Route.ABOUT.routeString) { AboutScreen(navActions) }
              composable(Route.MAIN.routeString) { MainPlaceHolder(navActions) }
              composable(Route.TRACK_LIST.routeString) { TrackListScreen() }
              composable(Route.MAP.routeString) { MapScreen() }
              composable(Route.PROFILE_SCREEN) { ProfileScreen(navActions, profileViewModel) }
        }
      }
}
