package ch.epfl.cs311.wanderwave.viewmodel

import android.util.Log
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.localDb.AppDatabase
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepository
import ch.epfl.cs311.wanderwave.model.repository.TrackRepository
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import ch.epfl.cs311.wanderwave.model.spotify.getLikedTracksFromSpotify
import ch.epfl.cs311.wanderwave.model.spotify.getTotalLikedTracksFromSpotity
import ch.epfl.cs311.wanderwave.viewmodel.interfaces.SpotifySongsActions
import com.spotify.protocol.types.ListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@HiltViewModel
class TrackListViewModel
@Inject
constructor(
    private val spotifyController: SpotifyController,
    private val appDatabase: AppDatabase,
    private val repository: TrackRepository,
    private val profileRepository: ProfileRepository,
    private val authenticationController: AuthenticationController
) : ViewModel(), SpotifySongsActions {

  private val _uiState = MutableStateFlow(UiState(loading = true))
  val uiState: StateFlow<UiState> = _uiState

  private var _searchQuery = MutableStateFlow("")

  private val _nbrLikedSongs = MutableStateFlow(0)
  override val nbrLikedSongs: StateFlow<Int> = _nbrLikedSongs

  fun loadTracksBasedOnSource(index: Int) {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(loading = true)
      when (index) {
        0 ->
            _uiState.value =
                _uiState.value.copy(
                    tracks =
                        spotifyController.recentlyPlayedTracks
                            .value) // TODO:modify for the recnetly played tracks
        1 -> loadRecentlyAddedTracks() // TODO: modify here for the liked tracks
        2 -> loadRecentlyAddedTracks() // TODO: modify here for the banned tracks
      }
    }
  }

  fun loadRecentlyAddedTracks() {
    viewModelScope.launch {
      val trackRecords =
          appDatabase.trackRecordDao().getAllRecentlyAddedTracks().firstOrNull() ?: listOf()
      val trackDetails =
          trackRecords.mapNotNull {
            repository.getItem(it.trackId).firstOrNull()?.getOrElse { null }
          }
      Log.d("TrackListViewModel", "loadRecentlyAddedTracks: $trackDetails")
      _uiState.value = _uiState.value.copy(tracks = trackDetails, loading = false)
    }
  }

  private val _likedSongsTrackList = MutableStateFlow<List<ListItem>>(emptyList())
  override val likedSongsTrackList: StateFlow<List<ListItem>> = _likedSongsTrackList

  init {
    observeTracks()
  }

  private fun observeTracks() {
    // Filter only songs already cached/downloaded in Spotify

    viewModelScope.launch {
      repository.getAll().collect { tracks ->
        _uiState.value =
            UiState(
                tracks = tracks.filter { matchesSearchQuery(it) },
                loading = false,
                progress = spotifyController.trackProgress)
      }
      // deal with the flow
    }
  }

  fun updateBannedSongs() {
    viewModelScope.launch {
      val profileId = authenticationController.getUserData()!!.id
      profileRepository.getItem(profileId).collect { fetchedProfile ->
        fetchedProfile.onSuccess { profile ->
          _uiState.value = uiState.value.copy(bannedTracks = profile.bannedSongs)
        }
      }
    }
  }

  private fun matchesSearchQuery(track: Track): Boolean {
    return track.title.contains(_searchQuery.value, ignoreCase = true) ||
        track.artist.contains(_searchQuery.value, ignoreCase = true)
  }

  private var searchJob: Job? = null

  fun setSearchQuery(query: String) {
    searchJob?.cancel()
    searchJob =
        viewModelScope.launch {
          delay(300) // Debounce time in milliseconds
          _searchQuery.value = query
          observeTracks() // Re-filter tracks when search query changes
        }
  }

  override fun addTrackToList(track: Track) {
    val newTrack =
        if (!track.id.contains("spotify:track:")) {
          Track("spotify:track:" + track.id, track.title, track.artist)
        } else {
          track
        }
    if (!_uiState.value.tracks.contains(newTrack)) {
      val updatedTracks = _uiState.value.tracks + newTrack
      _uiState.value = _uiState.value.copy(tracks = updatedTracks)
    }
  }

  fun removeTrackFromBanList(track: Track) {
    // TODO: only update the Ban List and make it update on firebase
    val updatedTracks = _uiState.value.tracks - track
    _uiState.value = _uiState.value.copy(tracks = updatedTracks)
  }

  /**
   * Plays the given track using the SpotifyController.
   *
   * @param track The track to play.
   */
  fun playTrack(track: Track) {
    spotifyController.playTrackList(uiState.value.tracks, track)
  }

  fun collapse() {
    _uiState.value = _uiState.value.copy(expanded = false)
  }

  fun expand() {
    _uiState.value = _uiState.value.copy(expanded = true)
  }

  override suspend fun getLikedTracks(page: Int) {
    getLikedTracksFromSpotify(this._likedSongsTrackList, spotifyController, viewModelScope, page)
  }

  override suspend fun getTotalLikedTracks() {
    _nbrLikedSongs.value = getTotalLikedTracksFromSpotity(spotifyController)
  }

  override fun clearLikedSongs() {
    _likedSongsTrackList.value = emptyList()
  }

  data class UiState(
      val tracks: List<Track> = listOf(),
      val retrievedTrack: List<Track> = listOf(),
      val bannedTracks: List<Track> = emptyList(),
      val loading: Boolean = false,
      val expanded: Boolean = false,
      val progress: MutableFloatState = mutableFloatStateOf(0f),
      val isShuffled: Boolean = false,
      val loopMode: SpotifyController.RepeatMode = SpotifyController.RepeatMode.OFF
  )
}
