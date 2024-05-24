package ch.epfl.cs311.wanderwave.viewmodel

import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationUserData
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.ListType
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.data.ProfileTrackAssociation
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.remote.BeaconConnection
import ch.epfl.cs311.wanderwave.model.repository.BeaconRepository
import ch.epfl.cs311.wanderwave.model.repository.TrackRepository
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import com.spotify.protocol.types.ListItem
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.verify
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class BeaconScreenViewModelTest {

  @get:Rule val mockkRule = MockKRule(this)
  @RelaxedMockK private lateinit var beaconConnection: BeaconConnection
  @RelaxedMockK private lateinit var mockSpotifyController: SpotifyController
  @RelaxedMockK private lateinit var mockAuthenticationController: AuthenticationController

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

    viewModel =
        BeaconViewModel(
            trackRepository, beaconRepository, mockSpotifyController, mockAuthenticationController)
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
        trackRepository, beaconConnection, mockSpotifyController, mockAuthenticationController)
  }

  @Test
  fun testUpdateBeacon() = runBlocking {
    val beacon = Beacon("beaconId", Location(0.0, 0.0, "Lausanne"))
    viewModel.updateBeacon(beacon)
    verify { beaconRepository.updateItem(beacon) }
  }

  @Test
  fun addTrackToBeaconTest() {
    val viewModel =
        BeaconViewModel(
            trackRepository, beaconConnection, mockSpotifyController, mockAuthenticationController)
    val track = Track("trackId", "trackName", "trackArtist")
    viewModel.addTrackToBeacon("beaconId", track, {})

    verify { beaconConnection.addTrackToBeacon(any(), any(), any(), any()) }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun testAddTrackToList() = runBlockingTest {
    val newTrack = Track("Some Track ID", "Track Title", "Artist Name")
    Assert.assertTrue(viewModel.songLists.value.isEmpty())
    viewModel.addTrackToList(ListType.TOP_SONGS, newTrack)
    Assert.assertEquals(0, viewModel.songLists.value.size)
  }

  @Test
  fun testRetrieveSubsectionAndChildrenFlow() = runBlockingTest {
    val expectedListItem = ListItem("id", "title", null, "subtitle", "", false, true)
    every { mockSpotifyController.getAllElementFromSpotify() } returns
        flowOf(listOf(expectedListItem))
    every {
      mockSpotifyController.getAllChildren(
          ListItem("id", "title", null, "subtitle", "", false, true))
    } returns flowOf(listOf(expectedListItem))
    viewModel.retrieveAndAddSubsection()
    viewModel.retrieveChild(expectedListItem)
    advanceUntilIdle() // Ensure all coroutines are completed

    // val result = viewModel.spotifySubsectionList.first()  // Safely access the first item
    val flow = viewModel.spotifySubsectionList
    val flow2 = viewModel.childrenPlaylistTrackList
    val result = flow.timeout(2.seconds).catch {}.firstOrNull()
    val result2 = flow2.timeout(2.seconds).catch {}.firstOrNull()

    Assert.assertEquals(expectedListItem, result?.get(0))
    Assert.assertEquals(expectedListItem, result2?.get(0))
  }

  @Test fun testGetLikedTracks() = runBlocking { viewModel.getLikedTracks() }

  @Test
  fun testGetTracksFromPlaylist() = runBlocking { viewModel.getTracksFromPlaylist("playlistId") }

  @Test
  fun testChangeChosenSongs() = runBlocking {
    val t = viewModel.isTopSongsListVisible.value
    viewModel.changeChosenSongs()
    assertNotEquals(t, viewModel.isTopSongsListVisible.value)
  }

  @Test
  fun canSelectTracks() {
    val viewModel =
        BeaconViewModel(
            trackRepository, beaconConnection, mockSpotifyController, mockAuthenticationController)
    val track = Track("trackId", "trackName", "trackArtist")
    viewModel.selectTrack(track)

    verify { mockSpotifyController.playTrackList(any(), any(), any()) }
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
  fun emptyChildrenList_clearsChildrenPlaylistTrackList() = runBlockingTest {

    // Act
    viewModel.emptyChildrenList()
  }
}
