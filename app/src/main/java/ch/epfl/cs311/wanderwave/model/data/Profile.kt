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


fun DocumentSnapshot.toProfile(): Profile? {
    val firstName = getString("firstName") ?: return null
    val lastName = getString("lastName") ?: return null
    val description = getString("description") ?: return null
    val numberOfLikes = getLong("numberOfLikes")?.toInt() ?: return null
    val isPublic = getBoolean("isPublic") ?: return null
    val profilePictureUri = getString("profilePictureUri").let { Uri.parse(it) } ?: return null
    val spotifyUid = getString("spotifyUid") ?: return null
    val firebaseUid = getString("firebaseUid") ?: return null

    return Profile(
        firstName = firstName,
        lastName = lastName,
        description = description,
        numberOfLikes = numberOfLikes,
        isPublic = isPublic,
        profilePictureUri = profilePictureUri,
        spotifyUid = spotifyUid,
        firebaseUid = firebaseUid
    )
}
