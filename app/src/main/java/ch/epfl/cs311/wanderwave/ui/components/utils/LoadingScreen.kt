package ch.epfl.cs311.wanderwave.ui.components.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import ch.epfl.cs311.wanderwave.ui.components.animated.AnimatedAppIcon

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
  Box(
      modifier = modifier.fillMaxSize().testTag("loadingScreen"),
      contentAlignment = androidx.compose.ui.Alignment.Center) {
        AnimatedAppIcon(initialColor = Color(0xFF03045E), finalColor = Color(0xFFCAF0F8))
      }
}
