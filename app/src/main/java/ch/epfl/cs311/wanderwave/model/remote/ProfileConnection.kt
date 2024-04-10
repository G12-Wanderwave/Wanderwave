package ch.epfl.cs311.wanderwave.model.remote

import android.util.Log
import ch.epfl.cs311.wanderwave.model.data.Profile
import com.google.firebase.firestore.DocumentSnapshot

class ProfileConnection : FirebaseConnection<Profile, Profile>() {

  override val collectionName: String = "users"

  override val getItemId = { profile: Profile -> profile.firebaseUid }

  // private val db = FirebaseFirestore.getInstance()

  fun isUidExisting(spotifyUid: String, callback: (Boolean, Profile?) -> Unit) {
    Log.d("ProfileConnection", "Checking if Spotify UID exists in Firestore...")
    db.collection("users")
        .whereEqualTo("spotifyUid", spotifyUid)
        .get()
        .addOnSuccessListener { documents ->
          Log.d("Firestore", "DocumentSnapshot data: ${documents.documents}")
          val isExisting = documents.size() > 0
          callback(isExisting, if (isExisting) documentToItem(documents.documents[0]) else null)
        }
        .addOnFailureListener { exception ->
          Log.w("Firestore", "Error getting documents: ", exception)
          callback(false, null) // Assuming failure means document doesn't exist
        }
  }

  // Document to Profile
  override fun documentToItem(document: DocumentSnapshot): Profile? {
    return Profile.from(document)
  }

  override fun itemToMap(profile: Profile): Map<String, Any> {
    val profileMap: HashMap<String, Any> =
        hashMapOf(
            "firstName" to profile.firstName,
            "lastName" to profile.lastName,
            "description" to profile.description,
            "numberOfLikes" to profile.numberOfLikes,
            "spotifyUid" to profile.spotifyUid,
            "firebaseUid" to profile.firebaseUid,
            "isPublic" to profile.isPublic,
            "profilePictureUri" to (profile.profilePictureUri?.toString() ?: ""))
    return profileMap
  }
}
