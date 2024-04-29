package ch.epfl.cs311.wanderwave.ui.components.tracklist

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ch.epfl.cs311.wanderwave.model.data.Track

@Composable
fun TrackListItem(track: Track, selected: Boolean, onClick: () -> Unit) {
  Card(
    onClick = onClick,
    colors =
    CardColors(
      containerColor =
        if (selected) MaterialTheme.colorScheme.surfaceContainerHighest
        else MaterialTheme.colorScheme.surfaceContainerHigh,
      CardDefaults.cardColors().contentColor,
      CardDefaults.cardColors().disabledContainerColor,
      CardDefaults.cardColors().disabledContentColor),
    modifier = Modifier.height(80.dp).fillMaxWidth().padding(4.dp).testTag("trackItem")) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier = Modifier.fillMaxHeight().aspectRatio(1f),
        contentAlignment = Alignment.Center) {
        Image(
          imageVector = Icons.Default.PlayArrow,
          contentDescription = "Album Cover",
          modifier = Modifier.fillMaxSize(.8f),
        )
      }
      Column(modifier = Modifier.padding(8.dp)) {
        Text(
          text = track.title,
          color = MaterialTheme.colorScheme.onSurface,
          style = MaterialTheme.typography.titleMedium)
        Text(
          text = track.artist,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          style = MaterialTheme.typography.bodyMedium,
        )
      }
    }
  }
}