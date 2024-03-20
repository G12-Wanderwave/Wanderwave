package ch.epfl.cs311.wanderwave

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import ch.epfl.cs311.wanderwave.ui.App
import ch.epfl.cs311.wanderwave.viewmodel.TrackListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  private val trackListViewModel: TrackListViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      val navController = rememberNavController()
      App(navController = navController, trackListViewModel = trackListViewModel)
    }
  }
}
