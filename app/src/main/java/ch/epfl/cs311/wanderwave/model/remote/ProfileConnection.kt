package ch.epfl.cs311.wanderwave.model.remote

import android.util.Log
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class ProfileConnection(private val database: FirebaseFirestore? = null) :
    FirebaseConnection<Profile, Profile>(), ProfileRepository {

  override val collectionName: String = "users"

  override val getItemId = { profile: Profile -> profile.firebaseUid }

  override val db = database ?: super.db

  override fun isUidExisting(spotifyUid: String, callback: (Boolean, Profile?) -> Unit) {
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

  override fun itemToMap(profile: Profile): HashMap<String, Any> {
    val profileMap: HashMap<String, Any> = profile.toMap()

    return profileMap
  }
}
