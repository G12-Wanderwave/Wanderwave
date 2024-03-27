package ch.epfl.cs311.wanderwave.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

enum class Route(val routeString: String, val showBottomBar: Boolean) {
  LAUNCH("launch", false),
  LOGIN("login", false),
  MAIN("main", true),
  TRACK_LIST("trackList", true)
}

// Top level destination
data class TopLevelDestination(val route: Route, val icon: ImageVector, val textId: Int)

class NavigationActions(navController: NavHostController) {

  private val navigationController = navController

  fun navigateToTopLevel(topLevelRoute: Route) {
    navigationController.navigate(topLevelRoute.routeString) {
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

  fun navigateTo(route: Route) {
    navigationController.navigate(route.routeString)
  }

  fun goBack() {
    navigationController.popBackStack()
  }
}

val TOP_LEVEL_DESTINATIONS =
    listOf(
        TopLevelDestination(route = Route.TRACK_LIST, icon = Icons.Filled.List, textId = 3),
        TopLevelDestination(route = Route.MAIN, icon = Icons.Filled.List, textId = 4),
    )
