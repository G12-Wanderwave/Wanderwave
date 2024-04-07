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
          if (localProfile == null) {
            repository.insert(fetchedProfile!!)
          } else {
            repository.delete()
          }
          repository.insert(fetchedProfile!!)
        }
      } else {
        val newProfile = profile
        profileConnection.addItem(newProfile)
        viewModelScope.launch { repository.insert(newProfile) }
        _profile.value = newProfile
      }
    }
  }
}
