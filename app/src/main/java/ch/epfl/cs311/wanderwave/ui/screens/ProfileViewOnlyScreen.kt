package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.components.profile.ClickableIcon
import ch.epfl.cs311.wanderwave.ui.components.profile.SongsListDisplay
import ch.epfl.cs311.wanderwave.ui.components.profile.VisitCard
import ch.epfl.cs311.wanderwave.ui.components.utils.LoadingScreen
import ch.epfl.cs311.wanderwave.viewmodel.ProfileViewModel


/**
 * This is the screen composable which can only show the profile of the user. It includes a visit
 * card and a list of songs. This screen is not modifiable.
 *
 * @param profileId the id of the profile to display
 * @param navigationActions the navigation actions
 * @param viewModel the view model of the profile
 *
 */
@Composable
fun ProfileViewOnlyScreen(
    profileId: String,
    navigationActions: NavigationActions,
    viewModel: ProfileViewModel = hiltViewModel()
) {

  LaunchedEffect(profileId) { viewModel.getProfileByID(profileId, false) }

  val uiState = viewModel.uiState.collectAsState().value

  ClickableIcon(icon = Icons.Default.ArrowBack, onClick = { navigationActions?.goBack() })
  if (uiState.isLoading) {
    LoadingScreen()
  } else {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).testTag("profileScreen"),
        horizontalAlignment = Alignment.CenterHorizontally) {
          Box(modifier = Modifier.fillMaxWidth()) {
            VisitCard(Modifier, uiState.profile!!)
            // to #127
          }

          SongsListDisplay(
              navigationActions = navigationActions,
              uiState.profile!!.topSongs,
              {},
              onSelectTrack = { viewModel.selectTrack(it) },
              canAddSong = false,
              profileViewModel = viewModel,
          )
        }
  }
}
