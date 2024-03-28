package ch.epfl.cs311.wanderwave.viewmodel

import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import com.spotify.sdk.android.auth.AuthorizationResponse
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class LogoutScreenViewModelTest {

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK private lateinit var mockSpotifyController: SpotifyController

  @Before
  fun setup() {
    every { mockSpotifyController.getLogoutRequest() } returns mockk()
  }

  @Test
  fun getAuthorizationRequest() {
    val viewModel = LogoutScreenViewModel(mockSpotifyController)
    val request = viewModel.getAuthorizationRequest()
    verify { mockSpotifyController.getLogoutRequest() }
  }

  @Test
  fun logoutSuccess() = runBlocking {
    val viewModel = LogoutScreenViewModel(mockSpotifyController)

    val response = mockk<AuthorizationResponse>()
    every { response.type } returns AuthorizationResponse.Type.TOKEN

    viewModel.handleAuthorizationResponse(response)

    val state = viewModel.uiState.first()
    assert(state.hasResult)
    assert(state.success)
  }

  @Test
  fun logoutFailure() = runBlocking {
    val viewModel = LogoutScreenViewModel(mockSpotifyController)

    val errorMsg = "logout error message"
    val response = mockk<AuthorizationResponse>()
    every { response.type } returns AuthorizationResponse.Type.ERROR
    every { response.error } returns errorMsg

    viewModel.handleAuthorizationResponse(response)

    val state = viewModel.uiState.first()
    assert(state.hasResult)
    assert(state.success.not())
    assert(state.message?.contains(errorMsg) ?: false)
  }

  @Test
  fun logoutUnknown() = runBlocking {
    val viewModel = LogoutScreenViewModel(mockSpotifyController)

    val errorMsg = "logout unknown message"
    val response = mockk<AuthorizationResponse>()
    every { response.type } returns AuthorizationResponse.Type.UNKNOWN
    every { response.error } returns errorMsg

    viewModel.handleAuthorizationResponse(response)

    val state = viewModel.uiState.first()
    assert(state.hasResult)
    assert(state.success.not())
  }
}
