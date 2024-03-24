package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import ch.epfl.cs311.wanderwave.ui.navigation.NavigationActions

@Composable
fun MainPlaceHolder(navigationActions: NavigationActions) {
  Row(modifier = Modifier.testTag("mainPlaceHolderScreen")) {
    Text(text = "MainPlaceHolder")
    Button(
        onClick = { navigationActions.navigateToLogin() },
        modifier = Modifier.testTag("signOutButton")) {
          Text(text = "Sign Out")
        }
  }
}

/*
@Composable
fun ThemeScreen() {
  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.fillMaxSize().testTag("themeScreen")) {
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
 */
