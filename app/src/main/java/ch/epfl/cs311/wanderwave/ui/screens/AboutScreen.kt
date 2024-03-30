package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.epfl.cs311.wanderwave.BuildConfig
import ch.epfl.cs311.wanderwave.R
import ch.epfl.cs311.wanderwave.navigation.NavigationActions

@Composable
fun AboutScreen(navigationActions: NavigationActions) {
  Column(modifier = Modifier.fillMaxSize()) {
    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
      FloatingActionButton(
          onClick = { navigationActions.goBack() },
          containerColor = MaterialTheme.colorScheme.background) {
            Icon(
                painter = painterResource(id = R.drawable.close_icon),
                contentDescription = "Close Icon")
          }
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth().weight(1f)) {
          Text(
              text = stringResource(id = R.string.upper_case_app_name),
              style = MaterialTheme.typography.displayMedium)
          Text(
              text = BuildConfig.VERSION_NAME,
              style = MaterialTheme.typography.titleMedium,
              textAlign = TextAlign.Center)
        }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth().weight(4f)) {
          Text(
              text = stringResource(id = R.string.developed_by),
              style = MaterialTheme.typography.displayMedium)
          Row(
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.epfl_logo),
                    contentDescription = "EPFL logo",
                    tint = Color.Red,
                    modifier = Modifier.height(25.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "CS311-",
                    style = MaterialTheme.typography.displayMedium,
                    textAlign = TextAlign.Center)
                Text(
                    text = "1",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.displayMedium)
                Text(text = "2", color = Color.Red, style = MaterialTheme.typography.displayMedium)
              }
        }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth().weight(1f)) {
          Text(
              text = stringResource(id = R.string.milestone_number),
              style = MaterialTheme.typography.displayMedium)
          Text(
              text = stringResource(id = R.string.milestone_date),
              style = MaterialTheme.typography.titleMedium,
              textAlign = TextAlign.Center)
        }
  }
}
