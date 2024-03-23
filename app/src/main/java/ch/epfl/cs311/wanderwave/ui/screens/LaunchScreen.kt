package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.runtime.Composable
import ch.epfl.cs311.wanderwave.ui.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.navigation.Route
import ch.epfl.cs311.wanderwave.ui.navigation.TOP_LEVEL_DESTINATIONS

@Composable
fun LaunchScreen(navigationActions: NavigationActions) {
  navigationActions.navigateTo(TOP_LEVEL_DESTINATIONS.first { it.route == Route.LOGIN })
}

/*
 val user by userSessionViewModel.user.observeAsState()

 LaunchedEffect(key1 = user) {
     if (user != null) {
         navActions.navigateTo(TOP_LEVEL_DESTINATIONS.first { it.route == Route.MAIN })
     } else {
         navActions.navigateTo(DESTINATIONS.first { it.route == Route.LOGIN })
     }
 }
*/
