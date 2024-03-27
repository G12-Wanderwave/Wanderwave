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
class LoginScreenViewModel @Inject constructor(private val spotifyController: SpotifyController) :
    ViewModel() {

  private var _uiState = MutableStateFlow(LoginScreenUiState())
  val uiState: StateFlow<LoginScreenUiState> = _uiState

  fun getAuthorizationRequest(): AuthorizationRequest {
    return spotifyController.getAuthorizationRequest()
  }

  fun handleAuthorizationResponse(response: AuthorizationResponse) {
    when (response.type) {
      AuthorizationResponse.Type.TOKEN -> {
        _uiState.value =
            LoginScreenUiState(
                hasResult = true,
                success = true,
                message =
                    "Logged in with token: ${response.accessToken}, expiresIn: ${response.expiresIn}")
      }
      AuthorizationResponse.Type.ERROR -> {
        _uiState.value =
            LoginScreenUiState(
                hasResult = true, success = false, message = "Error logging in: ${response.error}")
      }
      else -> {
        _uiState.value =
            LoginScreenUiState(hasResult = true, success = false, message = "User cancelled login")
      }
    }
  }

  data class LoginScreenUiState(
      val hasResult: Boolean = false,
      val success: Boolean = false,
      val message: String? = null
  )
}
