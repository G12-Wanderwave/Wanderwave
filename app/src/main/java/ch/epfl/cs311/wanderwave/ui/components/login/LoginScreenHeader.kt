package ch.epfl.cs311.wanderwave.ui.components.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.epfl.cs311.wanderwave.R
import ch.epfl.cs311.wanderwave.ui.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.navigation.Route

@Composable
fun LoginScreenHeader(navigationActions: NavigationActions, modifier: Modifier) {
  val startColor = MaterialTheme.colorScheme.primary
  val endColor = Color(0xFFE91E62)
  val colorSpots = List(10) { i -> lerp(startColor, endColor, i / 9f) }
  FloatingActionButton(
      onClick = { navigationActions.navigateTo(Route.ABOUT) },
      containerColor = MaterialTheme.colorScheme.background) {
        Column(modifier = modifier.padding(start = 47.dp, end = 47.dp)) {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Icon(
                painter = painterResource(id = R.drawable.info),
                contentDescription = "Info Icon",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(40.dp))
          }
          Row(
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()) {
                Icon(
                    painter = painterResource(id = R.drawable.wanderwave_icon),
                    contentDescription = "app icon",
                    tint = colorSpots[0],
                    modifier = Modifier.height(50.dp).testTag("appIcon"))

                Text(
                    text = "a",
                    style = MaterialTheme.typography.displayLarge,
                    color = colorSpots[1],
                    textAlign = TextAlign.Center)
                Text(
                    text = "n",
                    style = MaterialTheme.typography.displayLarge,
                    color = colorSpots[2],
                    textAlign = TextAlign.Center)
                Text(
                    text = "d",
                    style = MaterialTheme.typography.displayLarge,
                    color = colorSpots[3],
                    textAlign = TextAlign.Center)
                Text(
                    text = "e",
                    style = MaterialTheme.typography.displayLarge,
                    color = colorSpots[4],
                    textAlign = TextAlign.Center)
                Text(
                    text = "r",
                    style = MaterialTheme.typography.displayLarge,
                    color = colorSpots[5],
                    textAlign = TextAlign.Center)
                Text(
                    text = "w",
                    style = MaterialTheme.typography.displayLarge,
                    color = colorSpots[6],
                    textAlign = TextAlign.Center)
                Text(
                    text = "a",
                    style = MaterialTheme.typography.displayLarge,
                    color = colorSpots[7],
                    textAlign = TextAlign.Center)
                Text(
                    text = "v",
                    style = MaterialTheme.typography.displayLarge,
                    color = colorSpots[8],
                    textAlign = TextAlign.Center)
                Text(
                    text = "e",
                    style = MaterialTheme.typography.displayLarge,
                    color = colorSpots[9],
                    textAlign = TextAlign.Center)
              }
          Row(
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.circle_icon),
                    contentDescription = "Circle Icon",
                    tint = MaterialTheme.colorScheme.primary)
                LoginScreenPulse(modifier = Modifier)
                Icon(
                    painter = painterResource(id = R.drawable.wanderwave_icon),
                    contentDescription = "Circle Icon",
                    tint = Color(0xFFE91E62),
                    modifier = Modifier.size(30.dp))
              }
        }
      }
}

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
