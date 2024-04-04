package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.epfl.cs311.wanderwave.ui.components.tracklist.TrackListItem
import ch.epfl.cs311.wanderwave.viewmodel.TrackListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackListScreen() {
  val viewModel: TrackListViewModel = hiltViewModel()
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  LazyColumn(modifier = Modifier.testTag("trackListScreen")) {
    items(uiState.tracks.size) { index ->
      val track = uiState.tracks[index]

      Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
        TrackListItem(track = track, selected = track == uiState.selectedTrack) {}

        Divider()
      }
    }
  }
}
