package ch.epfl.cs311.wanderwave.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
import ch.epfl.cs311.wanderwave.model.repository.BeaconRepository
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class LoginScreenViewModel
@Inject
constructor(
    private val spotifyController: SpotifyController,
    private val authenticationController: AuthenticationController,
    val beaconConnection: BeaconRepository
) : ViewModel() {

  private var _uiState = MutableStateFlow(UiState())
  val uiState: StateFlow<UiState> = _uiState

  fun getAuthorizationRequest(): AuthorizationRequest {
    return spotifyController.getAuthorizationRequest()
  }

  fun handleAuthorizationResponse(response: AuthorizationResponse) {
    when (response.type) {
      AuthorizationResponse.Type.CODE -> {
        authenticate(response.code)
      }
      AuthorizationResponse.Type.ERROR -> {
        _uiState.value =
            uiState.value.copy(
                hasResult = true, success = false, message = "Error logging in: ${response.error}")
      }
      else -> {
        _uiState.value =
            uiState.value.copy(hasResult = true, success = false, message = "User cancelled login")
      }
    }
  }

  private fun authenticate(authenticationCode: String) {
    viewModelScope.launch {
      authenticationController.authenticate(authenticationCode).collect { success ->
        if (success) {
          _uiState.value = uiState.value.copy(hasResult = true, success = true, message = null)
        } else {
          _uiState.value =
              uiState.value.copy(
                  hasResult = true, success = false, message = "Failed to authenticate")
        }
      }
    }
  }

  data class UiState(
      val hasResult: Boolean = false,
      val success: Boolean = false,
      val message: String? = null
  )
}
