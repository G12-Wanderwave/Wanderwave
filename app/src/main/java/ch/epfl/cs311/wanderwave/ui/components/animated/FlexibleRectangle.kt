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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun FlexibleRectangle(
    modifier: Modifier = Modifier,
    startWidth: Float,
    endWidth: Float,
    startHeight: Float,
    endHeight: Float,
    startColor: Color,
    endColor: Color,
    durationMillis: Int,
    isFiredOnce: Boolean,
    isPlaying: Boolean = true,
    easing: Easing = LinearEasing,
    repeatMode: RepeatMode = RepeatMode.Reverse,
    reverse: Boolean = false
) {
  val done = remember { mutableStateOf(false) }
  val startW = if (reverse) endWidth else startWidth
  val endW = if (reverse) startWidth else endWidth

  val startH = if (reverse) endHeight else startHeight
  val endH = if (reverse) startHeight else endHeight

  val startC = if (reverse) endColor else startColor
  val endC = if (reverse) startColor else endColor
  val infiniteTransition = rememberInfiniteTransition(label = "")
  val width by
      infiniteTransition.animateFloat(
          initialValue = startW,
          targetValue = endW,
          animationSpec =
              infiniteRepeatable(
                  animation = tween(durationMillis, easing = easing), repeatMode = repeatMode),
          label = "")
  val height by
      infiniteTransition.animateFloat(
          initialValue = startH,
          targetValue = endH,
          animationSpec =
              infiniteRepeatable(
                  animation = tween(durationMillis, easing = easing), repeatMode = repeatMode),
          label = "")
  val color by
      infiniteTransition.animateColor(
          initialValue = startC,
          targetValue = endC,
          animationSpec =
              infiniteRepeatable(
                  animation = tween(durationMillis, easing = easing), repeatMode = repeatMode),
          label = "")

  LaunchedEffect(isPlaying) {
    if (isPlaying && !done.value) {
      val startTime = System.currentTimeMillis()
      while (true) {
        delay(500L)
        val elapsedTime = System.currentTimeMillis() - startTime
        if (elapsedTime >= durationMillis) {
          done.value = true
          break
        }
      }
    } else {}
  }

  if (done.value && isFiredOnce) {
    Box(modifier = modifier.height(endH.dp).width(endW.dp).background(endColor))
  } else if (isPlaying) {
    Box(modifier = modifier.height(height.dp).width(width.dp).background(color))
  } else {
    Box(modifier = Modifier)
  }
}
