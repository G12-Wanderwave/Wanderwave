package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment

@Composable
fun LaunchScreen() {
  Column (
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier = Modifier.fillMaxSize()
  ){

    Text(text = "Launch Screen",
      modifier = Modifier.padding(16.dp),
      fontWeight = FontWeight(700),
      fontSize = 40.sp,
    )

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
