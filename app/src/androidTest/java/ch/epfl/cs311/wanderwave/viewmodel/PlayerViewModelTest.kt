package ch.epfl.cs311.wanderwave.viewmodel

import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import com.spotify.protocol.types.Artist
import com.spotify.protocol.types.PlayerState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK private lateinit var mockSpotifyController: SpotifyController
  @RelaxedMockK private lateinit var playerStateFlow: MutableStateFlow<PlayerState>
  private lateinit var mockArtist: Artist
  private lateinit var mockSpotifyTrack: com.spotify.protocol.types.Track

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var track: Track
  private lateinit var trackList: List<Track>

  private fun Track.toSpotifyTrack(): com.spotify.protocol.types.Track {
    return com.spotify.protocol.types.Track(
        Artist(artist, ""), mockk(), mockk(), 60, title, id, mockk(), false, false)
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

    playerStateFlow =
        MutableStateFlow(PlayerState(mockSpotifyTrack, false, .1f, 0, mockk(), mockk()))
    every { mockSpotifyController.playerState() } returns playerStateFlow

    viewModel = PlayerViewModel(mockSpotifyController)
  }

  @Test
  fun songPausesProperly() = runTest {
    viewModel.pause()
    verify { mockSpotifyController.pauseTrack() }
  }

  @Test
  fun songResumesProperly() = runTest {
    viewModel.resume()
    verify { mockSpotifyController.resumeTrack() }
  }

  @Test
  fun songDoesntPlayWhenNull() = runTest {
    val job =
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
          viewModel.uiState.collect()
        }
    playerStateFlow.value = PlayerState(null, true, .1f, 0, mockk(), mockk())
    coEvery { mockSpotifyController.skip(1) } answers {}

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }

    viewModel.resume()
    advanceUntilIdle()
    Assert.assertFalse(viewModel.uiState.value.isPlaying)
    job.cancel()
  }
}
