package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.epfl.cs311.wanderwave.R
import ch.epfl.cs311.wanderwave.model.data.viewModelType
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.components.tracklist.TrackList
import ch.epfl.cs311.wanderwave.viewmodel.TrackListViewModel

@Composable
fun TrackListScreen(
    navActions: NavigationActions,
    showMessage: (String) -> Unit,
    viewModel: TrackListViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var searchQuery by remember { mutableStateOf("") }

  LaunchedEffect(Unit) { viewModel.updateBannedSongs() }

  Column(modifier = Modifier.testTag("trackListScreen")) {
    TextField(
        value = searchQuery,
        onValueChange = { query ->
          searchQuery = query
          viewModel.setSearchQuery(query)
        },
        label = { Text(stringResource(R.string.search_tracks)) }, // Use string resource
        modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("searchBar"))

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
      Text(stringResource(R.string.show_recently_added)) // Use string resource
      Switch(
          checked = uiState.showRecentlyAdded,
          onCheckedChange = { viewModel.toggleTrackSource() },
          colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.secondary))
    }

    TrackList(
        tracks =
            uiState.tracks.filter { track -> uiState.bannedTracks.any { it.id == track.id }.not() },
        title =
            if (uiState.showRecentlyAdded) stringResource(R.string.recently_added_tracks)
            else stringResource(R.string.recently_viewed_tracks),
        onAddTrack = {},
        onSelectTrack = viewModel::playTrack,
        navActions = navActions,
        viewModelName = viewModelType.TRACKLIST)
  }
}
