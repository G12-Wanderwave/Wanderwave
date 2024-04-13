package ch.epfl.cs311.wanderwave.viewmodel

import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepositoryImpl
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import com.spotify.protocol.types.ListItem
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
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

  @get:Rule val mockkRule = MockKRule(this)
  @RelaxedMockK private lateinit var profileRepositoryImpl: ProfileRepositoryImpl

  @RelaxedMockK private lateinit var spotifyController: SpotifyController

  @Before
  fun setup() {
    viewModel = ProfileViewModel(profileRepositoryImpl, spotifyController)
  }

  @Test
  fun testAddTrackToList() = runBlockingTest {
    // Define a new track
    val newTrack = Track("Some Track ID", "Track Title", "Artist Name")

    // Ensure song lists are initially empty
    assertTrue(viewModel.songLists.value.isEmpty())

    // Call createSpecificSongList to initialize a list
    viewModel.createSpecificSongList("TOP_SONGS")

    // Add track to "TOP SONGS"
    viewModel.addTrackToList("TOP SONGS", newTrack)

    // Get the updated song list
    val songLists = viewModel.songLists.value
    assertFalse("Song list should not be empty after adding a track", songLists.isEmpty())

    // Check if the track was added correctly
    val songsInList = songLists.find { it.name == "TOP SONGS" }?.tracks ?: emptyList()
    assertTrue("Song list should contain the newly added track", songsInList.contains(newTrack))
  }

  @Test
  fun retrieveTopTrack_doesNotAddTrackToTopSongs_whenTrackHasNoChildren() = runBlockingTest {
    val track = ListItem("id", "", null, "", "", false, false)
    every { spotifyController.getTrack() } returns flowOf(track)

    viewModel.retrieveTopTrack()

    val topSongs = viewModel.songLists.value.find { it.name == "TOP SONGS" }
    assertTrue(topSongs?.tracks?.isEmpty() ?: true)
  }

  @Test
  fun retrieveTopTrack_doesNotAddTrackToTopSongs_whenTrackHasChildren() = runBlockingTest {
    val track = ListItem("id", "", null, "", "", false, true)
    every { spotifyController.getTrack() } returns flowOf(track)

    viewModel.retrieveTopTrack()

    val topSongs = viewModel.songLists.value.find { it.name == "TOP SONGS" }
    assertTrue(topSongs?.tracks?.isEmpty() ?: true)
  }

  @Test
  fun retrieveTopTrack_doesNotAddTrackToTopSongs_whenTrackIdIsEmpty() = runBlockingTest {
    val track = ListItem("", "", null, "", "", false, false)
    every { spotifyController.getTrack() } returns flowOf(track)

    viewModel.retrieveTopTrack()

    val topSongs = viewModel.songLists.value.find { it.name == "TOP SONGS" }
    assertTrue(topSongs?.tracks?.isEmpty() ?: true)
  }
}
