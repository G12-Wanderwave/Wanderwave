package ch.epfl.cs311.wanderwave.viewmodel

import androidx.lifecycle.ViewModel
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import com.spotify.sdk.android.auth.AuthorizationRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.first

@HiltViewModel
class LoginScreenViewModel @Inject constructor(private val spotifyController: SpotifyController) :
    ViewModel() {

  fun getAuthorizationRequest(): AuthorizationRequest {
    return spotifyController.getAuthorizationRequest()
  }

  suspend fun handleTokenResponse(token: String, expiresIn: Int) {
    val connectResult = spotifyController.connectRemote().first()
    if (connectResult == SpotifyController.ConnectResult.SUCCESS) {
      spotifyController.appRemote?.playerApi?.play("spotify:track:4PTG3Z6ehGkBFwjybzWkR8")
    } else {
      println("Failed to connect")
    }
  }
}
