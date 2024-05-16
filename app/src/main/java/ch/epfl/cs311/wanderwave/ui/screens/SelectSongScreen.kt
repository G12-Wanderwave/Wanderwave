package ch.epfl.cs311.wanderwave.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.epfl.cs311.wanderwave.model.data.ListType
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.components.profile.TrackItem
import ch.epfl.cs311.wanderwave.viewmodel.interfaces.SpotifySongsActions
import com.spotify.protocol.types.ListItem
import kotlinx.coroutines.flow.MutableStateFlow

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
  val isTopSongsListVisible by viewModel.isTopSongsListVisible.collectAsState(false)

  initSongScreen(viewModel)

  val displayedList =
      determineDisplayedList(
          isTopSongsListVisible, subsectionList, likedSongsList, childrenPlaylistTrackList)

  SongScreenScaffold(navActions, displayedList, viewModel, isTopSongsListVisible)
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
    viewModel.retrieveAndAddSubsection()
    viewModel.getLikedTracks()
  }
}

/**
 * Determine which list to display
 *
 * @param isTopSongsListVisible Boolean indicating if the top songs list is visible
 * @param subsectionList List of subsections
 * @param likedSongsList List of liked songs
 * @param childrenPlaylistTrackList List of children playlist tracks
 * @return List of ListItem to display
 * @author Menzo Bouaissi
 * @since 3.0
 * @last update 3.0
 */
@Composable
fun determineDisplayedList(
    isTopSongsListVisible: Boolean,
    subsectionList: List<ListItem>,
    likedSongsList: List<ListItem>,
    childrenPlaylistTrackList: List<ListItem>
): List<ListItem> {
  return when {
    isTopSongsListVisible ->
        if (childrenPlaylistTrackList.isNotEmpty()) childrenPlaylistTrackList else subsectionList
    else -> likedSongsList
  }
}

/**
 * Scaffold for the song screen
 *
 * @param navActions Navigation actions
 * @param displayedList List of ListItem to display
 * @param viewModel ProfileViewModel
 * @param isTopSongsListVisible Boolean indicating if the top songs list is visible
 * @author Menzo Bouaissi
 * @since 3.0
 * @last update 3.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongScreenScaffold(
    navActions: NavigationActions,
    displayedList: List<ListItem>,
    viewModel: SpotifySongsActions,
    isTopSongsListVisible: Boolean
) {
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
        SongList(innerPadding, displayedList, navActions, viewModel, isTopSongsListVisible)
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
    isTopSongsListVisible: Boolean
) {
  LazyColumn(contentPadding = paddingValues, modifier = Modifier.padding(all = 16.dp)) {
    items(items, key = { it.id }) { item ->
      TrackItem(
          item, onClick = { handleItemClick(item, navActions, viewModel, isTopSongsListVisible) })
    }
  }
}

/**
 * Handle item click
 *
 * @param listItem ListItem
 * @param navActions Navigation actions
 * @param viewModel ProfileViewModel
 * @param isTopSongsListVisible Boolean indicating if the top songs list is visible
 * @author Menzo Bouaissi
 * @since 3.0
 * @last update 3.0
 */
fun handleItemClick(
    listItem: ListItem,
    navActions: NavigationActions,
    viewModel: SpotifySongsActions,
    isTopSongsListVisible: Boolean
) {
    if(listItem.id.contains("spotify:track:")) {
        viewModel.addTrackToList(
            if (isTopSongsListVisible) ListType.TOP_SONGS else ListType.LIKED_SONGS,
            Track(listItem.id, listItem.title, listItem.subtitle))
        navActions.goBack()
        return
    }

    val playlistHeader = "spotify:playlist:"
    if (listItem.id.contains(playlistHeader)) {
        viewModel.getTracksFromPlaylist(
            listItem.id.substring(playlistHeader.length))
        return
    }

  if (listItem.hasChildren) {
    viewModel.retrieveChild(listItem)
    return
  }

    viewModel.addTrackToList(
        if (isTopSongsListVisible) ListType.TOP_SONGS else ListType.LIKED_SONGS,
        Track(listItem.id, listItem.title, listItem.subtitle))
    navActions.goBack()
}
