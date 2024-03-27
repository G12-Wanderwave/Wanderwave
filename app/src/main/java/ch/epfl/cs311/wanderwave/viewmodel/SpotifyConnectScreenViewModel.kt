package ch.epfl.cs311.wanderwave.viewmodel

import androidx.lifecycle.ViewModel
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

@HiltViewModel
class SpotifyConnectScreenViewModel
@Inject
constructor(private val spotifyController: SpotifyController) : ViewModel() {

  private var _uiState = MutableStateFlow(SpotifyConnectScreenUiState())
  val uiState: StateFlow<SpotifyConnectScreenUiState> = _uiState

  suspend fun connectRemote() {
    val connectResult = spotifyController.connectRemote().first()
    if (connectResult == SpotifyController.ConnectResult.SUCCESS) {
      _uiState.value = SpotifyConnectScreenUiState(hasResult = true, success = true)
    } else {
      _uiState.value = SpotifyConnectScreenUiState(hasResult = true, success = false)
    }
  }

  data class SpotifyConnectScreenUiState(
      val hasResult: Boolean = false,
      val success: Boolean = false,
  )
}