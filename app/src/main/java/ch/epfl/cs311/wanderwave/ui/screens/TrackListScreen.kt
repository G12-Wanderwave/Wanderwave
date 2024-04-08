package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.epfl.cs311.wanderwave.ui.components.tracklist.TrackListItem
import ch.epfl.cs311.wanderwave.viewmodel.TrackListViewModel

@Composable
fun TrackListScreen(
    showMessage: (String) -> Unit,
    viewModel: TrackListViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  LaunchedEffect(uiState) { uiState.message?.let { message -> showMessage(message) } }

  Surface(modifier = Modifier.fillMaxSize()) {
    LazyColumn(modifier = Modifier.testTag("trackListScreen")) {
      items(uiState.tracks.size) { index ->
        val track = uiState.tracks[index]

        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
          TrackListItem(
              track = track,
              selected = track == uiState.selectedTrack,
              onClick = {viewModel.playTrack(track)})

          Divider()
        }
      }
    }
  }
}
