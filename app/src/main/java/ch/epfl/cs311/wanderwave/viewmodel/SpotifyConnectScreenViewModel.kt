package ch.epfl.cs311.wanderwave.viewmodel

import androidx.lifecycle.ViewModel
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.single

@HiltViewModel
class SpotifyConnectScreenViewModel
@Inject
constructor(
    private val spotifyController: SpotifyController,
    private val authenticationController: AuthenticationController
) : ViewModel() {

  private var _uiState = MutableStateFlow(UiState())
  val uiState: StateFlow<UiState> = _uiState

  suspend fun connectRemote() {
    if (!authenticationController.refreshTokenIfNecessary()) {
      _uiState.value = UiState(hasResult = true, success = false)
      return
    }

    if (spotifyController.isConnected()) {
      _uiState.value = UiState(hasResult = true, success = true)
      return
    }

    val connectSuccess =
        spotifyController.connectRemote().single() == SpotifyController.ConnectResult.SUCCESS
    _uiState.value = UiState(hasResult = true, success = connectSuccess)
  }

  data class UiState(
      val hasResult: Boolean = false,
      val success: Boolean = false,
  )
}
