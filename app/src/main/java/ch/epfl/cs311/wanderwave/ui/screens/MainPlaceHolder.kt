package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.runtime.Composable
import ch.epfl.cs311.wanderwave.navigation.NavigationActions

@Composable
fun MainPlaceHolder(navigationActions: NavigationActions) {
  AboutScreen(navigationActions = navigationActions)
}
