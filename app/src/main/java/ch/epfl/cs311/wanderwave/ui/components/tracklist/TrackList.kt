package ch.epfl.cs311.wanderwave.ui.components.tracklist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.epfl.cs311.wanderwave.R
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.ui.components.profile.AddTrackDialog

@Composable
fun TrackList(
    tracks: List<Track>,
    title: String? = null,
    onAddTrack: (Track) -> Unit,
    onSelectTrack: (Track) -> Unit = {},
    canAddSong: Boolean = true
) {

  Column {
    var showDialog by remember { mutableStateOf(false) }
    var selectedTrack by remember { mutableStateOf<Track?>(null) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {
          if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.testTag("trackListTitle"))
          }
          if (canAddSong) {
            IconButton(onClick = { showDialog = true }) { // Toggle dialog visibility
              Icon(
                  imageVector = Icons.Filled.Add,
                  contentDescription = stringResource(R.string.beaconTitle))
            }
          }
        }
    LazyColumn {
      items(tracks) { track ->
        TrackListItem(
            track,
            track == selectedTrack,
            onClick = {
              selectedTrack = track
              onSelectTrack(track)
            })
      }
    }
    if (showDialog) {
      AddTrackDialog(
          onAddTrack = {
            onAddTrack(it)
            showDialog = false
          },
          onDismiss = {
            showDialog = false // Close dialog on dismiss
          },
          initialTrackId = "",
          initialTrackTitle = "",
          initialTrackArtist = "",
          dialogTestTag = "addTrackDialog" // For testing purposes
          )
    }
  }
}
