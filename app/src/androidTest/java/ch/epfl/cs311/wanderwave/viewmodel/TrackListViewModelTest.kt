import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.TrackRepositoryImpl
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import ch.epfl.cs311.wanderwave.viewmodel.TrackListViewModel
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TrackListViewModelTest {

  lateinit var viewModel: TrackListViewModel

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK private lateinit var mockSpotifyController: SpotifyController
  @RelaxedMockK private lateinit var repository: TrackRepositoryImpl

  lateinit var track: Track

  @Before
  fun setup() {
    val connectResult = SpotifyController.ConnectResult.SUCCESS
    every { mockSpotifyController.connectRemote() } returns flowOf(connectResult)

    viewModel = TrackListViewModel(repository, mockSpotifyController)

    repository = mockk()

    track = Track("6ImuyUQYhJKEKFtlrstHCD", "Main Title", "John Williams")
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
}
