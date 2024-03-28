package ch.epfl.cs311.wanderwave.navigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import ch.epfl.cs311.wanderwave.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class Route(val routeString: String, val showBottomBar: Boolean) {
  LOGIN("login", false),
  ABOUT("about", false),
  MAIN("main", true),
  TRACK_LIST("trackList", true),
  MAP("map", true);

  companion object {
    fun forRouteString(routeString: String): Route? {
      return entries.firstOrNull { it.routeString == routeString }
    }
  }
}

// Top level destination
data class TopLevelDestination(val route: Route, val iconId: Int, val textId: Int)

class NavigationActions(navController: NavHostController) {

  private val navigationController = navController

  private var _currentRouteFlow = MutableStateFlow(getCurrentRoute())
  val currentRouteFlow: StateFlow<Route?> = _currentRouteFlow

  // Handle user manually clicking the back button
  init {
    navController.addOnDestinationChangedListener { _, destination, _ ->
      _currentRouteFlow.value = Route.forRouteString(destination.route ?: Route.LOGIN.routeString)
    }
  }

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
    _currentRouteFlow.value = topLevelRoute
  }

  fun getCurrentRoute(): Route? {
    navigationController.currentDestination?.route?.let {
      return Route.forRouteString(it)
    }
    return null
  }

  fun navigateTo(route: Route) {
    navigationController.navigate(route.routeString)
    _currentRouteFlow.value = route
  }

  fun goBack() {
    navigationController.popBackStack()
  }

  fun signIn() {
    navigationController.navigate(Route.MAIN.routeString) {
      popUpTo(navigationController.graph.startDestinationId) {
        inclusive = true
      }
      launchSingleTop = true
    }
  }
}


val TOP_LEVEL_DESTINATIONS =
    listOf(
        TopLevelDestination(
            route = Route.TRACK_LIST, iconId = R.drawable.tracklist, textId = R.string.trackList),
        // 0 is temporary, main will be removed later
        TopLevelDestination(route = Route.MAIN, iconId = 0, textId = R.string.main),
        TopLevelDestination(route = Route.MAP, iconId = R.drawable.map, textId = R.string.map))
