package ch.epfl.cs311.wanderwave.ui.components

import android.util.Log
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import ch.epfl.cs311.wanderwave.ui.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.navigation.Route
import ch.epfl.cs311.wanderwave.ui.navigation.TOP_LEVEL_DESTINATIONS
import ch.epfl.cs311.wanderwave.viewmodel.ProfileViewModel


/**
 * Handle the logic behind the edit button
 *
 * @param modifier the modifier to be applied to the Icon
 * @param icon the icon to be display
 * @param viewModel the viewModel that will handle the profile
 * @author Menzo Bouaissi
 * @since 1.0
 * @last update 1.0
 */
@Composable
fun ClickableIcon(
    modifier: Modifier,
    icon: ImageVector,
    navController: NavigationActions
) {

    IconButton(
        modifier = modifier.then(Modifier.size(24.dp))
            .testTag("clickableIcon"),
        onClick = {
            navController.navigateTo(TOP_LEVEL_DESTINATIONS[3])//TODO: CHANGE THIS

        }) {
        Icon(
            icon,
            contentDescription = "Edit",
            modifier = modifier
        )
    }
}