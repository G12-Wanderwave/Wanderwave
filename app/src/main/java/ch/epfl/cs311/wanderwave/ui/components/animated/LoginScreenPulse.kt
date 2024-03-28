package ch.epfl.cs311.wanderwave.ui.components.animated

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutLinearInEasing
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreenPulse(startColor: Color, endColor: Color, reverse: Boolean) {
  val startWidth = if (reverse) 0f else 230f
  val endWidth = if (reverse) 230f else 0f

  val startHeight = if (reverse) 18f else 3f
  val endHeight = if (reverse) 3f else 18f

  val infiniteTransition = rememberInfiniteTransition(label = "")
  val width by
      infiniteTransition.animateFloat(
          initialValue = startWidth,
          targetValue = endWidth,
          animationSpec =
              infiniteRepeatable(
                  animation = tween(1500, easing = FastOutLinearInEasing),
                  repeatMode = RepeatMode.Reverse),
          label = "")
  val height by
      infiniteTransition.animateFloat(
          initialValue = startHeight,
          targetValue = endHeight,
          animationSpec =
              infiniteRepeatable(
                  animation = tween(1500, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
          label = "")
  val color by
      infiniteTransition.animateColor(
          initialValue = startColor,
          targetValue = endColor,
          animationSpec =
              infiniteRepeatable(
                  animation = tween(1500, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
          label = "")

  Box(
      modifier = Modifier.height(20.dp),
      contentAlignment = Alignment.CenterStart,
  ) {
    Box(modifier = Modifier.height(height.dp).width(width.dp).background(color))
  }
}
