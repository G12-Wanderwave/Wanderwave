package ch.epfl.cs311.wanderwave.ui.components.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.epfl.cs311.wanderwave.R
import ch.epfl.cs311.wanderwave.ui.components.animated.AnimatedIcon
import ch.epfl.cs311.wanderwave.ui.components.animated.LoginScreenPulse
import ch.epfl.cs311.wanderwave.ui.theme.placeholderColor

@Composable
fun WelcomeTitle(modifier: Modifier) {
  Column(
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = modifier.fillMaxWidth()) {
        AnimatedIcon()
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically) {
              Icon(
                  painter = painterResource(id = R.drawable.circle_icon),
                  contentDescription = stringResource(id = R.string.circle_icon),
                  tint = placeholderColor)
              LoginScreenPulse(modifier = Modifier, reverse = true)
              Icon(
                  painter = painterResource(id = R.drawable.wanderwave_icon),
                  contentDescription = stringResource(id = R.string.circle_icon),
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(25.dp))
            }
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
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically) {
              Icon(
                  painter = painterResource(id = R.drawable.circle_icon),
                  contentDescription = stringResource(id = R.string.circle_icon),
                  tint = MaterialTheme.colorScheme.primary)
              LoginScreenPulse(modifier = Modifier, reverse = false)
              Icon(
                  painter = painterResource(id = R.drawable.wanderwave_icon),
                  contentDescription = stringResource(id = R.string.circle_icon),
                  tint = placeholderColor,
                  modifier = Modifier.size(25.dp))
            }
      }
}
