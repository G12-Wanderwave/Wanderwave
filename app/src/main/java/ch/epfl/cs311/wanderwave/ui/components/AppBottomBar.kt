
package ch.epfl.cs311.wanderwave.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.navigation.TOP_LEVEL_DESTINATIONS


val trackListIcon: ImageVector = Icons.Default.List
val mainIcon: ImageVector = Icons.Default.Home
val mapIcon: ImageVector = Icons.Default.LocationOn

@Composable
fun AppBottomBar(navActions: NavigationActions) {
  BottomAppBar(
    modifier = Modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surface),
  ) {
    // Assumes TOP_LEVEL_DESTINATIONS are in the order of Track List, Main, and Map for indexing
    IconButton(
      onClick = { navActions.navigateToTopLevel(TOP_LEVEL_DESTINATIONS[0].route) },
      modifier = Modifier
        .weight(1f) // Spread the icons evenly across the BottomAppBar
        .testTag("bottomAppBarIconTrackList")
    ) {
      Icon(trackListIcon, contentDescription = stringResource(id = TOP_LEVEL_DESTINATIONS[0].textId))
    }

    Spacer(Modifier.weight(1f)) // Spacer for centering the middle icon

    IconButton(
      onClick = { navActions.navigateToTopLevel(TOP_LEVEL_DESTINATIONS[1].route) },
      modifier = Modifier
        .weight(1f)
        .testTag("bottomAppBarIconMain")
    ) {
      Icon(mainIcon, contentDescription = stringResource(id = TOP_LEVEL_DESTINATIONS[1].textId))
    }

    Spacer(Modifier.weight(1f)) // Spacer for centering the middle icon

    IconButton(
      onClick = { navActions.navigateToTopLevel(TOP_LEVEL_DESTINATIONS[2].route) },
      modifier = Modifier
        .weight(1f)
        .testTag("bottomAppBarIconMap")
    ) {
      Icon(mapIcon, contentDescription = stringResource(id = TOP_LEVEL_DESTINATIONS[2].textId))
    }
  }
}
