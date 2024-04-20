package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.components.profile.TrackItem
import ch.epfl.cs311.wanderwave.viewmodel.ProfileViewModel
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
fun SelectSongScreen(navActions: NavigationActions, viewModel: ProfileViewModel) {
  val mainList by viewModel.spotifySubsectionList.collectAsState()
  val childrenPlaylistTrackList by viewModel.childrenPlaylistTrackList.collectAsState()

  var displayedList by remember { mutableStateOf(mainList) }

  LaunchedEffect(Unit) { viewModel.retrieveAndAddSubsection() }
  LaunchedEffect(mainList) { displayedList = mainList }

  LaunchedEffect(childrenPlaylistTrackList) { displayedList = childrenPlaylistTrackList }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Select Song") },
            navigationIcon = {
              IconButton(onClick = { navActions.goBack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Go back")
              }
            })
      }) { innerPadding -> // This is the padding parameter you need to use
        LazyColumn(
            contentPadding = innerPadding, // Apply the innerPadding to the LazyColumn
            modifier =
                Modifier.padding(
                    all = 16.dp) // Additional padding can still be applied here if needed
            ) {
              items(displayedList, key = { it.id }) { listItem ->
                TrackItem(
                    listItem,
                    onClick = {
                      if (listItem.hasChildren) {
                        viewModel.retrieveChild(listItem)
                      } else {
                        viewModel.addTrackToList(
                            "TOP SONGS", Track(listItem.id, listItem.title, listItem.subtitle))
                        navActions.goBack()
                      }
                    })
              }
            }
      }
}

