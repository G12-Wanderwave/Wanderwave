package ch.epfl.cs311.wanderwave.ui.components.login

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ch.epfl.cs311.wanderwave.R

@Composable
fun LoginScreenAnimation(modifier: Modifier) {
  val infiniteTransition = rememberInfiniteTransition(label = "")
  val size by
      infiniteTransition.animateFloat(
          initialValue = 30f,
          targetValue = 250f,
          // min(400f, LocalConfiguration.current.screenWidthDp.toFloat()),
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

  Box(modifier = modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
    Icon(
        painter = painterResource(id = R.drawable.wanderwave_icon),
        contentDescription = "Animated App Icon",
        modifier = Modifier.size(size.dp),
        tint = color)
  }
}
