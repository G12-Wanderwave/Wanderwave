package ch.epfl.cs311.wanderwave.ui.components.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ch.epfl.cs311.wanderwave.R
import ch.epfl.cs311.wanderwave.ui.components.animated.ScrollingTitle

@Composable
fun MiniPlayer(
    isPlaying: Boolean,
    onTitleClick: () -> Unit,
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit,
    progress: Float
) {
  Column(modifier = Modifier.testTag("miniPlayer")) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.height(60.dp).fillMaxWidth()) {
          Box(modifier = Modifier.weight(1f).background(Color.Black)) {}

          MiniPlayerTitle(modifier = Modifier.weight(4f), isPlaying = isPlaying) { onTitleClick() }

          PlayPauseButton(
              modifier = Modifier.weight(1f),
              isPlaying = isPlaying,
              onPlayClick = onPlayClick,
              onPauseClick = onPauseClick)
        }
    ProgressBar(progress = progress)
  }
}

@Composable
fun MiniPlayerTitle(modifier: Modifier, isPlaying: Boolean, onTitleClick: () -> Unit) {
  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = modifier.clickable { onTitleClick() }.testTag("miniPlayerTitleButton")) {
        ScrollingTitle(artist = "Travis Scott", title = "STARGAZING", isPlaying = isPlaying)
      }
}

@Composable
fun PlayPauseButton(
    modifier: Modifier,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit
) {
  IconButton(
      modifier = modifier.testTag("playPauseButton"),
      onClick = {
        if (!isPlaying) {
          onPlayClick()
        } else {
          onPauseClick()
        }
      }) {
        Icon(
            painter =
                if (isPlaying) painterResource(id = R.drawable.pause_icon)
                else painterResource(id = R.drawable.play_icon),
            contentDescription = "Play Pause Icons",
            tint = if (isPlaying) Color(0xFF1DB954) else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(if (isPlaying) 30.dp else 50.dp))
      }
}

@Composable
fun ProgressBar(progress: Float) {
  Box(modifier = Modifier.fillMaxWidth().height(2.dp), contentAlignment = Alignment.BottomStart) {
    LinearProgressIndicator(
        progress = progress, modifier = Modifier.fillMaxWidth(), color = Color.White)
  }
}
