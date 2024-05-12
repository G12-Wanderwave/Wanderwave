package ch.epfl.cs311.wanderwave.ui.components.animated

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ch.epfl.cs311.wanderwave.R

@Composable
fun AnimatedAppIcon(initialColor: Color, finalColor: Color) {
  val infiniteTransition = rememberInfiniteTransition(label = "")
  val size by
      infiniteTransition.animateFloat(
          initialValue = 150f,
          targetValue = 170f,
          animationSpec =
              infiniteRepeatable(
                  animation = tween(300, easing = FastOutLinearInEasing),
                  repeatMode = RepeatMode.Reverse),
          label = "")

  val color by
      infiniteTransition.animateColor(
          initialValue = initialColor,
          targetValue = finalColor,
          animationSpec =
              infiniteRepeatable(
                  animation = tween(1000, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
          label = "")

  Box(modifier = Modifier.size(250.dp), contentAlignment = Alignment.Center) {
    Icon(
        painter = painterResource(id = R.drawable.wanderwave_icon),
        contentDescription = "Animated App Icon",
        modifier = Modifier.size(size.dp),
        tint = color)
  }
}
