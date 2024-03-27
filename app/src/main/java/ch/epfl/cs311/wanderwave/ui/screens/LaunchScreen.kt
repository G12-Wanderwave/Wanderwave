package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LaunchScreen() {
  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.fillMaxSize().testTag("launchScreen")) {
        Text(
            text = "Launch Screen",
            modifier = Modifier.padding(16.dp),
            fontWeight = FontWeight(700),
            fontSize = 40.sp,
            color = MaterialTheme.colorScheme.onBackground)
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(16.dp)
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                    .padding(16.dp)) {
              Text(
                  text = "Primary",
                  modifier = Modifier.align(Alignment.Center),
                  fontWeight = FontWeight(700),
                  fontSize = 40.sp,
                  color = MaterialTheme.colorScheme.primary)
            }

        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)) {
              Text(
                  text = "primary on surface",
                  modifier = Modifier.align(Alignment.Center),
                  fontWeight = FontWeight(700),
                  fontSize = 30.sp,
                  color = MaterialTheme.colorScheme.primary)
            }

        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(16.dp)) {
              Text(
                  text = "Primary Container",
                  modifier = Modifier.align(Alignment.Center),
                  fontWeight = FontWeight(700),
                  fontSize = 40.sp,
                  color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
      }
}

/*
 val user by userSessionViewModel.user.observeAsState()

 LaunchedEffect(key1 = user) {
     if (user != null) {
         navActions.navigateTo(TOP_LEVEL_DESTINATIONS.first { it.route == Route.MAIN })
     } else {
         navActions.navigateTo(DESTINATIONS.first { it.route == Route.LOGIN })
     }
 }
*/
