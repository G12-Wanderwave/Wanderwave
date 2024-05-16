package ch.epfl.cs311.wanderwave.viewmodel

import ch.epfl.cs311.wanderwave.model.data.ListType
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.remote.BeaconConnection
import ch.epfl.cs311.wanderwave.model.repository.BeaconRepository
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

  lateinit var viewModel: BeaconViewModel
  val testDispatcher = TestCoroutineDispatcher()
  @RelaxedMockK private lateinit var beaconRepository: BeaconRepository

  @OptIn(ExperimentalCoroutinesApi::class)
  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    viewModel = BeaconViewModel(beaconRepository, mockSpotifyController)
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
    BeaconViewModel(beaconConnection, mockSpotifyController)
  }

  @Test
  fun addTrackToBeaconTest() {
    val viewModel = BeaconViewModel(beaconConnection, mockSpotifyController)
    val track = Track("trackId", "trackName", "trackArtist")
    viewModel.addTrackToBeacon("beaconId", track, {})

    verify { beaconConnection.addTrackToBeacon("beaconId", track, any()) }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun testAddTrackToList() = runBlockingTest {
    val newTrack = Track("Some Track ID", "Track Title", "Artist Name")
    Assert.assertTrue(viewModel.songLists.value.isEmpty())
    viewModel.addTrackToList(ListType.TOP_SONGS, newTrack)
    Assert.assertEquals(1, viewModel.songLists.value.size)
    Assert.assertEquals(newTrack, viewModel.songLists.value[0].tracks[0])
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
}
