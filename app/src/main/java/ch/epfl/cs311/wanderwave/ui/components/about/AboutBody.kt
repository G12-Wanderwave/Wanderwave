package ch.epfl.cs311.wanderwave.ui.components.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.epfl.cs311.wanderwave.R
import ch.epfl.cs311.wanderwave.ui.theme.placeholderColor

@Composable
fun AboutBody(modifier: Modifier) {
  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = modifier.fillMaxWidth()) {
        Text(text = "Developed by", style = MaterialTheme.typography.displayMedium)
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
              Icon(
                  painter = painterResource(id = R.drawable.epfl_logo),
                  contentDescription = "EPFL logo",
                  tint = Color.Red,
                  modifier = Modifier.height(25.dp))
              Text(
                  text = "CS311-",
                  style = MaterialTheme.typography.displayMedium,
                  textAlign = TextAlign.Center)
              Text(
                  text = "1",
                  color = MaterialTheme.colorScheme.primary,
                  style = MaterialTheme.typography.displayMedium)
              Text(
                  text = "2",
                  color = placeholderColor,
                  style = MaterialTheme.typography.displayMedium)
            }
      }
}
