package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import ch.epfl.cs311.wanderwave.navigation.NavigationActions

@Composable
fun MainPlaceHolder(navigationActions: NavigationActions) {
  Column(modifier = Modifier.testTag("mainPlaceHolderScreen")) {
      ProfileButton(navActions = navigationActions)
      //TODO: move to profile screen
//    Button(
//        onClick = { navigationActions.navigateToTopLevel(Route.LOGIN) },
//        modifier = Modifier.testTag("signOutButton")) {
//          Text(text = "Sign Out")
//        }
    AboutScreen(navigationActions = navigationActions)
  }
}
