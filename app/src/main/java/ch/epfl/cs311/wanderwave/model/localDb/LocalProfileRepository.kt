package ch.epfl.cs311.wanderwave.model.localDb

import android.net.Uri
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LocalProfileRepository @Inject constructor(private val database: AppDatabase) :
    ProfileRepository {

  private val profileDao = database.profileDao()

  private fun ProfileEntity.toProfile() =
      Profile(
          firstName,
          lastName,
          description,
          numberOfLikes,
          isPublic,
          profilePictureUri?.let {
            Uri.parse(it)
          }, // Assuming profilePictureUri is stored as String
          spotifyUid,
          firebaseUid)

  override suspend fun getProfile(): Flow<Profile?> {
    return flow {
      val profileEntity = profileDao.getProfile()
      emit(profileEntity?.toProfile())
    }
  }

  override suspend fun insert(profile: Profile) {
    profileDao.insertProfile(
        ProfileEntity(
            id = 1, // Assuming only one profile exists, you can adjust the primary key as needed
            firstName = profile.firstName,
            lastName = profile.lastName,
            description = profile.description,
            numberOfLikes = profile.numberOfLikes,
            isPublic = profile.isPublic,
            profilePictureUri = profile.profilePictureUri?.toString(), // Convert Uri to String
            spotifyUid = profile.spotifyUid,
            firebaseUid = profile.firebaseUid))
  }

  override suspend fun delete() {
    val profile = profileDao.getProfile()
    if (profile != null) {
      profileDao.deleteProfile(profile)
    }
  }
}
