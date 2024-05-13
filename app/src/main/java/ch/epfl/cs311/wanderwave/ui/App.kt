package ch.epfl.cs311.wanderwave.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.navigation.Route
import ch.epfl.cs311.wanderwave.ui.components.AppBottomBar
import ch.epfl.cs311.wanderwave.ui.components.player.SurroundWithMiniPlayer
import ch.epfl.cs311.wanderwave.ui.screens.AboutScreen
import ch.epfl.cs311.wanderwave.ui.screens.BeaconScreen
import ch.epfl.cs311.wanderwave.ui.screens.EditProfileScreen
import ch.epfl.cs311.wanderwave.ui.screens.LoginScreen
import ch.epfl.cs311.wanderwave.ui.screens.MapScreen
import ch.epfl.cs311.wanderwave.ui.screens.ProfileScreen
import ch.epfl.cs311.wanderwave.ui.screens.ProfileViewOnlyScreen
import ch.epfl.cs311.wanderwave.ui.screens.SelectSongScreen
import ch.epfl.cs311.wanderwave.ui.screens.SpotifyConnectScreen
import ch.epfl.cs311.wanderwave.ui.screens.TrackListScreen
import ch.epfl.cs311.wanderwave.ui.theme.WanderwaveTheme
import ch.epfl.cs311.wanderwave.viewmodel.ProfileViewModel
import ch.epfl.cs311.wanderwave.viewmodel.TrackListViewModel
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
  val profileViewModel: ProfileViewModel = hiltViewModel()
  val trackListViewModel = hiltViewModel<TrackListViewModel>()

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
      }) { innerPadding ->
        SurroundWithMiniPlayer(displayPlayer = showBottomBar) {
          NavHost(
              navController = navController,
              startDestination = Route.SPOTIFY_CONNECT.routeString,
              modifier =
                  Modifier.padding(innerPadding).background(MaterialTheme.colorScheme.background)) {
                composable(Route.LOGIN.routeString) { LoginScreen(navActions, showSnackbar) }
                composable(Route.SPOTIFY_CONNECT.routeString) { SpotifyConnectScreen(navActions) }
                composable(Route.ABOUT.routeString) { AboutScreen(navActions) }
                composable(Route.TRACK_LIST.routeString) {
                  TrackListScreen(navActions, showSnackbar, trackListViewModel)
                }
                composable(Route.MAP.routeString) { MapScreen(navActions) }
                composable(Route.PROFILE.routeString) {
                  ProfileScreen(navActions, profileViewModel)
                }
                composable(Route.EDIT_PROFILE.routeString) {
                  EditProfileScreen(navActions, profileViewModel)
                }
                composable(
                    route = "${Route.SELECT_SONG.routeString}/{viewModelType}",
                    arguments =
                        listOf(navArgument("viewModelType") { type = NavType.StringType })) {
                        backStackEntry ->
                      val viewModelType = backStackEntry.arguments?.getString("viewModelType")
                      val viewModel =
                          when (viewModelType) {
                            "profile" -> profileViewModel
                            "tracklist" -> trackListViewModel
                            else ->
                                throw IllegalStateException(
                                    "Invalid ViewModel type for SelectSongScreen")
                          }

                      SelectSongScreen(navActions, viewModel)
                    }
                composable("${Route.VIEW_PROFILE.routeString}/{profileId}") {
                  ProfileViewOnlyScreen(it.arguments?.getString("profileId") ?: "", navActions)
                }
                composable("${Route.BEACON.routeString}/{beaconId}") {
                  BeaconScreen(it.arguments?.getString("beaconId") ?: "", navActions)
                }
              }
        }
      }
}
