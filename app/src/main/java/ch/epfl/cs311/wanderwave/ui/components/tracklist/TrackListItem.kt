package ch.epfl.cs311.wanderwave.ui.components.tracklist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
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


