package ch.epfl.cs311.wanderwave.ui.components.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
  Box(
      modifier = modifier.fillMaxSize().testTag("loadingScreen"),
      contentAlignment = androidx.compose.ui.Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.width(64.dp).testTag("loadingScreenIndicator"),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant)
      }
}
