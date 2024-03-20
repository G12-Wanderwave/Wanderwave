package ch.epfl.cs311.wanderwave.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import ch.epfl.cs311.wanderwave.ui.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.navigation.Route
import ch.epfl.cs311.wanderwave.ui.navigation.TOP_LEVEL_DESTINATIONS
import ch.epfl.cs311.wanderwave.ui.screens.LaunchScreen
import ch.epfl.cs311.wanderwave.ui.screens.LoginScreen
import ch.epfl.cs311.wanderwave.ui.theme.WanderwaveTheme

@Composable
fun App(navController: NavHostController) {
  WanderwaveTheme {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
      AppScaffold(navController)
    }
  }
}

@Composable
fun AppScaffold(navController: NavHostController) {
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route
  val navActions = NavigationActions(navController)

  Scaffold(
      bottomBar = {
        Row(
            horizontalArrangement = Arrangement.Center,
        ) {
          BottomAppBar(
              modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
                TOP_LEVEL_DESTINATIONS.forEach { destination ->
                  Button(onClick = { navActions.navigateTo(destination)}, modifier = Modifier.padding(8.dp)) {
                    Text(text = destination.route)
                  }
                }
              }
        }
      }) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.LAUNCH,
            modifier = Modifier.padding(innerPadding)) {
              composable(Route.LAUNCH) { LaunchScreen() }
              composable(Route.LOGIN) { LoginScreen() }
            }
      }
}
