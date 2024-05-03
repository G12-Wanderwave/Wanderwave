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
) {

  fun toMap(): HashMap<String, Any> {
    return hashMapOf(
        "firstName" to firstName,
        "lastName" to lastName,
        "description" to description,
        "numberOfLikes" to numberOfLikes,
        "spotifyUid" to spotifyUid,
        "firebaseUid" to firebaseUid,
        "isPublic" to isPublic,
        "profilePictureUri" to (profilePictureUri?.toString() ?: ""))
  }

  companion object {
    fun from(documentSnapshot: DocumentSnapshot): Profile? {
      if (documentSnapshot.exists()) {
        return Profile(
            firstName = documentSnapshot.getString("firstName") ?: "",
            lastName = documentSnapshot.getString("lastName") ?: "",
            description = documentSnapshot.getString("description") ?: "",
            numberOfLikes = documentSnapshot.getLong("numberOfLikes")?.toInt() ?: 0,
            isPublic = documentSnapshot.getBoolean("isPublic") ?: false,
            profilePictureUri =
                documentSnapshot.getString("profilePictureUri")?.let { Uri.parse(it) },
            spotifyUid = documentSnapshot.getString("spotifyUid") ?: "",
            firebaseUid = documentSnapshot.getString("firebaseUid") ?: "",
        )
      } else {
        return null
      }
    }
  }
}
