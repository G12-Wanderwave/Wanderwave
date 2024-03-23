package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import ch.epfl.cs311.wanderwave.ui.components.LoginScreenHeader
import ch.epfl.cs311.wanderwave.ui.components.SignInButton
import ch.epfl.cs311.wanderwave.ui.components.WelcomeTitle
import ch.epfl.cs311.wanderwave.ui.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.navigation.Route
import ch.epfl.cs311.wanderwave.ui.navigation.TOP_LEVEL_DESTINATIONS

@Composable
fun LoginScreen(navigationActions: NavigationActions) {
  Column(modifier = Modifier.testTag("loginScreen")) {
    LoginScreenHeader(modifier = Modifier.weight(1.5f))
    WelcomeTitle(modifier = Modifier.weight(4f))
    SignInButton(modifier = Modifier.weight(1f)) {
      navigationActions.navigateTo(TOP_LEVEL_DESTINATIONS.first { it.route == Route.MAIN })
    }
  }
}
