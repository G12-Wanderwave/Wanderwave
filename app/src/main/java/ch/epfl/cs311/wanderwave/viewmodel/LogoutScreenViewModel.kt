package ch.epfl.cs311.wanderwave.viewmodel

import androidx.lifecycle.ViewModel
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class LogoutScreenViewModel @Inject constructor(private val spotifyController: SpotifyController) :
    ViewModel() {

  private var _uiState = MutableStateFlow(UiState())
  val uiState: StateFlow<UiState> = _uiState

  fun getAuthorizationRequest(): AuthorizationRequest {
    return spotifyController.getLogoutRequest()
  }

  fun handleAuthorizationResponse(response: AuthorizationResponse) {
    when (response.type) {
      AuthorizationResponse.Type.TOKEN -> {
        _uiState.value = UiState(hasResult = true, success = true)
      }
      AuthorizationResponse.Type.ERROR -> {
        _uiState.value =
            UiState(
                hasResult = true, success = false, message = "Error logging out: ${response.error}")
      }
      else -> {
        _uiState.value =
            UiState(hasResult = true, success = false, message = "User cancelled logout")
      }
    }
  }

  data class UiState(
      val hasResult: Boolean = false,
      val success: Boolean = false,
      val message: String? = null
  )
}
