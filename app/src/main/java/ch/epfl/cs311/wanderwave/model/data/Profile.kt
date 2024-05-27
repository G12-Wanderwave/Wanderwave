package ch.epfl.cs311.wanderwave.model.data

import android.net.Uri
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

data class Profile(
    var firstName: String,
    var lastName: String,
    var description: String,
    var numberOfLikes: Int,
    var isPublic: Boolean,
    var profilePictureUri: Uri? = null,
    var spotifyUid: String,
    var firebaseUid: String,
    var likedSongs: List<Track> = emptyList(),
    var topSongs: List<Track> = emptyList(),
    var chosenSongs: List<Track> = emptyList(),
    var bannedSongs: List<Track> = emptyList(),
) {

  fun toMap(db: FirebaseFirestore): Map<String, Any> {

    fun mapTrackToDocumentReference(tracks: List<Track>): List<DocumentReference> {
      return db?.let { nonNulldb -> tracks.map { nonNulldb.collection("tracks").document(it.id) } }
          ?: emptyList()
    }

    val likedSongsReferences: List<DocumentReference> = mapTrackToDocumentReference(likedSongs)
    val topSongsReferences: List<DocumentReference> = mapTrackToDocumentReference(topSongs)
    val chosenSongsReferences: List<DocumentReference> = mapTrackToDocumentReference(chosenSongs)
    val bannedSongsReferences: List<DocumentReference> = mapTrackToDocumentReference(bannedSongs)

    return hashMapOf(
        "firstName" to firstName,
        "lastName" to lastName,
        "description" to description,
        "numberOfLikes" to numberOfLikes,
        "spotifyUid" to spotifyUid,
        "firebaseUid" to firebaseUid,
        "isPublic" to isPublic,
        "profilePictureUri" to (profilePictureUri?.toString() ?: ""), // implement this later
        "numberOfLikes" to numberOfLikes,
        "topSongs" to topSongsReferences,
        "likedSongs" to likedSongsReferences,
        "chosenSongs" to chosenSongsReferences,
        "bannedSongs" to bannedSongsReferences)
  }

  companion object {
    fun from(documentSnapshot: DocumentSnapshot): Profile? {
      return if (documentSnapshot.exists()) {
        Profile(
            firstName = documentSnapshot.getString("firstName") ?: "",
            lastName = documentSnapshot.getString("lastName") ?: "",
            description = documentSnapshot.getString("description") ?: "",
            numberOfLikes = documentSnapshot.getLong("numberOfLikes")?.toInt() ?: 0,
            isPublic = documentSnapshot.getBoolean("isPublic") ?: false,
            profilePictureUri = // implement this later
            documentSnapshot.getString("profilePictureUri")?.let { Uri.parse(it) },
            spotifyUid = documentSnapshot.getString("spotifyUid") ?: "",
            firebaseUid = documentSnapshot.getString("firebaseUid") ?: "",
        )
      } else {
        null
      }
    }
  }
}
