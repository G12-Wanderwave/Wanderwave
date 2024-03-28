package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.navigation.Route

@Composable
fun MainPlaceHolder(navigationActions: NavigationActions) {
  Row(modifier = Modifier.testTag("mainPlaceHolderScreen")) {
    Text(text = "MainPlaceHolder")
    Button(
        onClick = { navigationActions.navigateTo(Route.LOGOUT) },
        modifier = Modifier.testTag("signOutButton")) {
          Text(text = "Sign Out")
        }
  }
}
