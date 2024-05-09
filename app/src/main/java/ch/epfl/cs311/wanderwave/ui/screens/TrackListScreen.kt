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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.epfl.cs311.wanderwave.ui.components.tracklist.TrackList
import ch.epfl.cs311.wanderwave.viewmodel.TrackListViewModel

@Composable
fun TrackListScreen(
    showMessage: (String) -> Unit,
    viewModel: TrackListViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var searchQuery by remember { mutableStateOf("") }

  LaunchedEffect(uiState.message) { uiState.message?.let { message -> showMessage(message) } }

  Column(modifier = Modifier.testTag("trackListScreen")) {
    TextField(
        value = searchQuery,
        onValueChange = { query ->
          searchQuery = query
          viewModel.setSearchQuery(query)
        },
        label = { Text("Search Tracks") },
        modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("searchBar"))

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
      Text("Show Recently Added")
      Switch(
          checked = uiState.showRecentlyAdded,
          onCheckedChange = { viewModel.toggleTrackSource() },
          colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.secondary))
    }

    TrackList(
        tracks = uiState.tracks,
        title =
            if (uiState.showRecentlyAdded) "Recently Added Tracks" else "Recently Viewed Tracks",
        onAddTrack = {},
        onSelectTrack = {})
  }
}
