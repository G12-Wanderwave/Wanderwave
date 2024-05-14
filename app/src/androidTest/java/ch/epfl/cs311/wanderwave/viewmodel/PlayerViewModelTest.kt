package ch.epfl.cs311.wanderwave.viewmodel

import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.TrackRepository
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import com.spotify.protocol.types.Album
import com.spotify.protocol.types.ListItem
import com.spotify.protocol.types.PlayerState
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
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
  private lateinit var playerState: PlayerState

  @RelaxedMockK
  private lateinit var repository: TrackRepository

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

    playerState = mockk<PlayerState>(relaxed = true)
    every { mockSpotifyController.playerState() } returns flowOf(playerState)

    viewModel = PlayerViewModel(mockSpotifyController)
//    runBlocking { viewModel.uiState.first { !it.loading } }

  }

  @Test
  fun songPausesProperly() = run {
    var playerState = PlayerState(mockk(), false, .1f, 0, mockk(), mockk())
    every { mockSpotifyController.playerState() } returns flowOf(playerState)
    every { mockSpotifyController.pauseTrack(any(), any()) } answers {
       playerState = PlayerState(mockk(), true, .1f, 0, mockk(), mockk())
    }

    viewModel.pause()
    Assert.assertTrue(playerState.isPaused)
    Assert.assertFalse(viewModel.uiState.value.isPlaying)
  }

//  @Test
//  fun songResumesProperly() = run {
//    viewModel.selectTrack(track)
//    viewModel.play()
//    viewModel.pause()
//    Assert.assertFalse(viewModel.uiState.value.isPlaying)
//
//    viewModel.play()
//    Assert.assertTrue(viewModel.uiState.value.isPlaying)
//    Assert.assertEquals(track.id, viewModel.uiState.value.selectedTrack?.id)
//  }
//
//  @Test
//  fun songDoesntPlayWhenNull() = run {
//    viewModel.play()
//    Assert.assertFalse(viewModel.uiState.value.isPlaying)
//  }
//
//  @Test
//  fun skipForwardWorksProperly() = run {
//    assert(viewModel.uiState.value.tracks.isNotEmpty())
//
//    val firstTrack = viewModel.uiState.value.tracks[0]
//    val secondTrack = viewModel.uiState.value.tracks[1]
//
//    viewModel.selectTrack(firstTrack)
//    Assert.assertEquals(firstTrack.id, viewModel.uiState.value.selectedTrack?.id)
//
//    viewModel.skipForward()
//    Assert.assertEquals(secondTrack.id, viewModel.uiState.value.selectedTrack?.id)
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