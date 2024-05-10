package ch.epfl.cs311.wanderwave.ui.components.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ch.epfl.cs311.wanderwave.model.data.ListType
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.data.viewModelType
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.components.tracklist.TrackList
import ch.epfl.cs311.wanderwave.viewmodel.SongList
import com.spotify.protocol.types.ListItem

/**
 * Dialog composable that allows the user to add a new track by entering the track ID, title, and
 * artist. On confirming, the track is added via the onAddTrack callback.
 *
 * @param onAddTrack Callback function to be invoked when the track is added.
 * @param onDismiss Callback function to be invoked when the dialog is dismissed.
 * @param initialTrackId Initial value for the track ID input field.
 * @param initialTrackTitle Initial value for the track title input field.
 * @param initialTrackArtist Initial value for the track artist input field.
 * @author Ayman Bakiri
 * @since 1.0
 * @last update 1.0
 */
@Composable
fun AddTrackDialog(
    onAddTrack: (Track) -> Unit,
    onDismiss: () -> Unit,
    initialTrackId: String,
    initialTrackTitle: String,
    initialTrackArtist: String,
    dialogTestTag: String
) {
  var newTrackId by remember { mutableStateOf(initialTrackId) }
  var newTrackTitle by remember { mutableStateOf(initialTrackTitle) }
  var newTrackArtist by remember { mutableStateOf(initialTrackArtist) }

  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text("Add New Track") },
      text = {
        Column {
          OutlinedTextField(
              value = newTrackId,
              onValueChange = { newTrackId = it },
              label = { Text("Track ID") },
              modifier = Modifier.testTag("trackIdInput"))
          OutlinedTextField(
              value = newTrackTitle,
              onValueChange = { newTrackTitle = it },
              label = { Text("Track Title") },
              modifier = Modifier.testTag("trackTitleInput"))
          OutlinedTextField(
              value = newTrackArtist,
              onValueChange = { newTrackArtist = it },
              label = { Text("Track Artist") },
              modifier = Modifier.testTag("trackArtistInput"))
        }
      },
      confirmButton = {
        Button(
            onClick = {
              onAddTrack(Track(newTrackId, newTrackTitle, newTrackArtist))
              newTrackId = "" // Resetting the state
              newTrackTitle = ""
              newTrackArtist = ""
            },
            modifier = Modifier.testTag("confirmAddTrack")) {
              Text("Add")
            }
      },
      dismissButton = {
        Button(onClick = onDismiss, modifier = Modifier.testTag("cancelAddTrack")) {
          Text("Cancel")
        }
      },
      modifier = Modifier.testTag(dialogTestTag))
}

/**
 * Displays either the "TOP SONGS" or "CHOSEN SONGS" list based on a toggle.
 *
 * @param songLists List of song lists including "TOP SONGS" and "CHOSEN SONGS".
 * @param isTopSongsListVisible Boolean state to toggle between showing "TOP SONGS" or "CHOSEN
 *   SONGS".
 *     @param onAddTrack Callback function to be invoked when a track is added.
 *     @param canAddSong Boolean state to enable or disable adding a song.
 *     @param viewModelName The name of the view model.
 *     @param navigationActions The navigation actions.
 *
 *     @author Menzo Bouaissi
 *     @author Ayman Bakiri
 *     @since 1.0
 *     @last update 2.0
 */
@Composable
fun SongsListDisplay(
    navigationActions: NavigationActions,
    songLists: List<SongList>,
    isTopSongsListVisible: Boolean,
    onAddTrack: (Track) -> Unit,
    canAddSong: Boolean = true,
    viewModelName: viewModelType = viewModelType.NULL
) {
  val name = if (isTopSongsListVisible) ListType.TOP_SONGS else ListType.CHOSEN_SONGS
  songLists
      .firstOrNull { it.name == name }
      ?.let { songList ->
        TrackList(
            tracks = songList.tracks,
            title = name.name,
            onSelectTrack = { /* TODO */},
            onAddTrack = onAddTrack,
            canAddSong = canAddSong,
            navActions = navigationActions,
            viewModelName = viewModelName)
      }
}

/**
 * Composable that displays a Track. Each track is represented by the TrackItem composable, which is
 * a Card with the track's title and subtitle.
 *
 * @author Menzo Bouaissi
 * @since 2.0
 * @last update 2.0
 */
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
      modifier =
          Modifier.height(80.dp)
              .fillMaxWidth()
              .padding(4.dp)
              .clickable(onClick = onClick)
              .testTag("trackItemCard")) {
        Row {
          Column(modifier = Modifier.padding(8.dp)) {
            Text(text = listItem.title, style = MaterialTheme.typography.titleMedium)
            Text(text = listItem.subtitle, style = MaterialTheme.typography.bodyMedium)
          }
        }
      }
}
