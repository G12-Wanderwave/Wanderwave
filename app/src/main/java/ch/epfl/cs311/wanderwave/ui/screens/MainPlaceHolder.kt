package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import ch.epfl.cs311.wanderwave.ui.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.navigation.Route
import ch.epfl.cs311.wanderwave.ui.navigation.TOP_LEVEL_DESTINATIONS

@Composable
fun MainPlaceHolder(navigationActions: NavigationActions) {
  Row(modifier = Modifier.testTag("mainPlaceHolderScreen")) {
    Text(text = "MainPlaceHolder")
    Button(
        onClick = {
          navigationActions.navigateTo(Route.LOGIN)
        },
        modifier = Modifier.testTag("signOutButton")) {
          Text(text = "Sign Out")
        }
  }
}
