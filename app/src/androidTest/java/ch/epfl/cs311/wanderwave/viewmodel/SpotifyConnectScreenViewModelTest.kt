package ch.epfl.cs311.wanderwave.viewmodel

import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import io.mockk.called
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class SpotifyConnectScreenViewModelTest {

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK private lateinit var mockSpotifyController: SpotifyController

  @RelaxedMockK private lateinit var mockAuthenticationController: AuthenticationController

  private lateinit var viewModel: SpotifyConnectScreenViewModel

  fun setup(connectResult: SpotifyController.ConnectResult, isSignedIn: Boolean) {
    every { mockSpotifyController.connectRemote() } returns flowOf(connectResult)
    every { mockAuthenticationController.isSignedIn() } returns isSignedIn
    viewModel = SpotifyConnectScreenViewModel(mockSpotifyController, mockAuthenticationController)
  }

  @Test
  fun connectSuccess() = runBlocking {
    setup(SpotifyController.ConnectResult.SUCCESS, true)
    viewModel.connectRemote()

    verify { mockSpotifyController.connectRemote() }

    val uiState = viewModel.uiState.first()
    assert(uiState.hasResult)
    assert(uiState.success)
  }

  @Test
  fun connectFailure() = runBlocking {
    setup(SpotifyController.ConnectResult.FAILED, true)
    viewModel.connectRemote()

    verify { mockSpotifyController.connectRemote() }

    val uiState = viewModel.uiState.first()
    assert(uiState.hasResult)
    assert(uiState.success.not())
  }

  @Test
  fun connectNotLoggedIn() = runBlocking {
    setup(SpotifyController.ConnectResult.NOT_LOGGED_IN, true)
    viewModel.connectRemote()

    verify { mockSpotifyController.connectRemote() }

    val uiState = viewModel.uiState.first()
    assert(uiState.hasResult)
    assert(uiState.success.not())
  }

  @Test
  fun notAuthenticated() = runBlocking {
    setup(SpotifyController.ConnectResult.SUCCESS, false)
    viewModel.connectRemote()

    verify { mockSpotifyController.connectRemote() wasNot called}

    val uiState = viewModel.uiState.first()
    assert(uiState.hasResult)
    assert(uiState.success.not())
  }
}
