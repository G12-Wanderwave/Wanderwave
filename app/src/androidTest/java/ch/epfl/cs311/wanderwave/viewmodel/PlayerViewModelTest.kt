package ch.epfl.cs311.wanderwave.viewmodel

import androidx.lifecycle.viewModelScope
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.TrackRepository
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import com.spotify.protocol.types.Album
import com.spotify.protocol.types.Artist
import com.spotify.protocol.types.ListItem
import com.spotify.protocol.types.PlayerState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class PlayerViewModelTest {

  private lateinit var viewModel: PlayerViewModel

  @get:Rule
  val mockkRule = MockKRule(this)

  @RelaxedMockK
  private lateinit var mockSpotifyController: SpotifyController
  @RelaxedMockK
  private lateinit var playerStateFlow: MutableStateFlow<PlayerState>
  private lateinit var mockArtist: Artist
  private lateinit var mockSpotifyTrack: com.spotify.protocol.types.Track

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var track: Track
  private lateinit var trackList: List<Track>

  private fun Track.toSpotifyTrack() : com.spotify.protocol.types.Track {
    return com.spotify.protocol.types.Track(Artist(artist, ""), mockk(), mockk(), 60, title, id, mockk(), false, false)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Before
  fun setup() = runTest {
    Dispatchers.setMain(testDispatcher)

    val connectResult = SpotifyController.ConnectResult.SUCCESS
    every { mockSpotifyController.connectRemote() } returns flowOf(connectResult)

    track = Track("spotify:track:1cNf5WAYWuQwGoJyfsHcEF", "Across The Stars", "John Williams")
    mockArtist = Artist(track.artist, "")
    mockSpotifyTrack = track.toSpotifyTrack()

    val track1 = Track("spotify:track:6ImuyUQYhJKEKFtlrstHCD", "Main Title", "John Williams")
    val track2 =
      Track("spotify:track:0HLQFjnwq0FHpNVxormx60", "The Nightingale", "Percival Schuttenbach")
    val track3 =
      Track("spotify:track:2NZhNbfb1rD1aRj3hZaoqk", "The Imperial Suite", "Michael Giacchino")
    val track4 = Track("spotify:track:5EWPGh7jbTNO2wakv8LjUI", "Free Bird", "Lynyrd Skynyrd")
    val track5 = Track("spotify:track:4rTlPsga6T8yiHGOvZAPhJ", "Godzilla", "Eminem")

    val trackA = Track("spotify:track:5PbMSJZcNA3p2LZv7C56cm", "Yeah", "Queen")
    val trackB = Track("spotify:track:3C7RbG9Co0zjO7CsuEOqRa", "Sing for the Moment", "Eminem")

    trackList =
      listOf(
        track,
        trackA,
        trackB,
        track1,
        track2,
        track3,
        track4,
        track5,
      )

    playerStateFlow = MutableStateFlow(PlayerState(
      mockSpotifyTrack,
      false,
      .1f,
      0,
      mockk(),
      mockk()))
    every { mockSpotifyController.playerState() } returns playerStateFlow

    viewModel = PlayerViewModel(mockSpotifyController)

  }

  @Test
  fun songPausesProperly() = runTest {
    val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
    Assert.assertTrue(viewModel.uiState.value.isPlaying)

    every { mockSpotifyController.pauseTrack(any(), any()) } answers {
      playerStateFlow.value = PlayerState(mockSpotifyTrack, true, .1f, 0, mockk(), mockk())
    }
    viewModel.pause()
    verify { mockSpotifyController.pauseTrack() }

    advanceUntilIdle()
    Assert.assertFalse(viewModel.uiState.value.isPlaying)
    job.cancel()
  }

  @Test
  fun songResumesProperly() = runTest {
    val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
    playerStateFlow.value = PlayerState(mockSpotifyTrack, true, .1f, 0, mockk(), mockk())
    advanceUntilIdle()
    Assert.assertFalse(viewModel.uiState.value.isPlaying)

    every { mockSpotifyController.resumeTrack(any(), any()) } answers {
      playerStateFlow.value = PlayerState(mockSpotifyTrack, false, .1f, 0, mockk(), mockk())
    }
    viewModel.resume()
    verify { mockSpotifyController.resumeTrack() }

    advanceUntilIdle()
    Assert.assertTrue(viewModel.uiState.value.isPlaying)
    job.cancel()
  }

  @Test
  fun songDoesntPlayWhenNull() = runTest {
    val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
    playerStateFlow.value = PlayerState(null, true, .1f, 0, mockk(), mockk())
    coEvery { mockSpotifyController.skip(1) } answers {

    }
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }

    viewModel.resume()
    advanceUntilIdle()
    Assert.assertFalse(viewModel.uiState.value.isPlaying)
    job.cancel()
  }

//  @Test
//  fun skipForwardWorksProperly() = runBlockingTest {
//    viewModel.skipForward()
//    delay(1000)
//    coVerify { mockSpotifyController.skip(any(), any(), any()) }
//  }
//
//  @Test
//  fun skipForwardAtEndWorksProperly() = run {
//    assert(viewModel.uiState.value.tracks.isNotEmpty())
//
//    val firstTrack = viewModel.uiState.value.tracks[0]
//    val lastTrack = viewModel.uiState.value.tracks[viewModel.uiState.value.tracks.size - 1]
//
//    viewModel.toggleLoop()
//    viewModel.selectTrack(lastTrack)
//    viewModel.play()
//    Assert.assertTrue(viewModel.uiState.value.isPlaying)
//    Assert.assertEquals(lastTrack.id, viewModel.uiState.value.selectedTrack?.id)
//
//    viewModel.skipForward()
//    Assert.assertEquals(firstTrack.id, viewModel.uiState.value.selectedTrack?.id)
//  }
//
//  @Test
//  fun skipBackwardWorksProperly() = run {
//    assert(viewModel.uiState.value.tracks.isNotEmpty())
//
//    val firstTrack = viewModel.uiState.value.tracks[1]
//    val secondTrack = viewModel.uiState.value.tracks[0]
//
//    viewModel.selectTrack(firstTrack)
//    Assert.assertEquals(firstTrack.id, viewModel.uiState.value.selectedTrack?.id)
//
//    viewModel.skipBackward()
//    Assert.assertEquals(secondTrack.id, viewModel.uiState.value.selectedTrack?.id)
//  }
//
//  @Test
//  fun skipBackwardAtBeginningWorksProperly() = run {
//    assert(viewModel.uiState.value.tracks.isNotEmpty())
//
//    val firstTrack = viewModel.uiState.value.tracks[0]
//    val lastTrack = viewModel.uiState.value.tracks[viewModel.uiState.value.tracks.size - 1]
//
//    viewModel.toggleLoop()
//    viewModel.selectTrack(firstTrack)
//    viewModel.play()
//    Assert.assertTrue(viewModel.uiState.value.isPlaying)
//    Assert.assertEquals(firstTrack.id, viewModel.uiState.value.selectedTrack?.id)
//
//    viewModel.skipBackward()
//    Assert.assertEquals(lastTrack.id, viewModel.uiState.value.selectedTrack?.id)
//  }
//
//  @Test
//  fun playTrackWhenControllerReturnsFalse() = run {
//    every { mockSpotifyController.playTrack(track) } returns flowOf(false)
//    viewModel.selectTrack(track)
//    viewModel.play()
//  }
//
//  @OptIn(ExperimentalCoroutinesApi::class)
//  @Test
//  fun resumeTrackWhenControllerReturnsFalse() = runTest {
//    every { mockSpotifyController.pauseTrack() } returns flowOf(true)
//    every { mockSpotifyController.playTrack(track) } returns flowOf(true)
//    every { mockSpotifyController.resumeTrack() } returns flowOf(false)
//    viewModel.selectTrack(track)
//    viewModel.play()
//    viewModel.pause()
//    viewModel.play()
//
//    advanceUntilIdle()
//  }
//
//  @Test
//  fun testToggleShuffle() = run {
//    viewModel.toggleShuffle()
//    Assert.assertTrue(viewModel.uiState.value.isShuffled)
//    viewModel.toggleShuffle()
//    Assert.assertFalse(viewModel.uiState.value.isShuffled)
//  }
//
//  @Test
//  fun testIfQueueHasBeenShuffled() = run {
//    Assert.assertEquals(viewModel.uiState.value.tracks, viewModel.uiState.value.queue)
//    viewModel.toggleShuffle()
//    Assert.assertNotEquals(viewModel.uiState.value.tracks, viewModel.uiState.value.queue)
//  }
//
//  @Test
//  fun testIfQueueHasBeenUnshuffled() = run {
//    Assert.assertEquals(viewModel.uiState.value.tracks, viewModel.uiState.value.queue)
//    viewModel.toggleShuffle()
//    viewModel.toggleShuffle()
//    Assert.assertEquals(viewModel.uiState.value.tracks, viewModel.uiState.value.queue)
//  }
//
//  @Test
//  fun testSkipForwardWhenLooping() = run {
//    viewModel.toggleLoop()
//    viewModel.selectTrack(viewModel.uiState.value.tracks[viewModel.uiState.value.tracks.size - 1])
//    viewModel.skipForward()
//    Assert.assertEquals(viewModel.uiState.value.tracks[0], viewModel.uiState.value.selectedTrack)
//  }
//
//  @Test
//  fun testSkipForwardWhenNotLooping() = run {
//    viewModel.selectTrack(viewModel.uiState.value.tracks[viewModel.uiState.value.tracks.size - 1])
//    viewModel.skipForward()
//    Assert.assertNull(viewModel.uiState.value.selectedTrack)
//  }
//
//  @Test
//  fun testSkipBackwardWhenLooping() = run {
//    viewModel.toggleLoop()
//    viewModel.selectTrack(viewModel.uiState.value.tracks[0])
//    viewModel.skipBackward()
//    Assert.assertEquals(
//      viewModel.uiState.value.tracks[viewModel.uiState.value.tracks.size - 1],
//      viewModel.uiState.value.selectedTrack
//    )
//  }
//
//  @Test
//  fun testSkipBackwardWhenNotLooping() = run {
//    viewModel.selectTrack(viewModel.uiState.value.tracks[0])
//    viewModel.skipBackward()
//    Assert.assertNull(viewModel.uiState.value.selectedTrack)
//  }
//
//  @Test
//  fun testLoopToggle() {
//    assertEquals(LoopMode.NONE, viewModel.uiState.value.loopMode)
//    viewModel.toggleLoop()
//    assertEquals(LoopMode.ALL, viewModel.uiState.value.loopMode)
//    viewModel.toggleLoop()
//    assertEquals(LoopMode.ONE, viewModel.uiState.value.loopMode)
//    viewModel.toggleLoop()
//    assertEquals(LoopMode.NONE, viewModel.uiState.value.loopMode)
//  }
//
//  @Test
//  fun testSetLoop() {
//    viewModel.setLoop(LoopMode.ALL)
//    assertEquals(LoopMode.ALL, viewModel.uiState.value.loopMode)
//    viewModel.setLoop(LoopMode.ONE)
//    assertEquals(LoopMode.ONE, viewModel.uiState.value.loopMode)
//    viewModel.setLoop(LoopMode.NONE)
//    assertEquals(LoopMode.NONE, viewModel.uiState.value.loopMode)
//  }
//
//  @Test
//  fun tracksPlayOneAfterAnother() = run {
//    viewModel.selectTrack(viewModel.uiState.value.queue[0])
//    viewModel.play()
//    Assert.assertTrue(viewModel.uiState.value.isPlaying)
//    assertEquals(viewModel.uiState.value.queue[0].id, viewModel.uiState.value.selectedTrack?.id)
//
//    testDispatcher.scheduler.advanceUntilIdle()
//  }
//
//  @OptIn(ExperimentalCoroutinesApi::class)
//  @Test
//  fun playTrackWhenNoTrackSelected() = runTest {
//    viewModel.play()
//    Assert.assertFalse(viewModel.uiState.value.isPlaying)
//    Assert.assertEquals("No track selected", viewModel.uiState.value.message)
//  }
//
//  @Test
//  fun playTrackWhenTrackAlreadyPlaying() = run {
//    viewModel.selectTrack(track)
//    viewModel.play()
//    viewModel.play()
//    Assert.assertTrue(viewModel.uiState.value.isPlaying)
//    Assert.assertEquals("Track already playing", viewModel.uiState.value.message)
//  }
//
//  @Test
//  fun pauseTrackWhenNoTrackPlaying() = run {
//    viewModel.pause()
//    Assert.assertFalse(viewModel.uiState.value.isPlaying)
//    Assert.assertEquals("No track playing", viewModel.uiState.value.message)
//  }
//
//  @Test
//  fun skipForwardWhenNoTrackSelected() = run {
//    viewModel.skipForward()
//    Assert.assertNull(viewModel.uiState.value.selectedTrack)
//  }
//
//  @Test
//  fun skipBackwardWhenNoTrackSelected() = run {
//    viewModel.skipBackward()
//    Assert.assertNull(viewModel.uiState.value.selectedTrack)
//  }
//
//  @Test
//  fun toggleLoopWhenLoopModeIsOne() = run {
//    viewModel.setLoop(LoopMode.ONE)
//    viewModel.toggleLoop()
//    assertEquals(LoopMode.NONE, viewModel.uiState.value.loopMode)
//  }
//
//  @Test
//  fun testGetAllChildrenFlow() = runBlockingTest {
//    val expectedListItem = ListItem("id", "title", null, "subtitle", "", false, true)
//    every { mockSpotifyController.getAllChildren(expectedListItem) } returns
//        flowOf(listOf(expectedListItem))
//
//    val result = mockSpotifyController.getAllChildren(expectedListItem)
//    Assert.assertEquals(expectedListItem, result.first().get(0)) // Check if the first item is as expected
//  }
}