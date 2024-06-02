package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ch.epfl.cs311.wanderwave.R
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.data.viewModelType
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.navigation.Route
import ch.epfl.cs311.wanderwave.ui.components.profile.ClickableIcon
import ch.epfl.cs311.wanderwave.ui.components.profile.SelectImage
import ch.epfl.cs311.wanderwave.ui.components.profile.SongsListDisplay
import ch.epfl.cs311.wanderwave.ui.components.profile.VisitCard
import ch.epfl.cs311.wanderwave.viewmodel.ProfileViewModel

const val SCALE_X = 0.5f
const val SCALE_Y = 0.5f
const val MAX_NBR_CHAR_NAMES = 12
const val MAX_NBR_CHAR_DESC = 35
val INPUT_BOX_NAM_SIZE = 150.dp

/**
 * This is the main screen of the profile, it displays the user's profile picture, name, description
 * and top songs. It also contains buttons to sign out and access the about screen.
 *
 * @param navActions to navigate to and from the profile editor
 * @param viewModel to get the profile
 * @param online to determine if the user is online or not
 */
@Composable
fun ProfileScreen(navActions: NavigationActions, viewModel: ProfileViewModel, online: Boolean) {
  val currentProfileState by viewModel.profile.collectAsState()
  val profile by viewModel.profile.collectAsState()

  val currentProfile: Profile = currentProfileState
  LaunchedEffect(Unit) { viewModel.getProfileOfCurrentUser(true) }

  Column(
      modifier = Modifier.fillMaxSize().padding(16.dp).testTag("profileScreen"),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
          SignOutButton(modifier = Modifier, navActions = navActions)
          AboutButton(modifier = Modifier, navActions = navActions)
        }
        Box(modifier = Modifier.fillMaxWidth()) {
          VisitCard(Modifier, currentProfile)
          ProfileSwitch(Modifier.align(Alignment.TopEnd), viewModel)
          if (online) {
            ClickableIcon(
                Modifier.align(Alignment.BottomEnd),
                Icons.Filled.Create,
                onClick = { navActions.navigateTo(Route.EDIT_PROFILE) })
          }
        }

        SongsListDisplay(
            navigationActions = navActions,
            songLists = profile.topSongs,
            onAddTrack = { track -> viewModel.addTrackToList(track) },
            onSelectTrack = { track -> viewModel.selectTrack(track) },
            viewModelName = viewModelType.PROFILE,
            profileViewModel = viewModel)
      }
}
/**
 * This handle the logic behind the switch that can permit the user to switch to the anonymous mode
 *
 * @param modifier to place the switch at a place, and still be able to modify it.
 * @param viewModel to get the profile
 */
@Composable
fun ProfileSwitch(modifier: Modifier = Modifier, viewModel: ProfileViewModel = hiltViewModel()) {
  // Determine the current public mode state
  val isPublicMode by viewModel.isInPublicMode.collectAsState(false)
  Switch(
      checked = isPublicMode,
      onCheckedChange = {
        // When the switch is toggled, call viewModel's method to update the profile's public mode
        viewModel.togglePublicMode()
      },
      modifier =
          modifier
              .graphicsLayer {
                scaleX = SCALE_X
                scaleY = SCALE_Y
              }
              .testTag("profileSwitch"),
      colors =
          SwitchDefaults.colors(
              checkedThumbColor = MaterialTheme.colorScheme.primary,
              checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
              uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
              uncheckedTrackColor = MaterialTheme.colorScheme.secondaryContainer,
          ),
  )
}

/**
 * Creates a button using the user's profile picture to access their profile
 *
 * @param modifier to apply any needed modifiers to the button
 * @param viewModel to get the profile
 * @param navActions to navigate to and from the profile editor
 * @author Imade Bouhamria
 */
@Composable
fun ProfileButton(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
    navActions: NavigationActions
) {
  val currentProfileState by viewModel.profile.collectAsState()
  val currentProfile: Profile = currentProfileState

  // Display the button only when on the main screen TODO: Also from Map ?
  Box(
      modifier =
          modifier
              .clickable { navActions.navigateTo(Route.PROFILE) }
              .background(Color.Transparent)
              .padding(16.dp)
              .testTag("profileButton")) {
        if (navActions.getCurrentRoute() == Route.MAIN) {
          SelectImage(
              modifier = Modifier.clip(CircleShape).size(50.dp),
              imageUri = currentProfile.profilePictureUri,
              profile = currentProfile)
        }
      }
}

/**
 * This is the sign out button that will allow the user to sign out
 *
 * @param modifier the modifier to be applied to the Button
 * @param navActions to be able to navigate back to the login screen
 * @author Imade Bouhamria
 */
@Composable
fun SignOutButton(modifier: Modifier, navActions: NavigationActions) {
  // TODO: Implement actual user sign out
  Button(
      onClick = { navActions.navigateToTopLevel(Route.LOGIN) },
      modifier = modifier.testTag("signOutButton"),
      colors =
          ButtonColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
              contentColor = MaterialTheme.colorScheme.onSurface,
              disabledContainerColor = Color.Gray,
              disabledContentColor = MaterialTheme.colorScheme.primary),
      shape = RoundedCornerShape(size = 50.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
              Icon(
                  painter = painterResource(id = R.drawable.logout_icon),
                  contentDescription = "logout")
              Spacer(modifier = Modifier.width(5.dp))
              Text(text = stringResource(id = R.string.signOut))
            }
      }
}

@Composable
fun AboutButton(modifier: Modifier, navActions: NavigationActions) {
  Button(
      onClick = { navActions.navigateTo(Route.ABOUT) },
      modifier = modifier.testTag("aboutButton"),
      colors =
          ButtonColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
              contentColor = MaterialTheme.colorScheme.onSurface,
              disabledContainerColor = Color.Gray,
              disabledContentColor = MaterialTheme.colorScheme.primary),
      shape = RoundedCornerShape(size = 50.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
              Icon(
                  painter = painterResource(id = R.drawable.info_icon), contentDescription = "info")
              Spacer(modifier = Modifier.width(5.dp))
              Text(text = stringResource(id = R.string.about))
            }
      }
}
