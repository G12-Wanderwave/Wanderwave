import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.TrackRepositoryImpl
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import ch.epfl.cs311.wanderwave.viewmodel.TrackListViewModel
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
import kotlinx.coroutines.test.runTest
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
  @RelaxedMockK private lateinit var repository: TrackRepositoryImpl

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
    val track2 = Track("spotify:track:0HLQFjnwq0FHpNVxormx60", "The Nightingale", "Percival Schuttenbach")
    val track3 = Track("spotify:track:2NZhNbfb1rD1aRj3hZaoqk", "The Imperial Suite", "Michael Giacchino")
    val track4 = Track("spotify:track:5EWPGh7jbTNO2wakv8LjUI", "Free Bird", "Lynyrd Skynyrd")
    val track5 = Track("spotify:track:4rTlPsga6T8yiHGOvZAPhJ", "Godzilla", "Eminem")

    val trackA = Track("spotify:track:5PbMSJZcNA3p2LZv7C56cm", "Yeah", "Queen")
    val trackB = Track("spotify:track:3C7RbG9Co0zjO7CsuEOqRa","Sing for the Moment", "Eminem")

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

    viewModel = TrackListViewModel(repository, mockSpotifyController)

    runBlocking { viewModel.uiState.first { !it.loading } }
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    testDispatcher.cleanupTestCoroutines()
  }

  @Test
  fun trackIsProperlySelected() = run {
    viewModel.selectTrack(track)
    assertEquals(track.id, viewModel.uiState.value.selectedTrack?.id)
  }

  @Test
  fun songPlaysProperly() = run {
    assertFalse(viewModel.uiState.value.isPlaying)

    viewModel.selectTrack(track)
    viewModel.play()

    assertTrue(viewModel.uiState.value.isPlaying)
    assertEquals(track.id, viewModel.uiState.value.selectedTrack?.id)
  }

  @Test
  fun songPausesProperly() = run {
    viewModel.selectTrack(track)
    viewModel.play()
    assertTrue(viewModel.uiState.value.isPlaying)

    viewModel.pause()
    assertFalse(viewModel.uiState.value.isPlaying)
    assertEquals(track.id, viewModel.uiState.value.pausedTrack?.id)
  }

  @Test
  fun songResumesProperly() = run {
    viewModel.selectTrack(track)
    viewModel.play()
    viewModel.pause()
    assertFalse(viewModel.uiState.value.isPlaying)

    viewModel.play()
    assertTrue(viewModel.uiState.value.isPlaying)
    assertEquals(track.id, viewModel.uiState.value.selectedTrack?.id)
  }

  @Test
  fun songDoesntPlayWhenNull() = run {
    viewModel.play()
    assertFalse(viewModel.uiState.value.isPlaying)
  }

  @Test
  fun skipForwardWorksProperly() = run {
    assert(viewModel.uiState.value.tracks.isNotEmpty())

    val firstTrack = viewModel.uiState.value.tracks[0]
    val secondTrack = viewModel.uiState.value.tracks[1]

    viewModel.selectTrack(firstTrack)
    assertEquals(firstTrack.id, viewModel.uiState.value.selectedTrack?.id)

    viewModel.skipForward()
    assertEquals(secondTrack.id, viewModel.uiState.value.selectedTrack?.id)
  }

  @Test
  fun skipForwardAtEndWorksProperly() = run {
    assert(viewModel.uiState.value.tracks.isNotEmpty())

    val firstTrack = viewModel.uiState.value.tracks[0]
    val lastTrack = viewModel.uiState.value.tracks[viewModel.uiState.value.tracks.size - 1]

    viewModel.selectTrack(lastTrack)
    viewModel.play()
    assertTrue(viewModel.uiState.value.isPlaying)
    assertEquals(lastTrack.id, viewModel.uiState.value.selectedTrack?.id)

    viewModel.skipForward()
    assertEquals(firstTrack.id, viewModel.uiState.value.selectedTrack?.id)
  }

  @Test
  fun skipBackwardWorksProperly() = run {
    assert(viewModel.uiState.value.tracks.isNotEmpty())

    val firstTrack = viewModel.uiState.value.tracks[1]
    val secondTrack = viewModel.uiState.value.tracks[0]

    viewModel.selectTrack(firstTrack)
    assertEquals(firstTrack.id, viewModel.uiState.value.selectedTrack?.id)

    viewModel.skipBackward()
    assertEquals(secondTrack.id, viewModel.uiState.value.selectedTrack?.id)
  }

  @Test
  fun skipBackwardAtBeginningWorksProperly() = run {
    assert(viewModel.uiState.value.tracks.isNotEmpty())

    val firstTrack = viewModel.uiState.value.tracks[0]
    val lastTrack = viewModel.uiState.value.tracks[viewModel.uiState.value.tracks.size - 1]

    viewModel.selectTrack(firstTrack)
    viewModel.play()
    assertTrue(viewModel.uiState.value.isPlaying)
    assertEquals(firstTrack.id, viewModel.uiState.value.selectedTrack?.id)

    viewModel.skipBackward()
    assertEquals(lastTrack.id, viewModel.uiState.value.selectedTrack?.id)
  }

  @Test
  fun playTrackWhenControllerReturnsFalse() = run {
    every { mockSpotifyController.playTrack(track) } returns flowOf(false)
    viewModel.selectTrack(track)
    viewModel.play()
    verify { mockSpotifyController.playTrack(track) }
    assertEquals("Failed to play track", viewModel.uiState.value.message)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun resumeTrackWhenControllerReturnsFalse() = runTest {
    every { mockSpotifyController.pauseTrack() } returns flowOf(true)
    every { mockSpotifyController.playTrack(track) } returns flowOf(true)
    every { mockSpotifyController.resumeTrack() } returns flowOf(false)
    viewModel.selectTrack(track)
    viewModel.play()
    viewModel.pause()
    viewModel.play()

    advanceUntilIdle()

    verify { mockSpotifyController.resumeTrack() }
    assertEquals("Failed to resume track", viewModel.uiState.value.message)
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
  fun queueNextTrack() = run {
    viewModel.selectTrack(track)
    viewModel.play()
    viewModel.skipForward()
    assertEquals(viewModel.uiState.value.selectedTrack, viewModel.uiState.value.tracks[3])
  }
}
