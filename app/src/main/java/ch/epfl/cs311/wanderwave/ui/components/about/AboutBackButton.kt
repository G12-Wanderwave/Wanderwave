package ch.epfl.cs311.wanderwave.ui.components.about

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import ch.epfl.cs311.wanderwave.R

@Composable
fun AboutBackButton(onClick: () -> Unit) {
  FloatingActionButton(
      onClick = { onClick() }, containerColor = MaterialTheme.colorScheme.background) {
        Icon(
            painter = painterResource(id = R.drawable.close_icon),
            contentDescription = "Close Icon")
      }
}
