package ch.epfl.cs311.wanderwave.ui.components.animated

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class RectangleConstraints(
    val startWidth: Float,
    val startHeight: Float,
    val endWidth: Float,
    val endHeight: Float
)

data class ColorRange(val startColor: Color, val endColor: Color)

@Composable
fun FlexibleRectangle(
    rectangleConstraints: RectangleConstraints,
    colorRange: ColorRange,
    durationMillis: Int,
    isPlaying: Boolean = true,
    easing: Easing = LinearEasing,
    repeatMode: RepeatMode = RepeatMode.Reverse,
) {
  val infiniteTransition = rememberInfiniteTransition(label = "")
  val width by
      infiniteTransition.animateFloat(
          initialValue = rectangleConstraints.startWidth,
          targetValue = rectangleConstraints.endWidth,
          animationSpec =
              infiniteRepeatable(
                  animation = tween(durationMillis, easing = easing), repeatMode = repeatMode),
          label = "")
  val height by
      infiniteTransition.animateFloat(
          initialValue = rectangleConstraints.startHeight,
          targetValue = rectangleConstraints.endHeight,
          animationSpec =
              infiniteRepeatable(
                  animation = tween(durationMillis, easing = easing), repeatMode = repeatMode),
          label = "")
  val color by
      infiniteTransition.animateColor(
          initialValue = colorRange.startColor,
          targetValue = colorRange.endColor,
          animationSpec =
              infiniteRepeatable(
                  animation = tween(durationMillis, easing = easing), repeatMode = repeatMode),
          label = "")

  if (isPlaying) {
    Box(modifier = Modifier.height(height.dp).width(width.dp).background(color))
  } else {
    Box(modifier = Modifier)
  }
}
