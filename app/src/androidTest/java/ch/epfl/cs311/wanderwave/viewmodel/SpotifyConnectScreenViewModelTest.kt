package ch.epfl.cs311.wanderwave.viewmodel

import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
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

  @get:Rule
  val mockkRule = MockKRule(this)

  @RelaxedMockK
  private lateinit var mockSpotifyController: SpotifyController

  fun setup(connectResult: SpotifyController.ConnectResult) {
    every { mockSpotifyController.connectRemote() } returns flowOf(connectResult)
  }

  @Test
  fun connectSuccess() = runBlocking {
    setup(SpotifyController.ConnectResult.SUCCESS)
    val viewModel = SpotifyConnectScreenViewModel(mockSpotifyController)
    viewModel.connectRemote()

    verify { mockSpotifyController.connectRemote() }

    val uiState = viewModel.uiState.first()
    assert(uiState.hasResult)
    assert(uiState.success)
  }

  @Test
  fun connectFailure() = runBlocking {
    setup(SpotifyController.ConnectResult.FAILED)
    val viewModel = SpotifyConnectScreenViewModel(mockSpotifyController)
    viewModel.connectRemote()

    verify { mockSpotifyController.connectRemote() }

    val uiState = viewModel.uiState.first()
    assert(uiState.hasResult)
    assert(uiState.success.not())
  }

  @Test
  fun connectNotLoggedIn() = runBlocking {
    setup(SpotifyController.ConnectResult.NOT_LOGGED_IN)
    val viewModel = SpotifyConnectScreenViewModel(mockSpotifyController)
    viewModel.connectRemote()

    verify { mockSpotifyController.connectRemote() }

    val uiState = viewModel.uiState.first()
    assert(uiState.hasResult)
    assert(uiState.success.not())
  }
}