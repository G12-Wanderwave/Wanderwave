package ch.epfl.cs311.wanderwave.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.navigation.Route
import ch.epfl.cs311.wanderwave.ui.components.profile.ClickableIcon
import ch.epfl.cs311.wanderwave.ui.components.profile.SongsListDisplay
import ch.epfl.cs311.wanderwave.ui.components.profile.VisitCard
import ch.epfl.cs311.wanderwave.viewmodel.ProfileViewModel
import ch.epfl.cs311.wanderwave.viewmodel.SongList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * This is the screen composable which can only show the profile of the user. It includes a visit
 * card and a list of songs. This screen is not modifiable.
 *
 * @param profile The profile to display.
 * @author Menzo Bouaissi
 * @since 2.0
 * @last update 2.0
 */
@Composable
fun ProfileViewOnlyScreen(profileId: String, navigationActions: NavigationActions) {

  val viewModel: ProfileViewModel = hiltViewModel()
  LaunchedEffect(profileId) {
    if (profileId != null) {
      viewModel.getProfileByID(profileId)
    } else {
      withContext(Dispatchers.Main) {
        navigationActions.navigateTo(Route.MAIN)
        Log.e("No profile found", "No beacons found for the given id")
      }
    }
  }

  val uiState = viewModel.uiState.collectAsState().value

  Column(
      modifier = Modifier.fillMaxSize().padding(16.dp).testTag("profileScreen"),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.fillMaxWidth()) {
          ClickableIcon(
              modifier = Modifier.align(Alignment.CenterEnd),
              icon = Icons.Default.ArrowBack,
              onClick = { navigationActions?.goBack() })
          VisitCard(Modifier, uiState.profile!!)

          val mockSongLists =
              listOf(
                  SongList(
                      "TOP SONGS",
                      listOf(Track("1", "Track 1", "Artist 1"), Track("2", "Track 2", "Artist 2"))),
                  SongList(
                      "CHOSEN SONGS",
                      listOf(Track("3", "Track 3", "Artist 3"), Track("4", "Track 4", "Artist 4"))))
          // TODO: modify this, because the profile.songLists is not available
          showListSong(
              mockSongLists) // TODO: change to actually recover the profile.songLists, but related
                             // to #127
        }
      }
}

/**
 * Composable that displays a list of tracks. Each track is represented by the TrackItem composable.
 *
 * @param songLists List of song lists to display.
 * @author Menzo Bouaissi
 * @since 2.0
 * @last update 2.0
 */
@Composable
fun showListSong(songLists: List<SongList>) {
  var isTopSongsListVisible by remember { mutableStateOf(true) }
  Button(
      onClick = { isTopSongsListVisible = !isTopSongsListVisible },
      modifier = Modifier.testTag("toggleSongList")) {
        Text(if (isTopSongsListVisible) "Show CHOSEN SONGS" else "Show TOP SONGS")
      }
  SongsListDisplay(songLists = songLists, isTopSongsListVisible = isTopSongsListVisible)
}
