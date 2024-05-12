package ch.epfl.cs311.wanderwave.ui.components.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import ch.epfl.cs311.wanderwave.R
import ch.epfl.cs311.wanderwave.ui.components.animated.AnimatedAppIcon

@Composable
fun WelcomeTitle(modifier: Modifier) {
  Column(
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = modifier.fillMaxWidth()) {
        AnimatedAppIcon(initialColor = MaterialTheme.colorScheme.primary, finalColor = Color.Red)
        Text(
            text = stringResource(id = R.string.welcome_title),
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.testTag("welcomeTitle"))
        Text(
            text = stringResource(id = R.string.welcome_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.testTag("welcomeSubtitle"))
      }
}
