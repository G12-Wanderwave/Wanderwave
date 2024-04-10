package ch.epfl.cs311.wanderwave.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.navigation.Route
import ch.epfl.cs311.wanderwave.navigation.TOP_LEVEL_DESTINATIONS

val trackListIcon: ImageVector = Icons.AutoMirrored.Filled.List
val mainIcon: ImageVector = Icons.Default.Home
val mapIcon: ImageVector = Icons.Default.LocationOn

/**
 * This Composable function represents the bottom navigation bar of the app. It utilizes a
 * BottomAppBar to display three navigation icons evenly spaced across the bar. The icons serve as
 * navigation buttons for 'Track List', 'Main', and 'Map' destinations within the app. This
 * Composable also utilizes the NavigationActions class to handle the navigation logic when an icon
 * is clicked.
 *
 * @param navActions The NavigationActions object that contains the navigation logic for navigating
 *   between the top-level destinations.
 * @author Ayman Bakiri
 * @author Clarence Linden
 * @since 1.0
 * @last update 1.0
 */
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
              when (destination.route) {
                Route.TRACK_LIST -> trackListIcon
                Route.MAIN -> mainIcon
                else -> mapIcon
              }
            }
      }
    }
  }
}
