package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.viewmodel.BeaconViewModel

@Composable
fun BeaconScreen(
    navigationActions: NavigationActions,
    viewModel: BeaconViewModel = hiltViewModel()
) {
  BeaconScreen()
}

@Composable
@Preview
private fun BeaconScreen() {
  Column(modifier = Modifier.fillMaxSize()) { Text(text = "Beacon") }
}
