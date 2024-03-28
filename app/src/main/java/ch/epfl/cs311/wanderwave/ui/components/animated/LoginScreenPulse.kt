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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreenPulse(modifier: Modifier, reverse: Boolean) {
  val startWidth = if (reverse) 15f else 230f
  val endWidth = if (reverse) 230f else 15f

  val startHeight = if (reverse) 15f else 1f
  val endHeight = if (reverse) 1f else 15f

  val startColor = if (reverse) MaterialTheme.colorScheme.primary else Color(0xFFE91E62)
  val endColor = if (reverse) Color(0xFFE91E62) else MaterialTheme.colorScheme.primary
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
      modifier = modifier.height(20.dp),
      contentAlignment = Alignment.CenterStart,
  ) {
    Box(modifier = modifier.height(height.dp).width(width.dp).background(color))
  }
}
