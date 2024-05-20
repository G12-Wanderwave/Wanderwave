package ch.epfl.cs311.wanderwave.model.data

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

/**
 * This class represents the association between a profile and a track.
 *
 * @param profile The profile associated with the track.
 * @param track The track associated with the profile.
 * @author Clarence LINDEN
 * @since 3.0
 * @last update 3.0
 */
data class ProfileTrackAssociation(val profile: Profile? = null, val track: Track, val likes: Int = 0) {

  fun toMap(db: FirebaseFirestore): Map<String, Any?> {
    val profileRef: DocumentReference? = profile?.let { db.collection("users").document(it.firebaseUid) }
    val trackRef: DocumentReference? = db.collection("tracks")?.document(track.id)

    // mapping to null will not show on firebase
    return hashMapOf("profile" to profileRef, "track" to trackRef, "likes" to likes)
  }
}
