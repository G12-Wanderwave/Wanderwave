package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.navigation.Route
import ch.epfl.cs311.wanderwave.ui.components.profile.AddTrackDialog
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
 * This is the screen composable which can either show the profile of the user or it can show a view
 * to modify the profile. It also includes a toggle to switch between showing the "TOP SONGS" list
 * or the "CHOSEN SONGS" list, as well as dialogs to add new tracks to the lists.
 *
 * @author Ayman Bakiri
 * @author Menzo Bouaissi
 * @since 1.0
 * @last update 2.0
 */
@Composable
fun ProfileScreen(navActions: NavigationActions, viewModel: ProfileViewModel) {
  val currentProfileState by viewModel.profile.collectAsState()

  val currentProfile: Profile = currentProfileState ?: return
  LaunchedEffect(Unit) {
    viewModel.createSpecificSongList("TOP_SONGS")
    viewModel.createSpecificSongList("CHOSEN_SONGS")
  }

  Column(
      modifier = Modifier.fillMaxSize().padding(16.dp).testTag("profileScreen"),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                ClickableIcon(
                    icon = Icons.Filled.ArrowBack,
                    onClick = { navActions.navigateTo(Route.MAIN) }
                )
                Spacer(modifier = Modifier.weight(1f))
                ClickableIcon(
                    modifier = Modifier.testTag("signOutButton"),
                    icon = Icons.Filled.ExitToApp,
                    onClick = { navActions.navigateToTopLevel(Route.LOGIN) }
                    // TODO: Implement actual user sign out

                )
          } // Spacer to create space between Box and VisitCard
          VisitCard(Modifier, currentProfile)
          ProfileSwitch(Modifier.align(Alignment.TopEnd).padding(top = 20.dp), viewModel)
          ClickableIcon(
              Modifier.align(Alignment.BottomEnd),
              Icons.Filled.Create,
              onClick = { navActions.navigateTo(Route.EDIT_PROFILE) })
        }
        showSongList(navActions, viewModel)
      }

  SignOutButton(modifier = Modifier, navActions = navActions)
}

/**
 * This shows the list of songs that the user has chosen to be part of their profile. It also
 * includes buttons to add new tracks to the list.
 *
 * @author Ayman Bakiri
 * @author Menzo Bouaissi
 * @since 1.0
 * @last update 2.0
 */
@Composable
fun showSongList(navActions: NavigationActions, viewModel: ProfileViewModel) {
  val songLists by viewModel.songLists.collectAsState()
  var showDialog by remember { mutableStateOf(false) }
  var dialogListType by remember { mutableStateOf("TOP SONGS") }
  var isTopSongsListVisible by remember { mutableStateOf(true) }
  Button(
      onClick = { isTopSongsListVisible = !isTopSongsListVisible },
      modifier = Modifier.testTag("toggleSongList")) {
        Text(if (isTopSongsListVisible) "Show CHOSEN SONGS" else "Show TOP SONGS")
      }

  // Call the SongsListDisplay function
  // Buttons for adding tracks to top songs lists
  if (isTopSongsListVisible) {
    Button(
        onClick = { navActions.navigateTo(Route.SELECT_SONG) },
        modifier = Modifier.testTag("addTopSongs")) {
          Text("Add Track to TOP SONGS List")
        }
  } else {
    Button(
        onClick = {
          showDialog = true
          dialogListType = "CHOSEN SONGS"
        },
        modifier = Modifier.testTag("addChosenSongs")) {
          Text("Add Track to CHOSEN SONGS List")
        }
  }
  SongsListDisplay(songLists = songLists, isTopSongsListVisible = isTopSongsListVisible)
  // Buttons for adding tracks to chosen songs list

  // Show dialog for adding a new track and add the track to the appropriate list
  if (showDialog) {
    AddTrackDialog(
        onAddTrack = { id, title, artist ->
          viewModel.createSpecificSongList(dialogListType) // Ensure the list is created
          viewModel.addTrackToList(dialogListType, Track(id, title, artist))
          showDialog = false
        },
        onDismiss = { showDialog = false },
        initialTrackId = "",
        initialTrackTitle = "",
        initialTrackArtist = "",
        dialogTestTag = "addTrackDialog")
  }
}
/**
 * This handle the logic behind the switch that can permit the user to switch to the anonymous mode
 *
 * @param modifier to place the switch at a place, and still be able to modify it.
 * @author Menzo Bouaissi
 * @since 1.0
 * @last update 1.0
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
          SelectImage(modifier = Modifier.clip(CircleShape).size(50.dp), profile = currentProfile)
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
      modifier = modifier.testTag("signOutButton")) {
        Text(text = "Sign Out")
      }
}
