package ch.epfl.cs311.wanderwave.viewmodel

import androidx.lifecycle.ViewModel
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.TrackRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class TrackListViewModel @Inject constructor(private val repository: TrackRepositoryImpl) :
    ViewModel() {

  private val _uiState = MutableStateFlow(TrackListUiState(loading = true))
  val uiState: StateFlow<TrackListUiState> = _uiState

  init {
    observeTracks()
  }

  private fun observeTracks() {
    CoroutineScope(Dispatchers.IO).launch {
      repository.getAll().collect { tracks ->
        _uiState.value = TrackListUiState(tracks = tracks, loading = false)
      }
    }
  }
}

data class TrackListUiState(val tracks: List<Track> = listOf(), val loading: Boolean = false)
