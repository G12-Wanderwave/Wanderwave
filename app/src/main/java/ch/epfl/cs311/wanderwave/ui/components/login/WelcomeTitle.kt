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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.epfl.cs311.wanderwave.R
import ch.epfl.cs311.wanderwave.ui.components.animated.AnimatedIcon
import ch.epfl.cs311.wanderwave.ui.components.animated.LoginScreenPulse

@Composable
fun WelcomeTitle(modifier: Modifier) {
  Column(
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = modifier.fillMaxWidth()) {
        AnimatedIcon()
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
                  painter = painterResource(id = R.drawable.hexagon),
                  contentDescription = stringResource(id = R.string.circle_icon),
                  tint = Color.Red)
              LoginScreenPulse(startColor = Color.Red, endColor = Color.Magenta, reverse = false)
              Icon(
                  painter = painterResource(id = R.drawable.hexagon),
                  contentDescription = stringResource(id = R.string.circle_icon),
                  tint = Color.Red,
                  modifier = Modifier.size(20.dp))
            }
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically) {
              Icon(
                  painter = painterResource(id = R.drawable.hexagon),
                  contentDescription = stringResource(id = R.string.circle_icon),
                  tint = Color.Green)
              LoginScreenPulse(startColor = Color.Green, endColor = Color.Magenta, reverse = false)
              Icon(
                  painter = painterResource(id = R.drawable.hexagon),
                  contentDescription = stringResource(id = R.string.circle_icon),
                  tint = Color.Green,
                  modifier = Modifier.size(20.dp))
            }
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically) {
              Icon(
                  painter = painterResource(id = R.drawable.hexagon),
                  contentDescription = stringResource(id = R.string.circle_icon),
                  tint = Color.Blue)
              LoginScreenPulse(startColor = Color.Blue, endColor = Color.Magenta, reverse = false)
              Icon(
                  painter = painterResource(id = R.drawable.hexagon),
                  contentDescription = stringResource(id = R.string.circle_icon),
                  tint = Color.Blue,
                  modifier = Modifier.size(20.dp))
            }
      }
}
