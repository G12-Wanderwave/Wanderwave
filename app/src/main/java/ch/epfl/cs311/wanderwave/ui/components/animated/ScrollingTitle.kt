package ch.epfl.cs311.wanderwave.ui.components.animated

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ScrollingTitle(title: String, isPlaying: Boolean, modifier: Modifier = Modifier) {
  val infiniteTransition = rememberInfiniteTransition(label = "")
  val boxWidth = remember { mutableFloatStateOf(0f) }
  val xPosition =
      infiniteTransition.animateFloat(
          initialValue = boxWidth.floatValue,
          targetValue = -boxWidth.floatValue,
          animationSpec =
              infiniteRepeatable(
                  animation = tween(10000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
          label = "")

  val gradient =
      Brush.horizontalGradient(
          colors =
              listOf(
                  Color.Transparent,
                  if (!isPlaying) MaterialTheme.colorScheme.primary.copy(alpha = .35f)
                  else Color(0xFF1DB954).copy(alpha = .35f),
                  Color.Transparent),
          startX = 0f,
          endX = boxWidth.floatValue)

  Column(
      modifier =
          Modifier.height(59.dp)
              .fillMaxWidth()
              .clip(RectangleShape)
              .background(gradient)
              .onSizeChanged { boxWidth.floatValue = it.width.toFloat() },
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceAround) {
        Text(text = "Yung Gravy", style = MaterialTheme.typography.bodyLarge)
        Text(
            text = title,
            modifier =
                modifier.graphicsLayer(translationX = if (isPlaying) xPosition.value else 0f),
            textAlign = TextAlign.Center,
            maxLines = 2)
      }
}
