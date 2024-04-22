package ch.epfl.cs311.wanderwave.model.repository

import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.remote.ProfileConnection
import kotlinx.coroutines.flow.Flow
// ProfileRepositoryImpl.kt
class ProfileRepositoryImpl(private val profileConnection: ProfileConnection) : ProfileRepository {
    override fun addItem(profile: Profile) {
        profileConnection.addItem(profile)
    }

    override fun updateItem(profile: Profile) {
        profileConnection.updateItem(profile)
    }

    override fun deleteItem(profile: Profile) {
        profileConnection.deleteItem(profile)
    }

    override fun getItem(profileId: String): Flow<Profile> {
        return profileConnection.getItem(profileId)
    }

    override fun isUidExisting(spotifyUid: String, callback: (Boolean, Profile?) -> Unit) {
        profileConnection.isUidExisting(spotifyUid, callback)
    }
}