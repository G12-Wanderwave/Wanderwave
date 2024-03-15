package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun LaunchScreen() {
  Text(text = "LaunchScreen Placeholder")
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
