package ch.epfl.cs311.wanderwave.navigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import ch.epfl.cs311.wanderwave.R
import ch.epfl.cs311.wanderwave.model.data.viewModelType
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class Route(val routeString: String, val showBottomBar: Boolean) {
  LOGIN("login", false),
  SPOTIFY_CONNECT("spotifyConnect", false),
  ABOUT("about", false),
  MAIN("main", true),
  TRACK_LIST("trackList", true),
  MAP("map", true),
  PROFILE("profile", true),
  EDIT_PROFILE("editprofile", true),
  BEACON("beacon", true),
  VIEW_PROFILE("viewProfile", false),
  SELECT_SONG("selectsong", false);

  companion object {
    fun forRouteString(routeString: String): Route? {
      val topLevelRouteString = routeString.split("/").firstOrNull()
      return entries.firstOrNull { it.routeString == topLevelRouteString }
    }
  }
}

// Top level destination
data class TopLevelDestination(val route: Route, val textId: Int)

class NavigationActions(navController: NavHostController) {

  private val navigationController = navController

  private var _currentRouteFlow = MutableStateFlow(getCurrentRoute())
  val currentRouteFlow: StateFlow<Route?> = _currentRouteFlow

  // Handle user manually clicking the back button
  init {
    navController.addOnDestinationChangedListener { _, destination, _ ->
      _currentRouteFlow.value = destination.route?.let { Route.forRouteString(it) }
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
  }

  fun getCurrentRoute(): Route? {
    navigationController.currentDestination?.route?.let {
      return Route.forRouteString(it)
    }
    return null
  }

  fun navigateTo(route: Route) {
    navigationController.navigate(route.routeString)
  }

  fun navigateToBeacon(beaconId: String) {
    navigationController.navigate("${Route.BEACON.routeString}/$beaconId")
  }

  fun navigateToProfile(profileId: String) {
    navigationController.navigate("${Route.VIEW_PROFILE.routeString}/$profileId")
  }

  fun navigateToSelectSongScreen(viewModelType: viewModelType) {
    val type =
        viewModelType.name.lowercase(Locale.getDefault()) // Converts enum to a lowercase string
    navigationController.navigate("${Route.SELECT_SONG.routeString}/$type")
  }

  fun goBack() {
    navigationController.popBackStack()
  }
}

val TOP_LEVEL_DESTINATIONS =
    listOf(
        TopLevelDestination(route = Route.TRACK_LIST, textId = R.string.trackList),
        TopLevelDestination(route = Route.MAP, textId = R.string.map),
        TopLevelDestination(route = Route.PROFILE, textId = R.string.profile),
    )
