package ch.epfl.cs311.wanderwave.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.firebase.FirebaseConnection
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(private val repository: ProfileRepositoryImpl) :
    ViewModel() {

  private val _profile =
      MutableLiveData(
          Profile(
              firstName = "My FirstName",
              lastName = "My LastName",
              description = "My Description",
              numberOfLikes = 0,
              isPublic = true,
              spotifyUid = "My Spotify UID",
              firebaseUid = "My Firebase UID",
              profilePictureUri = null))
  val profile: LiveData<Profile> = _profile

  private val _isInEditMode = MutableLiveData(false)
  val isInEditMode: LiveData<Boolean> = _isInEditMode

  private val _isInPublicMode = MutableLiveData(false)
  val isInPublicMode: LiveData<Boolean> = _isInPublicMode

  val firebaseConnection = FirebaseConnection()

  fun toggleEditMode() {
    _isInEditMode.value = !(_isInEditMode.value ?: false)
  }

  fun updateProfile(updatedProfile: Profile) {
    _profile.value = updatedProfile
  }

  fun togglePublicMode() {
    _isInPublicMode.value = !(_isInPublicMode.value ?: false)
  }

  fun fetchProfile(profile: Profile) {
    // TODO : fetch profile from Spotify
    // _profile.value = spotifyConnection.getProfile()....
    // Fetch profile from Firestore if it doesn't exist, create it
    firebaseConnection.isUidExisting(profile.spotifyUid) { isExisting, fetchedProfile ->
      if (isExisting) {

        _profile.value = fetchedProfile
        // update profile on the local database
        viewModelScope.launch {
          repository.insert(fetchedProfile!!)
        }

      } else {
        val newProfile = profile
        firebaseConnection.addProfile(newProfile)
        viewModelScope.launch {
          repository.insert(fetchedProfile!!)
        }
        _profile.value = newProfile
      }
    }
  }
}
