package ch.epfl.cs311.wanderwave.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.firebase.FirebaseConnection
import kotlinx.coroutines.flow.onEach

class ProfileViewModel : ViewModel() {
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

  fun fetchProfile() {
    // TODO : fetch profile from Spotify
    // _profile.value = spotifyConnection.getProfile()....
    // Fetch profile from Firestore if it doesn't exist, create it
    val isUidExisting = firebaseConnection.isUidExisting(_profile.value!!.spotifyUid)
    if (!isUidExisting) {
      val newUid = firebaseConnection.getNewUid()
      _profile.value = _profile.value!!.copy(firebaseUid = newUid)
      firebaseConnection.addProfile(_profile.value!!)
    } else {
      firebaseConnection.getProfile(_profile.value!!.spotifyUid).onEach {
        _profile.value = it
      }
    }
  }
}
