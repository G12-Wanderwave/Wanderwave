import android.util.Log
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.TrackRepository
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import ch.epfl.cs311.wanderwave.viewmodel.TrackListViewModel
import com.spotify.protocol.types.ListItem
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TrackListViewModelTest {

  private lateinit var viewModel: TrackListViewModel

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK private lateinit var mockSpotifyController: SpotifyController
  @RelaxedMockK private lateinit var repository: TrackRepository

  private val testDispatcher = TestCoroutineDispatcher()
  private lateinit var track: Track

  @OptIn(ExperimentalCoroutinesApi::class)
  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)

    val connectResult = SpotifyController.ConnectResult.SUCCESS
    every { mockSpotifyController.connectRemote() } returns flowOf(connectResult)

    repository = mockk()

    track = Track("spotify:track:1cNf5WAYWuQwGoJyfsHcEF", "Across The Stars", "John Williams")

    val track1 = Track("spotify:track:6ImuyUQYhJKEKFtlrstHCD", "Main Title", "John Williams")
    val track2 =
        Track("spotify:track:0HLQFjnwq0FHpNVxormx60", "The Nightingale", "Percival Schuttenbach")
    val track3 =
        Track("spotify:track:2NZhNbfb1rD1aRj3hZaoqk", "The Imperial Suite", "Michael Giacchino")
    val track4 = Track("spotify:track:5EWPGh7jbTNO2wakv8LjUI", "Free Bird", "Lynyrd Skynyrd")
    val track5 = Track("spotify:track:4rTlPsga6T8yiHGOvZAPhJ", "Godzilla", "Eminem")

    val trackA = Track("spotify:track:5PbMSJZcNA3p2LZv7C56cm", "Yeah", "Queen")
    val trackB = Track("spotify:track:3C7RbG9Co0zjO7CsuEOqRa", "Sing for the Moment", "Eminem")

    val trackList =
        listOf(
            trackA,
            trackB,
            track,
            track1,
            track2,
            track3,
            track4,
            track5,
        )

    every { repository.getAll() } returns flowOf(trackList)

    viewModel = TrackListViewModel(mockSpotifyController, repository)

    runBlocking { viewModel.uiState.first { !it.loading } }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @After
  fun tearDown() {
    Dispatchers.resetMain()
    testDispatcher.cleanupTestCoroutines()
  }

  @Test
  fun collapseTrackList() = run {
    viewModel.collapse()
    assertFalse(viewModel.uiState.value.expanded)
  }

  @Test
  fun expandTrackList() = run {
    viewModel.expand()
    assertTrue(viewModel.uiState.value.expanded)
  }

  @Test
  fun playTrack() = run {
    viewModel.playTrack(track)
    verify { mockSpotifyController.playTrackList(any(), track) }
  }

  @Test
  fun testRetrieveSubsectionAndChildrenFlow() =
      testDispatcher.runBlockingTest {
        val expectedListItem = ListItem("id", "title", null, "subtitle", "", false, true)
        every { mockSpotifyController.getAllElementFromSpotify() } returns
            flowOf(listOf(expectedListItem))
        every {
          mockSpotifyController.getAllChildren(
              ListItem("id", "title", null, "subtitle", "", false, true))
        } returns flowOf(listOf(expectedListItem))

        viewModel.retrieveAndAddSubsection()
        viewModel.retrieveChild(expectedListItem)

        advanceUntilIdle() // This replaces advanceTimeBy and ensures all coroutines are completed

        val result = viewModel.spotifySubsectionList.first() // Safely access the first item
        val result2 = viewModel.childrenPlaylistTrackList.first() // Safely access the first item
        Log.d(
            "TrackListViewModel23",
            "retrieveAndAddSubsection: ${viewModel.spotifySubsectionList.value}")

        assertEquals(listOf(expectedListItem), result)
        assertEquals(listOf(expectedListItem), result2)
      }
}

// for the CI rerun to be removed
