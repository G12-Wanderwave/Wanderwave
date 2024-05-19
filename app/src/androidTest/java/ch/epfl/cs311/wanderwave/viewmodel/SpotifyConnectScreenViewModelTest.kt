package ch.epfl.cs311.wanderwave.viewmodel

import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepository
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import io.mockk.called
import io.mockk.coEvery
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
  @RelaxedMockK private lateinit var mockProfileRepository: ProfileRepository

  private lateinit var viewModel: SpotifyConnectScreenViewModel

  fun setup(
      connectResult: SpotifyController.ConnectResult,
      isSignedIn: Boolean,
      canRefresh: Boolean,
      profileExists: Boolean
  ) {
    every { mockSpotifyController.connectRemote() } returns flowOf(connectResult)
    every { mockAuthenticationController.isSignedIn() } returns isSignedIn
    coEvery { mockAuthenticationController.refreshTokenIfNecessary() } returns
        (isSignedIn || canRefresh)

    val profileFlow =
        if (profileExists) {
          flowOf(
              Result.success(
                  Profile(
                      firstName = "John",
                      lastName = "Doe",
                      description = "Test description",
                      numberOfLikes = 0,
                      isPublic = true,
                      spotifyUid = "spotifyUid",
                      firebaseUid = "firebaseUid")))
        } else {
          flowOf(Result.failure(Exception("Document does not exist")))
        }
    every { mockProfileRepository.getItem(any()) } returns profileFlow
    viewModel =
        SpotifyConnectScreenViewModel(
            mockSpotifyController, mockAuthenticationController, mockProfileRepository)
  }

  @Test
  fun connectSuccess() = runBlocking {
    setup(SpotifyController.ConnectResult.SUCCESS, true, true, true)
    viewModel.connectRemote()

    verify { mockSpotifyController.connectRemote() }

    val uiState = viewModel.uiState.first()
    assert(uiState.hasResult)
    assert(uiState.success)
  }

  fun connectUsingRefresh() = runBlocking {
    setup(SpotifyController.ConnectResult.SUCCESS, false, true, true)
    viewModel.connectRemote()

    verify { mockSpotifyController.connectRemote() }

    val uiState = viewModel.uiState.first()
    assert(uiState.hasResult)
    assert(uiState.success)
  }

  @Test
  fun connectFailure() = runBlocking {
    setup(SpotifyController.ConnectResult.FAILED, true, false, true)
    viewModel.connectRemote()

    verify { mockSpotifyController.connectRemote() }

    val uiState = viewModel.uiState.first()
    assert(uiState.hasResult)
    assert(uiState.success.not())
  }

  @Test
  fun connectNotLoggedIn() = runBlocking {
    setup(SpotifyController.ConnectResult.NOT_LOGGED_IN, true, false, true)
    viewModel.connectRemote()

    verify { mockSpotifyController.connectRemote() }

    val uiState = viewModel.uiState.first()
    assert(uiState.hasResult)
    assert(uiState.success.not())
  }

  @Test
  fun notAuthenticated() = runBlocking {
    setup(SpotifyController.ConnectResult.SUCCESS, false, false, true)
    viewModel.connectRemote()

    verify { mockSpotifyController.connectRemote() wasNot called }

    val uiState = viewModel.uiState.first()
    assert(uiState.hasResult)
    assert(uiState.success.not())
  }

  @Test
  fun checkProfileFirstTime() = runBlocking {
    setup(SpotifyController.ConnectResult.SUCCESS, true, true, false)
    viewModel.checkProfile()

    val uiState = viewModel.uiState.first()
    assert(uiState.isFirstTime)
  }

  @Test
  fun checkProfileReturningUser() = runBlocking {
    setup(SpotifyController.ConnectResult.SUCCESS, true, true, true)
    viewModel.checkProfile()

    val uiState = viewModel.uiState.first()
    assert(!uiState.isFirstTime)
  }
}
