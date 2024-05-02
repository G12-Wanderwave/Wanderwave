package ch.epfl.cs311.wanderwave.model.remote

import android.util.Log
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileConnection(private val database: FirebaseFirestore? = null) :
    FirebaseConnection<Profile, Profile>(), ProfileRepository {

  override val collectionName: String = "users"

  override val getItemId = { profile: Profile -> profile.firebaseUid }

  override val db = database ?: super.db
  private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

  override fun isUidExisting(spotifyUid: String, callback: (Boolean, Profile?) -> Unit) {
    db.collection("users")
        .whereEqualTo("spotifyUid", spotifyUid)
        .get()
        .addOnSuccessListener { documents ->
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

  fun addProfilesIfNotExist(profiles: List<Profile>) {
    coroutineScope.launch {
      profiles.forEach { profile ->
        val querySnapshot =
            db.collection(collectionName)
                .whereEqualTo("firebaseUid", profile.firebaseUid)
                .get()
                .await()
        if (querySnapshot.isEmpty) {
          addItemWithId(profile)
        }
      }
    }
  }
}
