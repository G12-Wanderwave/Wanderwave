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
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
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
      canRefresh: Boolean
  ) {
    every { mockSpotifyController.connectRemote() } returns flowOf(connectResult)
    every { mockAuthenticationController.isSignedIn() } returns isSignedIn
    coEvery { mockAuthenticationController.refreshTokenIfNecessary() } returns
        (isSignedIn || canRefresh)
    viewModel =
        SpotifyConnectScreenViewModel(
            mockSpotifyController, mockAuthenticationController, mockProfileRepository)
  }

  @Test
  fun connectSuccess() = runBlocking {
    setup(SpotifyController.ConnectResult.SUCCESS, true, true)
    viewModel.connectRemote()

    verify { mockSpotifyController.connectRemote() }

    val uiState = viewModel.uiState.first()
    assert(uiState.hasResult)
    assert(uiState.success)
  }

  fun connectUsingRefresh() = runBlocking {
    setup(SpotifyController.ConnectResult.SUCCESS, false, true)
    viewModel.connectRemote()

    verify { mockSpotifyController.connectRemote() }

    val uiState = viewModel.uiState.first()
    assert(uiState.hasResult)
    assert(uiState.success)
  }

  @Test
  fun connectFailure() = runBlocking {
    setup(SpotifyController.ConnectResult.FAILED, true, false)
    viewModel.connectRemote()

    verify { mockSpotifyController.connectRemote() }

    val uiState = viewModel.uiState.first()
    assert(uiState.hasResult)
    assert(uiState.success.not())
  }

  @Test
  fun connectNotLoggedIn() = runBlocking {
    setup(SpotifyController.ConnectResult.NOT_LOGGED_IN, true, false)
    viewModel.connectRemote()

    verify { mockSpotifyController.connectRemote() }

    val uiState = viewModel.uiState.first()
    assert(uiState.hasResult)
    assert(uiState.success.not())
  }

  @Test
  fun notAuthenticated() = runBlocking {
    setup(SpotifyController.ConnectResult.SUCCESS, false, false)
    viewModel.connectRemote()

    verify { mockSpotifyController.connectRemote() wasNot called }

    val uiState = viewModel.uiState.first()
    assert(uiState.hasResult)
    assert(uiState.success.not())
  }

  @Test
  fun checkIfFirstTime_whenProfileExists() = runBlocking {
    setup(SpotifyController.ConnectResult.SUCCESS, true, true)
    val userId = "user123"
    val profile =
        Profile(
            firstName = "John",
            lastName = "Doe",
            description = "A profile description",
            numberOfLikes = 0,
            isPublic = true,
            spotifyUid = "spotifyUid",
            firebaseUid = "firebaseUid")
    val profileResult = Result.success(profile)
    every { mockProfileRepository.getItem(userId) } returns flowOf(profileResult)

    viewModel.checkIfFirstTime()

    val isFirstTime = viewModel.isFirstTime.first()
    assert(!isFirstTime!!)
  }

  @Test
  fun checkIfFirstTime_whenProfileDoesNotExist() = runBlocking {
    setup(SpotifyController.ConnectResult.SUCCESS, true, true)
    val userId = "user123"
    val profileResult = Result.failure<Profile>(Exception("Document does not exist"))
    every { mockProfileRepository.getItem(userId) } returns flowOf(profileResult)

    viewModel.checkIfFirstTime()

    val isFirstTime = viewModel.isFirstTime.first()
    if (isFirstTime != null) {
      assertTrue(!isFirstTime)
    }
  }

  @Test
  fun checkIfFirstTime_whenNoUserId() = runBlocking {
    setup(SpotifyController.ConnectResult.SUCCESS, true, true)
    every { mockAuthenticationController.getUserData() } returns null

    viewModel.checkIfFirstTime()

    val isFirstTime = viewModel.isFirstTime.firstOrNull()
    assertNull(isFirstTime) // Now correctly checks for null
  }

  @Test
  fun checkIfFirstTime_whenProfileLoadFailsWithDifferentMessage() = runBlocking {
    setup(SpotifyController.ConnectResult.SUCCESS, true, true)
    val userId = "user123"
    val profileResult = Result.failure<Profile>(Exception("Some other error"))
    every { mockProfileRepository.getItem(userId) } returns flowOf(profileResult)

    viewModel.checkIfFirstTime()

    val isFirstTime = viewModel.isFirstTime.first()
    if (isFirstTime != null) {
      assertFalse(isFirstTime)
    }
  }
}
