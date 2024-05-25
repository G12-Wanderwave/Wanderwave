package ch.epfl.cs311.wanderwave.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.epfl.cs311.wanderwave.R
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.components.utils.WanderWaveButton
import ch.epfl.cs311.wanderwave.ui.theme.md_theme_dark_error
import ch.epfl.cs311.wanderwave.viewmodel.interfaces.SpotifySongsActions
import com.spotify.protocol.types.ListItem
import okhttp3.internal.ignoreIoExceptions

/**
 * Screen to select a song from Spotify
 *
 * @param navActions Navigation actions
 * @param viewModel ProfileViewModel
 * @author Menzo Bouaissi
 * @since 2.0
 * @last update 2.0
 */
@Composable
fun SelectSongScreen(navActions: NavigationActions, viewModel: SpotifySongsActions) {
  val likedSongsList by viewModel.likedSongsTrackList.collectAsState()
  val nbrLikedSongs by viewModel.nbrLikedSongs.collectAsState()

  initSongScreen(viewModel)

  val displayedList = likedSongsList

  SongScreenScaffold(navActions, displayedList, viewModel,nbrLikedSongs)
}

/**
 * Initialize the song screen
 *
 * @param viewModel ProfileViewModel
 * @author Menzo Bouaissi
 * @since 3.0
 * @last update 3.0
 */
@Composable
fun initSongScreen(viewModel: SpotifySongsActions) {
  LaunchedEffect(Unit) {
      viewModel.clearLikedSongs()
      viewModel.getLikedTracks()
        viewModel.getTotalLikedTracks()
  }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongScreenScaffold(
    navActions: NavigationActions,
    displayedList: List<ListItem>,
    viewModel: SpotifySongsActions,
    nbrLikedSongs: Int
) {
    // Manage the page state
    var page by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Song") },
                navigationIcon = {
                    IconButton(onClick = { navActions.goBack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Go back")
                    }
                })
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp), // Consistent padding on both sides
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(modifier = Modifier.weight(1f, false)) {
                    WanderWaveButton(
                        id = R.string.previous,
                        onClick = { if (page > 0) page -= 1 },
                        modifier = Modifier.fillMaxWidth(),
                        textColor = md_theme_dark_error
                    )
                }
                Spacer(modifier = Modifier.width(8.dp)) // Adjust this value to reduce space between buttons
                Box(modifier = Modifier.weight(1f, false)) {
                    WanderWaveButton(
                        id = R.string.next,
                        onClick = {
                            if ((page + 1) * 50 < nbrLikedSongs) {
                                Log.d("SelectSongScreen", "Next")
                                page += 1
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textColor = md_theme_dark_error
                    )
                }
            }
        }) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            SongList(
                paddingValues = innerPadding,
                items = displayedList,
                navActions = navActions,
                viewModel = viewModel
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Load the next songs based on the current page
    loadNextSongs(viewModel = viewModel, page = page)
}

@Composable
fun loadNextSongs(viewModel: SpotifySongsActions, page: Int) {
    LaunchedEffect(page) {
        viewModel.getLikedTracks(page)
    }
}
@Composable
fun TrackItem(listItem: ListItem, onClick: () -> Unit) {
  Card(
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
              contentColor = MaterialTheme.colorScheme.onSurface,
              disabledContainerColor = MaterialTheme.colorScheme.onSurfaceVariant,
              disabledContentColor = MaterialTheme.colorScheme.error // Example color
              ),
      modifier = Modifier
          .height(80.dp)
          .fillMaxWidth()
          .padding(4.dp)
          .clickable(onClick = onClick)) {
        Row {
          Column(modifier = Modifier.padding(8.dp)) {
            Text(text = listItem.title, style = MaterialTheme.typography.titleMedium)
            Text(text = listItem.subtitle, style = MaterialTheme.typography.bodyMedium)
          }
        }
      }
}

/**
 * List of songs
 *
 * @param paddingValues Padding values
 * @param items List of ListItem
 * @param navActions Navigation actions
 * @param viewModel ProfileViewModel
 * @param isTopSongsListVisible Boolean indicating if the top songs list is visible
 * @author Menzo Bouaissi
 * @since 3.0
 * @last update 3.0
 */
@Composable
fun SongList(
    paddingValues: PaddingValues,
    items: List<ListItem>,
    navActions: NavigationActions,
    viewModel: SpotifySongsActions,
) {
  LazyColumn(contentPadding = paddingValues, modifier = Modifier.padding(bottom = 63.dp)) {
    items(items, key = { it.id }) { item ->
      TrackItem(item, onClick = { handleItemClick(item, navActions, viewModel) })
    }
  }
}

/**
 * Handle item click
 *
 * @param listItem ListItem
 * @param navActions Navigation actions
 * @param viewModel ProfileViewModel
 * @author Menzo Bouaissi
 * @since 3.0
 * @last update 3.0
 */
fun handleItemClick(
    listItem: ListItem,
    navActions: NavigationActions,
    viewModel: SpotifySongsActions
) {

  viewModel.addTrackToList(Track(listItem.id, listItem.title, listItem.subtitle))
  navActions.goBack()
}
