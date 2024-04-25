package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(uiState) { uiState.message?.let { message -> showMessage(message) } }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column {

            TextField(
                value = searchQuery,
                onValueChange = { query ->
                    searchQuery = query
                    viewModel.setSearchQuery(query)
                },
                label = { Text("Search Tracks") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("searchBar") // Adding a test tag for the search bar
            )       
            LazyColumn(modifier = Modifier
                .testTag("trackListScreen")) { // Maintaining the LazyColumn test tag
                items(uiState.tracks.size) { index ->
                    val track = uiState.tracks[index]

                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        TrackListItem(
                            track = track,
                            selected = track == uiState.selectedTrack,
                            onClick = { viewModel.selectTrack(track) })

                        Divider()
                    }
                }
            }
        }
    }
}




