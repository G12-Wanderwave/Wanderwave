package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import ch.epfl.cs311.wanderwave.ui.components.login.LoginScreenHeader
import ch.epfl.cs311.wanderwave.ui.components.login.SignInButton
import ch.epfl.cs311.wanderwave.ui.components.login.WelcomeTitle
import ch.epfl.cs311.wanderwave.ui.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.navigation.Route

@Composable
fun LoginScreen(navigationActions: NavigationActions) {
  Column(modifier = Modifier.testTag("loginScreen")) {
    LoginScreenHeader(navigationActions, modifier = Modifier.weight(1.5f))
    // LoginScreenAnimation(modifier = Modifier.weight(3f))
    WelcomeTitle(modifier = Modifier.weight(4f))
    SignInButton(modifier = Modifier.weight(1f)) {
      navigationActions.navigateToTopLevel(Route.MAIN)
    }
  }
}
