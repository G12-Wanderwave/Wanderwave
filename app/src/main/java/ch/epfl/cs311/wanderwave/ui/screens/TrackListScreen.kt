package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.epfl.cs311.wanderwave.ui.components.ScreenScaffold
import ch.epfl.cs311.wanderwave.ui.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.viewmodel.TrackListViewModel

@Composable
fun TrackListScreen(navigationActions: NavigationActions) {
  val viewModel: TrackListViewModel = hiltViewModel()
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  ScreenScaffold(navActions = navigationActions) {
    LazyColumn(modifier = Modifier.testTag("trackListScreen")) {
      items(uiState.tracks.size) { index ->
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
          Text(text = uiState.tracks[index].title)
          Divider()
        }
      }
    }
  }
}
