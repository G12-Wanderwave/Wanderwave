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
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.navigation.Route
import ch.epfl.cs311.wanderwave.ui.components.animated.utils.Lerp
import ch.epfl.cs311.wanderwave.ui.theme.placeholderColor

@Composable
fun LoginScreenHeader(navigationActions: NavigationActions, modifier: Modifier) {
  val startColor = MaterialTheme.colorScheme.primary
  val endColor = placeholderColor
  val colorSpots = List(10) { i -> Lerp().lerp(startColor, endColor, i / 9f) }
  FloatingActionButton(
      onClick = { navigationActions.navigateTo(Route.ABOUT) },
      containerColor = MaterialTheme.colorScheme.background,
      modifier = Modifier.testTag("appIcon").padding(top = 30.dp)) {
        Column(modifier = modifier.padding(start = 60.dp, end = 60.dp)) {
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
                    modifier = Modifier.height(40.dp).testTag("appIcon"))
                val anderwave: List<String> = listOf("a", "n", "d", "e", "r", "w", "a", "v", "e")
                anderwave.forEachIndexed() { index, letter ->
                  FillLogo(letter = letter, colorSpot = index, colorSpots = colorSpots)
                }
              }
        }
      }
}

@Composable
private fun FillLogo(letter: String, colorSpot: Int, colorSpots: List<Color>) {
  Text(
      text = letter,
      style = MaterialTheme.typography.displayMedium,
      color = colorSpots[colorSpot],
      textAlign = TextAlign.Center)
}
