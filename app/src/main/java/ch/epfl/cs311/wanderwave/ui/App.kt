package ch.epfl.cs311.wanderwave.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ch.epfl.cs311.wanderwave.AppResources
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.navigation.Route
import ch.epfl.cs311.wanderwave.ui.components.AppBottomBar
import ch.epfl.cs311.wanderwave.ui.components.map.getIcon
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
import ch.epfl.cs311.wanderwave.viewmodel.BeaconViewModel
import ch.epfl.cs311.wanderwave.viewmodel.MapViewModel
import ch.epfl.cs311.wanderwave.viewmodel.ProfileViewModel
import ch.epfl.cs311.wanderwave.viewmodel.TrackListViewModel
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.maps.MapsInitializer
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
  val navActions = remember { NavigationActions(navController) }
  var showBottomBar by remember { mutableStateOf(false) }
  val currentRouteState by navActions.currentRouteFlow.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }
  val profileViewModel: ProfileViewModel = hiltViewModel()
  val trackListViewModel = hiltViewModel<TrackListViewModel>()
  val beaconViewModel = hiltViewModel<BeaconViewModel>()
  val mapViewModel = hiltViewModel<MapViewModel>()

  CreateIcon()

  val scope = rememberCoroutineScope()
  val showSnackbar = { message: String ->
    scope.launch { snackbarHostState.showSnackbar(message) }
    Unit
  }

  LaunchedEffect(currentRouteState) { showBottomBar = currentRouteState?.showBottomBar ?: false }

  val online = isOnline(LocalContext.current)

  Scaffold(
      bottomBar = {
        if (showBottomBar) {
          AppBottomBar(navActions = navActions, online = online)
        }
      }) { innerPadding ->
        SurroundWithMiniPlayer(displayPlayer = showBottomBar) {
          NavHost(
              navController = navController,
              enterTransition = { EnterTransition.None },
              exitTransition = { ExitTransition.None },
              popEnterTransition = { EnterTransition.None },
              popExitTransition = { ExitTransition.None },
              startDestination =
                  if (online) Route.SPOTIFY_CONNECT.routeString else Route.TRACK_LIST.routeString,
              modifier =
                  Modifier.padding(innerPadding).background(MaterialTheme.colorScheme.background)) {
                if (online)
                    composable(Route.LOGIN.routeString) { LoginScreen(navActions, showSnackbar) }
                if (online)
                    composable(Route.SPOTIFY_CONNECT.routeString) {
                      SpotifyConnectScreen(navActions)
                    }
                composable(Route.ABOUT.routeString) { AboutScreen(navActions) }
                composable(Route.TRACK_LIST.routeString) {
                  TrackListScreen(navActions, trackListViewModel, online)
                }
                if (online)
                    composable(Route.MAP.routeString) { MapScreen(navActions, mapViewModel) }
                composable(Route.PROFILE.routeString) {
                  ProfileScreen(navActions, profileViewModel, online)
                }
                if (online)
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
                            "beacon" -> beaconViewModel
                            else -> error("Invalid ViewModel type for SelectSongScreen")
                          }

                      SelectSongScreen(navActions, viewModel)
                    }
                composable("${Route.VIEW_PROFILE.routeString}/{profileId}") {
                  ProfileViewOnlyScreen(it.arguments?.getString("profileId") ?: "", navActions)
                }

                if (online)
                    composable("${Route.BEACON.routeString}/{beaconId}") {
                      BeaconScreen(
                          it.arguments?.getString("beaconId") ?: "",
                          profileViewModel,
                          navActions,
                          beaconViewModel)
                    }
              }
        }
      }
}

@Composable
private fun CreateIcon() {
  val context = LocalContext.current
  try {
    MapsInitializer.initialize(context)
    AppResources.beaconIcon = getIcon(context)
  } catch (e: GooglePlayServicesNotAvailableException) {
    e.printStackTrace()
  }
}

private fun isOnline(context: Context): Boolean {
  val connectivityManager =
      context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
  val network = connectivityManager.activeNetwork ?: return false
  val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
  return when {
    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
    else -> false
  }
}
