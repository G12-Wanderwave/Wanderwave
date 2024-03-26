package ch.epfl.cs311.wanderwave.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ch.epfl.cs311.wanderwave.model.data.Profile

class ProfileViewModel : ViewModel() {
  private val _profile =
      MutableLiveData(
          Profile(
              firstName = "My FirstName",
              lastName = "My LastName",
              description = "My Description",
              numberOfLikes = 0,
              isPublic = true,
              profilePictureUri = null))
  val profile: LiveData<Profile> = _profile

  private val _isInEditMode = MutableLiveData(false)
  val isInEditMode: LiveData<Boolean> = _isInEditMode

  private val _isInPublicMode = MutableLiveData(false)
  val isInPublicMode: LiveData<Boolean> = _isInPublicMode

  fun toggleEditMode() {
    _isInEditMode.value = !(_isInEditMode.value ?: false)
  }

  fun updateProfile(updatedProfile: Profile) {
    _profile.value = updatedProfile
  }

  fun togglePublicMode() {
    _isInPublicMode.value = !(_isInPublicMode.value ?: false)
  }
}
