package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.runtime.Composable
import ch.epfl.cs311.wanderwave.ui.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.navigation.Route

@Composable
fun LaunchScreen(navigationActions: NavigationActions) {
  navigationActions.navigateTo(Route.LOGIN)
}
