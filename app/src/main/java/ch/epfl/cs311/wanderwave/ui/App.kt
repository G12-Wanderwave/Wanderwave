package ch.epfl.cs311.wanderwave.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import ch.epfl.cs311.wanderwave.ui.components.AppBottomBar
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
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route
  val navActions = NavigationActions(navController)
  var bottomBarState by remember { mutableStateOf(false) }

  // For some reason it doesn't work if I put them directly in mutableMapOf
  val routesMap: MutableMap<Route, @Composable () -> Unit> = mutableMapOf()
  routesMap[Route.LAUNCH] = @Composable { LaunchScreen(navActions) }
  routesMap[Route.LOGIN] = @Composable { LoginScreen(navActions) }
  routesMap[Route.MAIN] = @Composable { MainPlaceHolder(navActions) }
  routesMap[Route.TRACK_LIST] = @Composable { TrackListScreen() }

  Scaffold(
      bottomBar = {
        if (bottomBarState) {
          AppBottomBar(
              navActions = navActions,
              currentRoute = currentRoute,
          )
        }
      }) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.LAUNCH.routeString,
            modifier = Modifier.padding(innerPadding)) {
              routesMap.forEach { (route, content) ->
                composable(route.routeString) {
                  bottomBarState = route.showBottomBar
                  content()
                }
              }
            }
      }
}
