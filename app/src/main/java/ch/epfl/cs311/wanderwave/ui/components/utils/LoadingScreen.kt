package ch.epfl.cs311.wanderwave.ui.components.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import ch.epfl.cs311.wanderwave.ui.components.login.AppLogoWithTitle
import ch.epfl.cs311.wanderwave.ui.theme.coldEnd
import ch.epfl.cs311.wanderwave.ui.theme.coldStart

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
  Box(
      modifier = modifier.fillMaxSize().testTag("loadingScreen"),
      contentAlignment = Alignment.Center) {
        Column {
          Box(modifier = Modifier.weight(1f))
          AppLogoWithTitle(
              modifier = Modifier.weight(4f),
              initialColor = coldStart,
              finalColor = coldEnd,
              title = "",
              subTitle = "")
          Box(modifier = modifier.weight(1f))
        }
      }
}
