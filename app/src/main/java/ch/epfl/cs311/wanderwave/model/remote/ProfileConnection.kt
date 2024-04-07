package ch.epfl.cs311.wanderwave.model.remote

import android.util.Log
import ch.epfl.cs311.wanderwave.model.data.Profile
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach

class ProfileConnection : FirebaseConnectionInt<Profile, Profile>{

  init {}

  override val collectionName: String = "users"

  override val getItemId = { profile:Profile -> profile.firebaseUid }



  private val db = FirebaseFirestore.getInstance()

  fun isUidExisting(spotifyUid: String, callback: (Boolean, Profile?) -> Unit) {
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
  override fun documentToItem(document: DocumentSnapshot): Profile {
    val uid = document.id
    val spotifyUid = document.getString("spotifyUid") ?: ""
    val firstname = document.getString("firstname") ?: "Untitled"
    val lastname = document.getString("lastname") ?: "Untitled"
    val description = document.getString("description") ?: "No description"
    val numberOfLikes = document.getLong("numberOfLikes")?.toInt() ?: 0
    val isPublic = document.getBoolean("isPublic") ?: false
    return Profile(
        firstName = firstname,
        lastName = lastname,
        description = description,
        firebaseUid = uid,
        spotifyUid = spotifyUid,
        numberOfLikes = numberOfLikes,
        isPublic = isPublic)
  }

  override fun itemToHash(profile: Profile): HashMap<String, Any> {
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

  // obtain a Profile
  override fun getItem(uid: String): Flow<Profile> {
    Log.d("Firestore", "Fetching profile from Firestore...")
    val profile = MutableStateFlow(Profile("", "", "", 0, false, null, "", ""))
    db.collection("profiles")
        .document(uid)
        .get()
        .addOnSuccessListener { document ->
          if (document != null && document.data != null) {
            val _profile = documentToItem(document) // You need to implement this function
            profile.tryEmit(_profile)
          } else {
            Log.d("Firestore", "No such Profile document")
          }
        }
        .addOnFailureListener { e -> Log.e("Firestore", "Error getting document: ", e) }
    //  Log.d("Firestore", "Fetched profile from Firestore: $profile")
    return profile
  }



  override fun addItem(profile: Profile) {
    val profileMap = itemToHash(profile)

    db.collection("users")
        .add(profileMap)
        .addOnFailureListener { e -> Log.e("Firestore", "Error adding document: ", e) }
        .addOnSuccessListener { Log.d("Firestore", "DocumentSnapshot successfully added!") }
  }

  // Update stored todos
  override fun updateItem(profile: Profile) {
    val uid = profile.firebaseUid

    val profileMap = itemToHash(profile)

    db.collection("users")
        .document(uid)
        .set(profileMap)
        .addOnFailureListener { e -> Log.e("Firestore", "Error updating document: ", e) }
        .addOnSuccessListener { Log.d("Firestore", "DocumentSnapshot successfully updated!") }
  }

  // Remove todos from the database
  override fun deleteItem(profile: Profile) {
    db.collection("users").document(profile.firebaseUid).delete().addOnFailureListener { e ->
      Log.e("Firestore", "Error deleting document: ", e)
    }
  }
}
