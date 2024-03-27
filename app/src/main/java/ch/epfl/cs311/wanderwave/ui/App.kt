package ch.epfl.cs311.wanderwave.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ch.epfl.cs311.wanderwave.ui.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.navigation.Route
import ch.epfl.cs311.wanderwave.ui.screens.LaunchScreen
import ch.epfl.cs311.wanderwave.ui.screens.LoginScreen
import ch.epfl.cs311.wanderwave.ui.screens.MainPlaceHolder
import ch.epfl.cs311.wanderwave.ui.screens.TrackListScreen
import ch.epfl.cs311.wanderwave.ui.theme.WanderwaveTheme

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

  NavHost(
      navController = navController,
      startDestination = Route.LAUNCH,
  ) {
    composable(Route.LAUNCH) { LaunchScreen(navActions) }
    composable(Route.LOGIN) { LoginScreen(navActions) }
    composable(Route.MAIN) { MainPlaceHolder(navActions) }
    composable(Route.TRACK_LIST) { TrackListScreen(navActions) }
  }
}
