package ch.epfl.cs311.wanderwave.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

object Route {
  const val LAUNCH = "launch"
  const val LOGIN = "login"
  const val MAIN = "main"
  const val TRACK_LIST = "trackList"
}

// Top level destination
data class TopLevelDestination(val route: String, val icon: ImageVector, val textId: Int)

class NavigationActions(navController: NavHostController) {

  private val navigationController = navController

  fun navigateTo(destination: TopLevelDestination) {
    navigationController.navigate(destination.route) {
      // Pop up to the start destination of the graph to
      // avoid building up a large stack of destinations
      // on the back stack as users select items
      popUpTo(navigationController.graph.findStartDestination().id) { saveState = true }
      // Avoid multiple copies of the same destination when
      // reselecting the same item
      launchSingleTop = true
      // Restore state when reselecting a previously selected item
      restoreState = true
    }
  }

  fun goBack() {
    navigationController.popBackStack()
  }
}

val TOP_LEVEL_DESTINATIONS =
    listOf(
        TopLevelDestination(route = Route.LAUNCH, icon = Icons.Filled.ExitToApp, textId = 1),
        TopLevelDestination(route = Route.LOGIN, icon = Icons.Filled.AccountBox, textId = 2),
        TopLevelDestination(route = Route.TRACK_LIST, icon = Icons.Filled.List, textId = 3),
        TopLevelDestination(route = Route.MAIN, icon = Icons.Filled.List, textId = 4),
    )
