package ch.epfl.cs311.wanderwave.viewmodel

import androidx.lifecycle.ViewModel
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepository
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.runBlocking

@HiltViewModel
class SpotifyConnectScreenViewModel
@Inject
constructor(
    private val spotifyController: SpotifyController,
    private val authenticationController: AuthenticationController,
    private val profileRepository: ProfileRepository
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

  fun checkProfile() {
    val userId = authenticationController.getUserData()!!.id
    runBlocking {
      val profileResult = profileRepository.getItem(userId).single()
      if (profileResult.isFailure &&
          profileResult.exceptionOrNull()?.message == "Document does not exist") {
        _uiState.value = _uiState.value.copy(isFirstTime = true)
      } else {
        _uiState.value = _uiState.value.copy(isFirstTime = false)
      }
    }
  }

  data class UiState(
      val hasResult: Boolean = false,
      val success: Boolean = false,
      val isFirstTime: Boolean = false
  )
}
