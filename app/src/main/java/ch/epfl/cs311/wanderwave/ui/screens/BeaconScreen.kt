package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.theme.WanderwaveTheme
import ch.epfl.cs311.wanderwave.viewmodel.BeaconViewModel

@Composable
fun BeaconScreen(
    navigationActions: NavigationActions,
    viewModel: BeaconViewModel = hiltViewModel()
) {
  BeaconScreen()
}

@Composable
@Preview(showBackground = true)
private fun BeaconScreenPreview() {
  WanderwaveTheme { BeaconScreen() }
}

@Composable
private fun BeaconScreen() {
  Column(
      modifier = Modifier.fillMaxSize().padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally) {
        BeaconInformation()
        SongList()
      }
}

@Composable fun BeaconInformation() {}

@Composable fun SongList() {}
