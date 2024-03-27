package ch.epfl.cs311.wanderwave.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ch.epfl.cs311.wanderwave.ui.navigation.NavigationActions

@Composable
fun ScreenScaffold(navActions: NavigationActions, content: @Composable () -> Unit) {
  Scaffold(
      bottomBar = {
        AppBottomBar(navActions = navActions, currentRoute = navActions.getCurrentRoute())
      }) {
        Surface(modifier = Modifier.padding(it).fillMaxSize()) { content() }
      }
}
