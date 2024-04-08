package ch.epfl.cs311.wanderwave.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.remote.ProfileConnection
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ch.epfl.cs311.wanderwave.model.data.Track




// Define a simple class for a song list
data class SongList(val name: String, val tracks: MutableList<Track> = mutableListOf())
@HiltViewModel
class ProfileViewModel @Inject constructor(private val repository: ProfileRepositoryImpl) :
    ViewModel() {

  private val _profile =
      MutableStateFlow(
          Profile(
              firstName = "My FirstName",
              lastName = "My LastName",
              description = "My Description",
              numberOfLikes = 0,
              isPublic = true,
              spotifyUid = "My Spotify UID",
              firebaseUid = "My Firebase UID",
              profilePictureUri = null))
  val profile: StateFlow<Profile> = _profile

  private val _isInEditMode = MutableStateFlow(false)
  val isInEditMode: StateFlow<Boolean> = _isInEditMode

  private val _isInPublicMode = MutableStateFlow(false)
  val isInPublicMode: StateFlow<Boolean> = _isInPublicMode

  // Add a state for managing song lists
  private val _songLists = MutableStateFlow<List<SongList>>(emptyList())
  val songLists: StateFlow<List<SongList>> = _songLists

  fun createSpecificSongList(listType: String) {
    val listName =
      when (listType) {
        "TOP_SONGS" -> "TOP SONGS"
        "CHOSEN_SONGS" -> "CHOSEN SONGS"
        else -> return // Or handle error/invalid type
      }
    // Check if the list already exists
    val existingList = _songLists.value.firstOrNull { it.name == listName }
    if (existingList == null) {
      // Add new list if it doesn't exist
      _songLists.value = _songLists.value + SongList(listName)
    }
    // Do nothing if the list already exists
  }

  // Function to add a track to a song list

  fun addTrackToList(listName: String, track: Track) {
    val updatedLists =
      _songLists.value.map { list ->
        if (list.name == listName) {
          list.copy(tracks = ArrayList(list.tracks).apply { add(track) })
        } else {
          list
        }
      }
    _songLists.value = updatedLists
  }

  val profileConnection = ProfileConnection()

  fun updateProfile(updatedProfile: Profile) {
    _profile.value = updatedProfile
    profileConnection.updateItem(updatedProfile)
    viewModelScope.launch {
      repository.delete()
      repository.insert(_profile.value)
    }
  }

  fun deleteProfile() {
    profileConnection.deleteItem(_profile.value)
    viewModelScope.launch { repository.delete() }
  }

  fun togglePublicMode() {
    _isInPublicMode.value = !_isInPublicMode.value
  }

  fun fetchProfile(profile: Profile) {
    // TODO : fetch profile from Spotify
    // _profile.value = spotifyConnection.getProfile()....
    // Fetch profile from Firestore if it doesn't exist, create it
    Log.d("ProfileViewModel", "Fetching profile from Firestore...")
    profileConnection.isUidExisting(profile.spotifyUid) { isExisting, fetchedProfile ->
      if (isExisting) {
        _profile.value = fetchedProfile ?: profile
        // update profile on the local database
        viewModelScope.launch {
          val localProfile = repository.getProfile()
          localProfile.collect { fetchedLocalProfile ->
            if (fetchedLocalProfile != fetchedProfile) {
              repository.delete()
              repository.insert(fetchedProfile!!)
            }
          }
        }
      } else {
        val newProfile = profile
        profileConnection.addItem(newProfile)
        viewModelScope.launch { repository.insert(newProfile) }
        _profile.value = newProfile
      }
    }
    // TODO : get rid of this line
    profileConnection.getItem(profile.spotifyUid).let { Log.d("Firebase", it.toString()) }
  }
}
