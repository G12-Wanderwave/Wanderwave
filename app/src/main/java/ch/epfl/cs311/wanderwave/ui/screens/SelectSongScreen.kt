package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.epfl.cs311.wanderwave.model.data.ListType
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.components.profile.TrackItem
import ch.epfl.cs311.wanderwave.viewmodel.interfaces.SpotifySongsActions
import com.spotify.protocol.types.ListItem

/**
 * Screen to select a song from Spotify
 *
 * @param navActions Navigation actions
 * @param viewModel ProfileViewModel
 * @author Menzo Bouaissi
 * @since 2.0
 * @last update 2.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectSongScreen(navActions: NavigationActions, viewModel: SpotifySongsActions) {
  val subsectionList by viewModel.spotifySubsectionList.collectAsState()
  val likedSongsList by viewModel.likedSongsTrackList.collectAsState()
  val childrenPlaylistTrackList by viewModel.childrenPlaylistTrackList.collectAsState()
  // Conditionally display the list based on isChosenSongs state
  var displayedList by remember { mutableStateOf(emptyList<ListItem>()) }

  val isTopSongsListVisible by viewModel.isTopSongsListVisible.collectAsState(false)

  LaunchedEffect(Unit) {
    viewModel.retrieveAndAddSubsection()
    viewModel.getLikedTracks()
  }

  if (isTopSongsListVisible) {
    LaunchedEffect(subsectionList) { displayedList = subsectionList }
    LaunchedEffect(childrenPlaylistTrackList) { displayedList = childrenPlaylistTrackList }
  } else {
    LaunchedEffect(likedSongsList) { displayedList = likedSongsList }
  }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Select Song") },
            navigationIcon = {
              IconButton(onClick = { navActions.goBack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Go back")
              }
            })
      }) { innerPadding ->
        LazyColumn(contentPadding = innerPadding, modifier = Modifier.padding(all = 16.dp)) {
          items(displayedList, key = { it.id }) { listItem ->
            TrackItem(
                listItem,
                onClick = {
                  if (listItem.hasChildren) {
                    viewModel.retrieveChild(listItem)
                  } else {
                    viewModel.addTrackToList(
                        isTopSongsListVisible?.let {
                          if (it) ListType.TOP_SONGS else ListType.LIKED_SONGS
                        } ?: ListType.TOP_SONGS,
                        Track(listItem.id, listItem.title, listItem.subtitle))
                    navActions.goBack()
                  }
                })
          }
        }
      }
}
