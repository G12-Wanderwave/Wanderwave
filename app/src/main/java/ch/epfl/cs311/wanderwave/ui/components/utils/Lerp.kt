package ch.epfl.cs311.wanderwave.ui.components.utils

import androidx.compose.ui.graphics.Color

fun lerp(start: Color, stop: Color, fraction: Float): Color {
  return Color(
      red = lerp(start.red, stop.red, fraction),
      green = lerp(start.green, stop.green, fraction),
      blue = lerp(start.blue, stop.blue, fraction),
      alpha = lerp(start.alpha, stop.alpha, fraction))
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
  return (1 - fraction) * start + fraction * stop
}
