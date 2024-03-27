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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.epfl.cs311.wanderwave.ui.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.navigation.Route
import ch.epfl.cs311.wanderwave.ui.navigation.TOP_LEVEL_DESTINATIONS

@Composable
fun AppBottomBar(navActions: NavigationActions, currentRoute: String?) {
  Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.testTag("appBottomBar")) {
    if (currentRoute != Route.LOGIN && currentRoute != Route.LAUNCH) {
      BottomAppBar(
          modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
            TOP_LEVEL_DESTINATIONS.forEach { destination ->
              Button(
                  onClick = { navActions.navigateTo(destination) },
                  modifier =
                      Modifier.padding(8.dp).testTag("bottomAppBarButton" + destination.route)) {
                    Text(text = stringResource(destination.textId))
                  }
            }
          }
    }
  }
}
