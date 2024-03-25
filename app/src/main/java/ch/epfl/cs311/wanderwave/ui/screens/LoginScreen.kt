package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import ch.epfl.cs311.wanderwave.ui.components.login.LoginScreenHeader
import ch.epfl.cs311.wanderwave.ui.components.login.SignInButton
import ch.epfl.cs311.wanderwave.ui.components.login.SpotifyAuthentication
import ch.epfl.cs311.wanderwave.ui.components.login.WelcomeTitle
import ch.epfl.cs311.wanderwave.ui.navigation.NavigationActions

@Composable
fun LoginScreen(navigationActions: NavigationActions) {
  var spotifyLoginShown: Boolean by remember { mutableStateOf(false) }

  Column(modifier = Modifier.testTag("loginScreen")) {
    LoginScreenHeader(modifier = Modifier.weight(1.5f))
    WelcomeTitle(modifier = Modifier.weight(4f))
    SignInButton(modifier = Modifier.weight(1f)) { spotifyLoginShown = true }
    if (spotifyLoginShown) {
      SpotifyAuthentication(
          onSuccess = { token, expiresIn -> println("Logged in with token: $token, expiresIn: $expiresIn") },
          onError = { println("Error logging in: $it") },
          onCancel = { println("User cancelled login") })
    }
  }
}
