package ch.epfl.cs311.wanderwave.model.data

import android.net.Uri
import com.google.firebase.firestore.DocumentSnapshot

data class Profile(
    var firstName: String,
    var lastName: String,
    var description: String,
    var numberOfLikes: Int,
    var isPublic: Boolean,
    var profilePictureUri: Uri? = null,
    var spotifyUid: String,
    var firebaseUid: String,
)

fun DocumentSnapshot.toProfile(): Profile {
  val defaultProfile =
      Profile(
          firstName = "",
          lastName = "",
          description = "",
          numberOfLikes = 0,
          isPublic = false,
          profilePictureUri = null,
          spotifyUid = "",
          firebaseUid = "")
  val firstName = getString("firstName") ?: return defaultProfile
  val lastName = getString("lastName") ?: return defaultProfile
  val description = getString("description") ?: return defaultProfile
  val numberOfLikes = getLong("numberOfLikes")?.toInt() ?: return defaultProfile
  val isPublic = getBoolean("isPublic") ?: return defaultProfile
  val profilePictureUri =
      getString("profilePictureUri").let { Uri.parse(it) } ?: return defaultProfile
  val spotifyUid = getString("spotifyUid") ?: return defaultProfile
  val firebaseUid = getString("firebaseUid") ?: return defaultProfile

  return Profile(
      firstName = firstName,
      lastName = lastName,
      description = description,
      numberOfLikes = numberOfLikes,
      isPublic = isPublic,
      profilePictureUri = profilePictureUri,
      spotifyUid = spotifyUid,
      firebaseUid = firebaseUid)
}
