package ch.epfl.cs311.wanderwave.viewmodel

import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationUserData
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.data.ProfileTrackAssociation
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.BeaconRepository
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepository
import ch.epfl.cs311.wanderwave.model.repository.TrackRepository
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class BeaconScreenViewModelTest {

  @get:Rule val mockkRule = MockKRule(this)
  @RelaxedMockK private lateinit var mockSpotifyController: SpotifyController
  @RelaxedMockK private lateinit var mockAuthenticationController: AuthenticationController
  @RelaxedMockK private lateinit var mockProfileRepository: ProfileRepository

  lateinit var viewModel: BeaconViewModel
  val testDispatcher = TestCoroutineDispatcher()
  @RelaxedMockK private lateinit var beaconRepository: BeaconRepository
  @RelaxedMockK private lateinit var trackRepository: TrackRepository

  @OptIn(ExperimentalCoroutinesApi::class)
  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)

    every { mockAuthenticationController.isSignedIn() } returns true
    every { mockAuthenticationController.getUserData() } returns
        AuthenticationUserData("uid", "email", "name", "http://photoUrl/img.jpg")

    val bannedSongs = listOf(Track("bannedid", "banned title", "banned artist"))
    val mockProfile = mockk<Profile>()
    every { mockProfileRepository.getItem(any()) } returns flowOf(Result.success(mockProfile))
    every { mockProfile.bannedSongs } returns bannedSongs

    every { beaconRepository.getItem(any()) } returns
        flowOf(
            Result.success(
                mockk<Beacon> {
                  every { profileAndTrack } returns
                      listOf(
                          ProfileTrackAssociation(
                              Profile(
                                  "Sample First Name",
                                  "Sample last name",
                                  "Sample desc",
                                  0,
                                  false,
                                  null,
                                  "Sample Profile ID",
                                  "Sample Track ID"),
                              Track("Sample Track ID", "Sample Track Title", "Sample Artist Name")),
                          ProfileTrackAssociation(
                              Profile(
                                  "Sample First Name",
                                  "Sample last name",
                                  "Sample desc",
                                  0,
                                  false,
                                  null,
                                  "Sample Profile ID",
                                  "Sample Track ID"),
                              Track("bannedid", "banned title", "banned artist")))
                }))

    viewModel =
        BeaconViewModel(
            trackRepository,
            beaconRepository,
            mockProfileRepository,
            mockSpotifyController,
            mockAuthenticationController)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @After
  fun tearDown() {
    try {
      testDispatcher.cleanupTestCoroutines()
    } finally {
      Dispatchers.resetMain() // Always reset the dispatcher
    }
  }

  @After
  fun clearMocks() {
    clearAllMocks() // Clear all MockK mocks
  }

  @Test
  fun canConstructWithNoErrors() {
    val connectResult = SpotifyController.ConnectResult.SUCCESS
    every { mockSpotifyController.connectRemote() } returns flowOf(connectResult)
    BeaconViewModel(
        trackRepository,
        beaconRepository,
        mockProfileRepository,
        mockSpotifyController,
        mockAuthenticationController)
  }

  @Test
  fun addTrackToBeaconTest() {
    val viewModel =
        BeaconViewModel(
            trackRepository,
            beaconRepository,
            mockProfileRepository,
            mockSpotifyController,
            mockAuthenticationController)
    val track = Track("trackId", "trackName", "trackArtist")
    viewModel.addTrackToBeacon("beaconId", track, {})

    verify { beaconRepository.addTrackToBeacon(any(), any(), any(), any()) }
  }

  @Test fun testGetLikedTracks() = runBlocking { viewModel.getLikedTracks() } // Test no crash

  @Test
  fun canSelectTracks() {
    val viewModel =
        BeaconViewModel(
            trackRepository,
            beaconRepository,
            mockProfileRepository,
            mockSpotifyController,
            mockAuthenticationController)
    val track = Track("trackId", "trackName", "trackArtist")

    viewModel.getBeaconById("beaconId")
    println("beacon: ${viewModel.uiState.value.beacon}")
    viewModel.selectTrack(track)

    verify { mockSpotifyController.playTrackList(any(), any(), any()) }
  }

  @Test
  fun addTrackToList_addsTrack_whenSuccessful() = runBlockingTest {
    // Mocking
    val track = Track("Sample Track ID", "Sample Track Title", "Sample Artist Name")
    val beaconId = "Sample Beacon ID"
    coEvery { beaconRepository.addTrackToBeacon(beaconId, track, any(), any()) } answers
        {
          lastArg<(Boolean) -> Unit>().invoke(true)
        }

    // Act
    viewModel.addTrackToList(track)

    // Assert
    assertEquals(track, viewModel.uiState.value.beacon?.profileAndTrack?.first()?.track)

    viewModel.clearLikedSongs()

    assertTrue(viewModel.likedSongsTrackList.value.isEmpty())
  }

  @Test
  fun testGetBeaconById() = runBlocking {
    // Arrange
    val id = "beaconId"
    val expectedBeacon =
        Beacon(
            id,
            Location(46.519653, 6.632273, "Lausanne"),
            profileAndTrack =
                listOf(
                    ProfileTrackAssociation(
                        Profile(
                            "Sample First Name",
                            "Sample last name",
                            "Sample desc",
                            0,
                            false,
                            null,
                            "Sample Profile ID",
                            "Sample Track ID"),
                        Track("Sample Track ID", "Sample Track Title", "Sample Artist Name"))))
    val beaconFlow = flowOf(Result.success(expectedBeacon))

    every { beaconRepository.getItem(id) } returns beaconFlow

    // Act
    viewModel.getBeaconById(id)

    // Assert
    val uiState = viewModel.uiState.value
    assertEquals(expectedBeacon, uiState.beacon)
    assertFalse(uiState.isLoading)
    assertNull(uiState.error)

    // fail case
    val beaconFlowError = flowOf(Result.failure<Beacon>(Exception("Test Exception")))
    every { beaconRepository.getItem(id) } returns beaconFlowError

    viewModel.getBeaconById(id)

    val uiStateError = viewModel.uiState.value
    assertNull(uiStateError.beacon)
    assertFalse(uiStateError.isLoading)
    assertEquals("Test Exception", uiStateError.error)
  }

  @Test
  fun testGetNbrLikedTracks() = runBlocking { viewModel.getTotalLikedTracks() } // Test no crash

  @Test
  fun bannedSongsAreAddedToUiState() = runBlocking {
    viewModel.getBeaconById("beaconId")

    assert(viewModel.uiState.value.bannedTracks.size == 1)
  }
}
