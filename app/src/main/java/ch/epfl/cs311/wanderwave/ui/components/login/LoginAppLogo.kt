package ch.epfl.cs311.wanderwave.ui.components.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import ch.epfl.cs311.wanderwave.ui.components.utils.lerp

@Composable
fun LoginAppLogo(modifier: Modifier) {
  val startColor = MaterialTheme.colorScheme.primary
  val endColor = Color.Red
  val colorSpots = List(10) { i -> lerp(startColor, endColor, i / 9f) }

  Column(
      modifier = modifier.padding(start = 60.dp, end = 60.dp).padding(top = 30.dp),
      verticalArrangement = Arrangement.Bottom) {
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
              anderwave.forEachIndexed { index, letter ->
                FillLogo(letter = letter, colorSpots[index + 1])
              }
            }
      }
}

@Composable
private fun FillLogo(letter: String, color: Color) {
  Text(
      text = letter,
      style = MaterialTheme.typography.displayMedium,
      color = color,
      textAlign = TextAlign.Center)
}
