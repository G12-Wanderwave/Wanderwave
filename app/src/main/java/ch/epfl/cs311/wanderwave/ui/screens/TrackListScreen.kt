package ch.epfl.cs311.wanderwave.ui.screens

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
import ch.epfl.cs311.wanderwave.viewmodel.TrackListViewModel

@Composable
fun TrackListScreen(
    navActions: NavigationActions,
    viewModel: TrackListViewModel = hiltViewModel()
) {

  var selectedTabIndex by remember { mutableIntStateOf(0) }
  val tabs =
      listOf(
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
    TabContent1(navActions, viewModel, selectedTabIndex)
  }
}
/**
 * This function is responsible for displaying the content of the tabs in the TrackListScreen.
 *
 * @param navActions: NavigationActions
 * @param viewModel: TrackListViewModel
 * @param selectedTabIndex: Int
 * @author Menzo Bouaissi
 * @since 4.0
 */
@Composable
fun TabContent1(
    navActions: NavigationActions,
    viewModel: TrackListViewModel = hiltViewModel(),
    selectedTabIndex: Int
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var searchQuery by remember { mutableStateOf("") }

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
    RemovableTrackList(
        tracks = uiState.tracks,
        title =
            when (selectedTabIndex) {
              0 -> stringResource(R.string.recently_added_tracks)
              1 -> stringResource(R.string.liked_tracks)
              2 -> stringResource(R.string.banned_tracks)
              else -> stringResource(R.string.recently_added_tracks)
            },
        onAddTrack = { navActions.navigateToSelectSongScreen(viewModelType.TRACKLIST) },
        onSelectTrack = viewModel::playTrack,
        // TODO: make more generic, not only for banned tracks
        onRemoveTrack = viewModel::removeTrackFromBanList,
    )
  }
}
