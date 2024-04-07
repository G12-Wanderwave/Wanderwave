package ch.epfl.cs311.wanderwave.model.repository

import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.localDb.LocalProfileRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ProfileRepositoryImpl
@Inject
constructor(private val localRepository: LocalProfileRepository) : ProfileRepository {

  override suspend fun getProfile(): Flow<Profile?> {
    return localRepository.getProfile()
  }

  override suspend fun insert(profile: Profile) {
    localRepository.insert(profile)
  }

  override suspend fun delete() {
    localRepository.delete()
  }
}
