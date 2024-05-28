package ch.epfl.cs311.wanderwave.viewmodel

import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.remote.ProfileConnection
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import com.spotify.protocol.types.ListItem
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class ProfileViewModelTest {

  lateinit var viewModel: ProfileViewModel
  val testDispatcher = TestCoroutineDispatcher()
  @get:Rule val mockkRule = MockKRule(this)
  @RelaxedMockK private lateinit var profileRepository: ProfileConnection

  @RelaxedMockK private lateinit var spotifyController: SpotifyController

  @RelaxedMockK private lateinit var authenticationController: AuthenticationController

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    viewModel = ProfileViewModel(profileRepository, spotifyController, authenticationController)
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
  fun testLikeTrack() = runBlockingTest {
    val track = Track("id", "title", "artist")
    val track2 = Track("id2", "title2", "artist2")
    assertTrue(viewModel.wanderwaveLikedTracks.value.isEmpty())
    viewModel.likeTrack(track)
    viewModel.likeTrack(track2)
    assertFalse(viewModel.wanderwaveLikedTracks.value.isEmpty())
    viewModel.likeTrack(track)
    viewModel.likeTrack(track2)
    assertFalse(viewModel.wanderwaveLikedTracks.value.isEmpty())
  }

  @Test
  fun testUnlikeTrack() = runBlockingTest {
    val track = Track("id", "title", "artist")
    val track2 = Track("id2", "title2", "artist2")
    viewModel.likeTrack(track)
    viewModel.likeTrack(track2)
    assertFalse(viewModel.wanderwaveLikedTracks.value.isEmpty())
    viewModel.unlikeTrack(track)
    viewModel.unlikeTrack(track2)
    assertTrue(viewModel.wanderwaveLikedTracks.value.isEmpty())
    viewModel.unlikeTrack(track)
    viewModel.unlikeTrack(track2)
    assertTrue(viewModel.wanderwaveLikedTracks.value.isEmpty())
  }

  @Test
  fun testAddTrackToList() = runBlockingTest {
    // Define a new track
    val newTrack = Track("Some Track ID", "Track Title", "Artist Name")
    val expectedTrack = Track("spotify:track:Some Track ID", "Track Title", "Artist Name")

    // Ensure song lists are initially empty
    assertTrue(viewModel.songLists.value.isEmpty())

    // Call createSpecificSongList to initialize a list

    // Add track to "TOP SONGS"
    viewModel.addTrackToList(newTrack)

    // Get the updated song list
    val songLists = viewModel.songLists.value
    assertFalse("Song list should not be empty after adding a track", songLists.isEmpty())

    // Check if the track was added correctly

    assertTrue("Song list should contain the newly added track", songLists.contains(expectedTrack))
  }

  @Test
  fun testGetAllChildrenFlow() = runBlockingTest {
    val expectedListItem = ListItem("id", "title", null, "subtitle", "", false, true)
    every { spotifyController.getAllChildren(expectedListItem) } returns
        flowOf(listOf(expectedListItem))

    val result = spotifyController.getAllChildren(expectedListItem)
    assertEquals(expectedListItem, result.first().get(0)) // Check if the first item is as expected
  }

  @Test
  fun addTrackToList_addsNewTrack_whenTrackIdDoesNotContainSpotifyPrefix() = runBlockingTest {
    // Arrange
    val trackWithoutSpotifyPrefix = Track("123", "title", "artist")
    val expectedTrackWithSpotifyPrefix = Track("spotify:track:123", "title", "artist")

    // Act
    viewModel.addTrackToList(trackWithoutSpotifyPrefix)

    // Assert
    assertTrue(viewModel.songLists.value.contains(expectedTrackWithSpotifyPrefix))
    // Act
    viewModel.clearLikedSongs()

    // Assert
    assertTrue(viewModel.likedSongsTrackList.value.isEmpty())
  }

  @Test
  fun addTrackToList_addsExistingTrack_whenTrackIdContainsSpotifyPrefix() = runBlockingTest {
    // Arrange
    val trackWithSpotifyPrefix = Track("spotify:track:123", "title", "artist")

    // Act
    viewModel.addTrackToList(trackWithSpotifyPrefix)

    // Assert
    assertTrue(viewModel.songLists.value.contains(trackWithSpotifyPrefix))
  }

  @Test fun testGetLikedTracks() = runBlocking { viewModel.getLikedTracks() } // Test no crash

  @Test
  fun addTrackToList_doesNotAddDuplicateTrack() = runBlockingTest {
    // Arrange
    val track = Track("spotify:track:123", "title", "artist")

    // Act
    viewModel.addTrackToList(track)
    viewModel.addTrackToList(track)

    // Assert
    assertEquals(1, viewModel.songLists.value.count { it == track })
  }

  @Test
  fun testGetProfileByID() = runBlocking {
    // Arrange
    val testId = "testId"
    val testProfile =
        Profile(
            firstName = "Test",
            lastName = "User",
            description = "Test Description",
            numberOfLikes = 0,
            isPublic = true,
            spotifyUid = "Test Spotify UID",
            firebaseUid = "Test Firebase UID",
            profilePictureUri = null)
    val testFlow = flowOf(Result.success(testProfile))

    every { profileRepository.getItem(testId) } returns testFlow

    // Act
    viewModel.getProfileByID(testId, false)

    // Assert
    assertEquals(testProfile, viewModel.profile.value)
    assertEquals(
        ProfileViewModel.UIState(profile = testProfile, isLoading = false), viewModel.uiState.value)

    // failure case
    val testFlowError = flowOf(Result.failure<Profile>(Exception("Test Exception")))
    every { profileRepository.getItem(testId) } returns testFlowError

    viewModel.getProfileByID(testId, false)
    assertEquals(
        ProfileViewModel.UIState(profile = null, isLoading = false, error = "Test Exception"),
        viewModel.uiState.value)
  }

  @Test
  fun testCreateProfile() = runBlockingTest {
    every { profileRepository.getItem(any()) } returns
        flowOf(Result.failure(Exception("Document does not exist")))

    viewModel.getProfileByID("firebaseUid", true)

    verify { profileRepository.addItemWithId(any()) }

    viewModel.getProfileByID("firebaseUid", false)
  }

  @Test
  fun testGetNbrLikedTracks() = runBlocking { viewModel.getTotalLikedTracks() } // Test no crash
}
