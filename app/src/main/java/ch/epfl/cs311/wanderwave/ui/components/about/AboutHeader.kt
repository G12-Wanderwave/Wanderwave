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
fun AboutHeader(modifier: Modifier) {
  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = modifier.fillMaxWidth()) {
        Text(text = "Wanderwave", style = MaterialTheme.typography.displayMedium)
        Text(
            text = "alpha v1.0",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center)
      }
}
