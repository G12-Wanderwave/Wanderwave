package ch.epfl.cs311.wanderwave.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.epfl.cs311.wanderwave.R
import ch.epfl.cs311.wanderwave.model.data.viewModelType
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.components.tracklist.RemovableTrackList
import ch.epfl.cs311.wanderwave.ui.components.tracklist.TrackList
import ch.epfl.cs311.wanderwave.viewmodel.ProfileViewModel
import ch.epfl.cs311.wanderwave.viewmodel.TrackListViewModel

/**
 * This function is responsible for displaying the TrackListScreen. It displays the list of tracks
 * based on the selected tab. The user can search for tracks and add them to the list. The user can
 * also play the tracks.
 *
 * @param navActions: NavigationActions
 * @param viewModel: TrackListViewModel
 * @param profileViewModel: ProfileViewModel
 * @param online: Boolean
 */
@Composable
fun TrackListScreen(
    navActions: NavigationActions,
    viewModel: TrackListViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel,
    online: Boolean
) {

  var selectedTabIndex by remember { mutableIntStateOf(0) }
  val tabs =
      listOf(
          stringResource(R.string.recently_played_tracks),
          stringResource(R.string.recently_added_tracks),
          stringResource(R.string.liked_tracks),
          stringResource(R.string.banned_tracks))
  LaunchedEffect(Unit) { viewModel.loadTracksBasedOnSource(selectedTabIndex) }
  Column {
    TabRow(selectedTabIndex = selectedTabIndex, modifier = Modifier.fillMaxWidth()) {
      tabs.forEachIndexed { index, title ->
        Tab(
            selected = selectedTabIndex == index,
            onClick = {
              selectedTabIndex = index
              viewModel.loadTracksBasedOnSource(selectedTabIndex)
            },
            text = { Text(text = title, fontSize = 10.sp) },
            modifier = Modifier.testTag("tab$index") // Adding a test tag for the tabs
            )
      }
    }
    TabContent1(navActions, viewModel, profileViewModel, selectedTabIndex)
  }
}
/**
 * This function is responsible for displaying the content of the tabs in the TrackListScreen.
 *
 * @param navActions: NavigationActions
 * @param viewModel: TrackListViewModel
 * @param selectedTabIndex: Int
 */
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun TabContent1(
    navActions: NavigationActions,
    viewModel: TrackListViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel,
    selectedTabIndex: Int
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var searchQuery by remember { mutableStateOf("") }
  LaunchedEffect(Unit) { viewModel.updateBannedAndRetrievedSongsSongs() }

  Column(modifier = Modifier.testTag("trackListScreen")) {
    TextField(
        value = searchQuery,
        onValueChange = { query ->
          searchQuery = query
          viewModel.setSearchQuery(query)
        },
        label = { Text("Search Tracks") },
        modifier =
            Modifier.fillMaxWidth()
                .padding(16.dp)
                .testTag("searchBar") // Adding a test tag for the search bar
        )
    when (selectedTabIndex) {
      0 -> {
        TrackList(
            tracks =
                uiState.tracks.filter { track ->
                  uiState.bannedTracks.any { it.id == track.id }.not()
                },
            title = stringResource(R.string.recently_played_tracks),
            canAddSong = false,
            onAddTrack = { navActions.navigateToSelectSongScreen(viewModelType.TRACKLIST) },
            onSelectTrack = viewModel::playTrack,
            navActions = navActions,
            viewModelName = viewModelType.TRACKLIST,
            profileViewModel = profileViewModel,
            canLike = true)
      }
      1 -> {
        Log.d("TrackListScreen", "Chosen songs: ${uiState.retrievedTrack}")
        TrackList(
            tracks =
                uiState.retrievedTrack.filter { track ->
                  uiState.bannedTracks.any { it.id == track.id }.not()
                },
            title = stringResource(R.string.recently_added_tracks),
            canAddSong = false,
            onAddTrack = { navActions.navigateToSelectSongScreen(viewModelType.TRACKLIST) },
            onSelectTrack = viewModel::playTrack,
            navActions = navActions,
            viewModelName = viewModelType.TRACKLIST,
            profileViewModel = profileViewModel,
            canLike = true)
      }
      2 -> {
        TrackList(
            tracks =
                profileViewModel.wanderwaveLikedTracks.value.filter { track ->
                  uiState.bannedTracks.any { it.id == track.id }.not()
                },
            title = stringResource(R.string.liked_tracks),
            onAddTrack = { navActions.navigateToSelectSongScreen(viewModelType.TRACKLIST) },
            onSelectTrack = viewModel::playTrack,
            navActions = navActions,
            viewModelName = viewModelType.TRACKLIST,
            profileViewModel = profileViewModel)
      }
      3 -> {
        RemovableTrackList(
            tracks = uiState.retrievedTrack,
            title = stringResource(R.string.banned_tracks),
            onAddTrack = { navActions.navigateToSelectSongScreen(viewModelType.TRACKLIST) },
            onSelectTrack = viewModel::playTrack,
            onRemoveTrack = viewModel::removeTrackFromBanList,
            profileViewModel = profileViewModel)
      }
    }
  }
}
