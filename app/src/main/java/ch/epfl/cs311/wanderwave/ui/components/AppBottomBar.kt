package ch.epfl.cs311.wanderwave.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import ch.epfl.cs311.wanderwave.R
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.navigation.Route
import ch.epfl.cs311.wanderwave.navigation.TOP_LEVEL_DESTINATIONS

val trackListIcon: ImageVector = Icons.Default.List
val profileIcon: ImageVector = Icons.Default.Person
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
fun AppBottomBar(navActions: NavigationActions, online: Boolean) {
  BottomAppBar(
      modifier = Modifier.fillMaxWidth().testTag("appBottomBar"), containerColor = Color.Black) {
        // Assumes TOP_LEVEL_DESTINATIONS are in the order of Track List, Main, and Map for indexing
        IconButton(
            onClick = { navActions.navigateToTopLevel(TOP_LEVEL_DESTINATIONS[0].route) },
            modifier =
                Modifier.weight(1f) // Spread the icons evenly across the BottomAppBar
                    .testTag("bottomAppBarButton" + Route.TRACK_LIST.routeString)) {
              Column(
                  verticalArrangement = Arrangement.Center,
                  horizontalAlignment = Alignment.CenterHorizontally,
                  modifier = Modifier.fillMaxHeight()) {
                    Icon(
                        trackListIcon,
                        contentDescription = stringResource(id = TOP_LEVEL_DESTINATIONS[0].textId),
                    )
                    Text(
                        text = stringResource(id = R.string.trackList),
                        style = MaterialTheme.typography.bodySmall)
                  }
            }

        if (online) {
          IconButton(
              onClick = { navActions.navigateToTopLevel(TOP_LEVEL_DESTINATIONS[1].route) },
              modifier =
                  Modifier.weight(1f).testTag("bottomAppBarButton" + Route.MAP.routeString)) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxHeight()) {
                      Icon(
                          mapIcon,
                          contentDescription =
                              stringResource(id = TOP_LEVEL_DESTINATIONS[1].textId),
                      )
                      Text(
                          text = stringResource(id = R.string.map),
                          style = MaterialTheme.typography.bodySmall)
                    }
              }
        }

        IconButton(
            onClick = { navActions.navigateToTopLevel(TOP_LEVEL_DESTINATIONS[2].route) },
            modifier =
                Modifier.weight(1f).testTag("bottomAppBarButton" + Route.PROFILE.routeString)) {
              Column(
                  verticalArrangement = Arrangement.Center,
                  horizontalAlignment = Alignment.CenterHorizontally,
                  modifier = Modifier.fillMaxHeight()) {
                    Icon(
                        profileIcon,
                        contentDescription = stringResource(id = TOP_LEVEL_DESTINATIONS[2].textId),
                    )
                    Text(
                        text = stringResource(id = R.string.profile),
                        style = MaterialTheme.typography.bodySmall)
                  }
            }
      }
}
