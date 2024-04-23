package ch.epfl.cs311.wanderwave.model.repository

import ch.epfl.cs311.wanderwave.model.data.Profile

// ProfileRepository.kt
interface ProfileRepository : FirebaseRepository<Profile> {

  fun isUidExisting(spotifyUid: String, callback: (Boolean, Profile?) -> Unit)
}
