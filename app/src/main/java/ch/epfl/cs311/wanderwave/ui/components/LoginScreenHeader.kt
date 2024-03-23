package ch.epfl.cs311.wanderwave.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ch.epfl.cs311.wanderwave.R

@Composable
fun LoginScreenHeader(modifier: Modifier) {

  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Icon(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "app logo",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.height(50.dp).fillMaxWidth().padding(start = 50.dp, end = 50.dp))
        Spacer(modifier = Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
              text = "powered by",
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.onSurface)
          Spacer(modifier = Modifier.width(10.dp))
          Image(
              painter = painterResource(id = R.drawable.spotify_logo_cmyk_green),
              contentDescription = "Spotify logo",
              modifier = Modifier.height(30.dp))
        }
      }
}
