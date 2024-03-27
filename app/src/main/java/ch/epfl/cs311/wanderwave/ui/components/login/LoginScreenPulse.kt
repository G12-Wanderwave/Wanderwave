package ch.epfl.cs311.wanderwave.ui.components.login

import androidx.compose.animation.animateColor
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
fun LoginScreenPulse(modifier: Modifier) {
  val infiniteTransition = rememberInfiniteTransition(label = "")
  val width by
      infiniteTransition.animateFloat(
          initialValue = 1f,
          targetValue = 290f,
          animationSpec =
              infiniteRepeatable(
                  animation = tween(1500, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
          label = "")
  val height by
      infiniteTransition.animateFloat(
          initialValue = 1f,
          targetValue = 30f,
          animationSpec =
              infiniteRepeatable(
                  animation = tween(1500, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
          label = "")
  val color by
      infiniteTransition.animateColor(
          initialValue = MaterialTheme.colorScheme.primary,
          targetValue = Color(0xFFE91E62),
          animationSpec =
              infiniteRepeatable(
                  animation = tween(500, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
          label = "")

  Box(
      modifier = modifier.height(5.dp),
      contentAlignment = Alignment.CenterStart,
  ) {
    Box(modifier = modifier.height(height.dp).width(width.dp).background(color))
  }
}
