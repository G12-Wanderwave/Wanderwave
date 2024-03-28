package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ch.epfl.cs311.wanderwave.ui.components.about.AboutBackButton
import ch.epfl.cs311.wanderwave.ui.components.about.AboutBody
import ch.epfl.cs311.wanderwave.ui.components.about.AboutFooter
import ch.epfl.cs311.wanderwave.ui.components.about.AboutHeader
import ch.epfl.cs311.wanderwave.ui.navigation.NavigationActions

@Composable
fun AboutScreen(navigationActions: NavigationActions) {
  Column {
    AboutBackButton { navigationActions.goBack() }
    AboutHeader(modifier = Modifier.weight(1f))
    AboutBody(modifier = Modifier.weight(1f))
    AboutFooter(modifier = Modifier.weight(1f))
  }
}
