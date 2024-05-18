package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.epfl.cs311.wanderwave.model.data.viewModelType
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.components.tracklist.TrackList
import ch.epfl.cs311.wanderwave.viewmodel.TrackListViewModel

@Composable
fun TrackListScreen(
    navActions: NavigationActions,
    viewModel: TrackListViewModel = hiltViewModel()
) {

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Recently listened", "Liked songs", "Banned songs")

    Column {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(text = title, fontSize =10.sp ) }
                )
            }
        }
            TabContent1(navActions,viewModel,selectedTabIndex)
        }
    }

@Composable
fun TabContent1(
    navActions: NavigationActions,
    viewModel: TrackListViewModel = hiltViewModel(),
    selectedTabIndex: Int) {
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
        TrackList(
            when (selectedTabIndex) {
                0 -> uiState.recentlyListenedTracks
                1 -> uiState.tracks
                2 -> uiState.bannedTracks
                else -> uiState.recentlyListenedTracks
            },
            title =  when (selectedTabIndex) {
                0 -> "Recently listened songs"
                1 -> "Liked songs"
                2 -> "Banned songs"
                else -> "Recently listened songs"
            },
            onAddTrack = {},
            onSelectTrack = viewModel::playTrack,
            navActions = navActions,
            viewModelName = viewModelType.TRACKLIST)
    }
}