package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun LoginScreen() {
  Text(text = "LoginScreen Placeholder", modifier = Modifier.testTag("loginScreen"))
}
