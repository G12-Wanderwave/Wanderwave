package ch.epfl.cs311.wanderwave.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import ch.epfl.cs311.wanderwave.R

object Route {
  const val LAUNCH = "launch"
  const val LOGIN = "login"
  const val MAIN = "main"
  const val TRACK_LIST = "trackList"
}

// Top level destination
data class TopLevelDestination(val route: String, val iconId: Int, val textId: Int)

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

  fun navigateTo(route: String) {
    navigationController.navigate(route)
  }

  fun goBack() {
    navigationController.popBackStack()
  }
}

val TOP_LEVEL_DESTINATIONS =
    listOf(
        TopLevelDestination(route = Route.TRACK_LIST, iconId = R.drawable.tracklist, textId = R.string.trackList),
        // 0 is temporary, main will be removed later
        TopLevelDestination(route = Route.MAIN, iconId = 0, textId = R.string.main),
    )
