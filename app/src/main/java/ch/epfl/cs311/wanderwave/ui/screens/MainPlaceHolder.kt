package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import ch.epfl.cs311.wanderwave.ui.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.navigation.Route
import ch.epfl.cs311.wanderwave.ui.navigation.TOP_LEVEL_DESTINATIONS

@Composable
fun MainPlaceHolder(navigationActions: NavigationActions) {
  Row {
    Text(text = "MainPlaceHolder")
    Button(
        onClick = {
          navigationActions.navigateTo(TOP_LEVEL_DESTINATIONS.first { it.route == Route.LOGIN })
        }) {
          Text(text = "Sign Out")
        }
  }
}
