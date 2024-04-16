import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.TrackRepositoryImpl
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import ch.epfl.cs311.wanderwave.viewmodel.TrackListViewModel
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class TrackListViewModelTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @RelaxedMockK
    private lateinit var mockSpotifyController: SpotifyController

    lateinit var viewModel: TrackListViewModel
    lateinit var repository: TrackRepositoryImpl
    lateinit var track: Track
    fun setup(connectResult: SpotifyController.ConnectResult) {
        every { mockSpotifyController.connectRemote() } returns flowOf(connectResult)

        viewModel = TrackListViewModel(repository, mockSpotifyController)

        repository = mockk()

        track = Track("6ImuyUQYhJKEKFtlrstHCD","Main Title", "John Williams")
    }

    @OptIn(TrackListViewModel.ForTestingPurposesOnly::class)
    @Test
    fun pauseStopsPlayingWhenTrackIsPlaying() = run {
        val uiState = MutableStateFlow(viewModel.uiState.value.copy(selectedTrack = track, isPlaying = true))

        viewModel.testUpdateUiState(uiState.value)

        viewModel.pause()

        assertFalse(viewModel.uiState.value.isPlaying)
    }

    @Test
    fun pauseDoesNotStopPlayingWhenNoTrackIsPlaying() = run {
//        uiState.uiState.value = viewModel.uiState.value.copy(selectedTrack = null, isPlaying = false)

        viewModel.pause()

        assertFalse(viewModel.uiState.value.isPlaying)
        assertEquals("No track playing", viewModel.uiState.value.message)
    }
}