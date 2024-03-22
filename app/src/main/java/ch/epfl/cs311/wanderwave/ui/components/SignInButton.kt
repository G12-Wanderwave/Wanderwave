package ch.epfl.cs311.wanderwave.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.epfl.cs311.wanderwave.R

@Composable
fun SignInButton(modifier: Modifier, onClick: () -> Unit) {
  Box(modifier = modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
    Button(onClick = { onClick() }) {
      Icon(
          painter = painterResource(id = R.drawable.spotify_icon_rgb_black),
          contentDescription = "Spotify Icon",
          modifier = Modifier.height(27.dp))
      Spacer(modifier = Modifier.width(12.dp))
      Text(
          text = stringResource(id = R.string.button_label),
          style = MaterialTheme.typography.titleMedium)
    }
  }
}
