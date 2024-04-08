package ch.epfl.cs311.wanderwave.ui.components.tracklist

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import ch.epfl.cs311.wanderwave.model.data.Track

@Composable
fun TrackListItem(track: Track, selected: Boolean, onClick: () -> Unit) {
  TextButton(onClick = { onClick() }, modifier = Modifier.testTag("trackButton")) {
    Text(
        text = "${track.artist} - ${track.title}",
        style = MaterialTheme.typography.bodyLarge,
        color =
            if (selected) MaterialTheme.colorScheme.secondary
            else MaterialTheme.colorScheme.tertiary)
  }
}
