package ch.epfl.cs311.wanderwave.ui.components.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

@Composable
fun AboutBody(modifier: Modifier) {
  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = modifier.fillMaxWidth()) {
        Text(text = "Developed by", style = MaterialTheme.typography.displayMedium)
        Text(
            text = "CS311 - Team 12",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center)
      }
}
