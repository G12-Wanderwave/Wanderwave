package ch.epfl.cs311.wanderwave.model.repository

import ch.epfl.cs311.wanderwave.model.data.Profile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {

  suspend fun getProfile(): Flow<Profile?>

  suspend fun insert(profile: Profile)

  suspend fun delete()
}
