package ch.epfl.cs311.wanderwave.model.firebase

import android.util.Log
import ch.epfl.cs311.wanderwave.model.data.Profile
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach

class FirebaseConnection {

  private val db = FirebaseFirestore.getInstance()

  // Obtain new UIDs
  fun getNewUid(): String {
    return db.collection("users").document().id
  }

  fun isUidExisting(spotifyUid: String): Boolean {
    var isExisting = false
    db.collection("users")
        .whereEqualTo("spotifyUid", spotifyUid)
        .get()
        .addOnSuccessListener { documents ->
          if (documents.size() > 0) {
            isExisting = true
          }
        }
        .addOnFailureListener { exception ->
          Log.w("Firestore", "Error getting documents: ", exception)
        }
    return isExisting
  }

  // Document to Profile
  private fun documentToProfile(document: DocumentSnapshot): Profile {
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

  // obtain a Profile
  fun getProfile(uid: String): Flow<Profile> {
    Log.d("Firestore", "Fetching profile from Firestore...")
    val profile = MutableStateFlow(Profile("", "", "", 0, false, null, "", ""))
    db.collection("profiles")
        .document(uid)
        .get()
        .addOnSuccessListener { document ->
          if (document != null && document.data != null) {
            val _profile = documentToProfile(document) // You need to implement this function
            profile.tryEmit(_profile)
          } else {
            Log.d("Firestore", "No such Profile document")
          }
        }
        .addOnFailureListener { e -> Log.e("Firestore", "Error getting document: ", e) }
    //  Log.d("Firestore", "Fetched profile from Firestore: $profile")
    return profile.onEach { _profile ->
      Log.d("Firestore", "Fetched profile from Firestore: $_profile")
    }
  }

  fun addProfile(profile: Profile) {
    val profileMap =
        hashMapOf(
            "firstName" to profile.firstName,
            "lastName" to profile.lastName,
            "description" to profile.description,
            "numberOfLikes" to profile.numberOfLikes,
            "isPublic" to profile.isPublic,
            "profilePictureUri" to profile.profilePictureUri.toString())

    db.collection("profiles")
        .document(profile.firebaseUid)
        .set(profileMap)
        .addOnFailureListener { e -> Log.e("Firestore", "Error adding document: ", e) }
        .addOnSuccessListener { Log.d("Firestore", "DocumentSnapshot successfully added!") }
  }

  // Update stored todos
  fun updateProfile(profile: Profile) {
    val uid = profile.firebaseUid

    val profileMap =
        hashMapOf(
            "firstName" to profile.firstName,
            "lastName" to profile.lastName,
            "description" to profile.description,
            "numberOfLikes" to profile.numberOfLikes,
            "isPublic" to profile.isPublic,
            "profilePictureUri" to profile.profilePictureUri.toString())

    db.collection("profiles")
        .document(uid)
        .set(profileMap)
        .addOnFailureListener { e -> Log.e("Firestore", "Error updating document: ", e) }
        .addOnSuccessListener { Log.d("Firestore", "DocumentSnapshot successfully updated!") }
  }

  // Remove todos from the database
  fun deleteProfile(profile: Profile) {
    db.collection("profiles").document(profile.firebaseUid).delete().addOnFailureListener { e ->
      Log.e("Firestore", "Error deleting document: ", e)
    }
  }
}
