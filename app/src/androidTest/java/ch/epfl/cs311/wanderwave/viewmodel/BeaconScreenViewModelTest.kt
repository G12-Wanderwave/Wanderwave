package ch.epfl.cs311.wanderwave.viewmodel

import ch.epfl.cs311.wanderwave.model.data.ListType
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.remote.BeaconConnection
import ch.epfl.cs311.wanderwave.model.remote.ProfileConnection
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import com.spotify.protocol.types.ListItem
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.verify
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.timeout
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

  lateinit var viewModel: ProfileViewModel
  val testDispatcher = TestCoroutineDispatcher()
  @RelaxedMockK private lateinit var profileRepository: ProfileConnection

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    viewModel = ProfileViewModel(profileRepository, mockSpotifyController)
  }

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

  @Test
  fun testAddTrackToList() = runBlockingTest {
    // Define a new track
    val newTrack = Track("Some Track ID", "Track Title", "Artist Name")

    // Ensure song lists are initially empty
    Assert.assertTrue(viewModel.songLists.value.isEmpty())

    // Call createSpecificSongList to initialize a list
    viewModel.createSpecificSongList(ListType.TOP_SONGS)

    // Add track to "TOP SONGS"
    viewModel.addTrackToList(ListType.TOP_SONGS, newTrack)

    // Get the updated song list
    val songLists = viewModel.songLists.value
    Assert.assertFalse("Song list should not be empty after adding a track", songLists.isEmpty())

    // Check if the track was added correctly
    val songsInList = songLists.find { it.name == ListType.TOP_SONGS }?.tracks ?: emptyList()
    Assert.assertTrue(
        "Song list should contain the newly added track", songsInList.contains(newTrack))
  }

  @Test
  fun testGetAllChildrenFlow() = runBlockingTest {
    val expectedListItem = ListItem("id", "title", null, "subtitle", "", false, true)
    every { mockSpotifyController.getAllChildren(expectedListItem) } returns
        flowOf(listOf(expectedListItem))

    val result = mockSpotifyController.getAllChildren(expectedListItem)
    Assert.assertEquals(
        expectedListItem, result.first().get(0)) // Check if the first item is as expected
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
}
