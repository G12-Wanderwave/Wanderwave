package ch.epfl.cs311.wanderwave.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.navigation.TOP_LEVEL_DESTINATIONS

@Composable
fun AppBottomBar(navActions: NavigationActions) {
  Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.testTag("appBottomBar")) {
    BottomAppBar(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface),
    ) {
      for (destination in TOP_LEVEL_DESTINATIONS) {
        Button(
            onClick = { navActions.navigateToTopLevel(destination.route) },
            modifier =
                Modifier.padding(8.dp)
                    .testTag("bottomAppBarButton" + destination.route.routeString)) {
              Text(text = destination.route.routeString)
            }
      }
    }
  }
}
