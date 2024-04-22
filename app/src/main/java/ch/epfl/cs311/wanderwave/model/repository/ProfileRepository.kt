package ch.epfl.cs311.wanderwave.model.repository

import ch.epfl.cs311.wanderwave.model.data.Profile
import kotlinx.coroutines.flow.Flow

// ProfileRepository.kt
interface ProfileRepository {
    fun addItem(profile: Profile)
    fun updateItem(profile: Profile)
    fun deleteItem(profile: Profile)
    fun getItem(profileId: String): Flow<Profile>
    fun isUidExisting(spotifyUid: String, callback: (Boolean, Profile?) -> Unit)
}