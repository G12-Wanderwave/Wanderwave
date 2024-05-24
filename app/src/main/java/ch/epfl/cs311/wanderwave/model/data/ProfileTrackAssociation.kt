package ch.epfl.cs311.wanderwave.model.data


import com.google.firebase.firestore.DocumentSnapshot
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
data class ProfileTrackAssociation(
    val profile: Profile? = null,
    val track: Track,
    val likersId: List<String> = emptyList(),
    val likes: Int = 0
) {

  fun toMap(db: FirebaseFirestore): Map<String, Any?> {
    return hashMapOf(
        "creator" to profile?.firebaseUid?.let { db.collection("users").document(it) },
        "track" to db.collection("tracks").document(track.id),
        "likersId" to likersId,
        "likes" to likes)
  }

  fun isLiked(profile: Profile): Boolean {
    return likersId.contains(profile.firebaseUid)
  }

  fun likeTrack(profile: Profile): ProfileTrackAssociation {
    if (this.profile != null && !likersId.contains(profile.firebaseUid)) {
      this.profile.numberOfLikes += 1
      return ProfileTrackAssociation(profile, track, likersId + profile.firebaseUid, likes + 1)
    }
    return this
  }

  fun unlikeTrack(profile: Profile): ProfileTrackAssociation {
    if (this.profile != null && likersId.contains(profile.firebaseUid)) {
      this.profile.numberOfLikes -= 1
      return ProfileTrackAssociation(profile, track, likersId - profile.firebaseUid, likes - 1)
    }
    return this
  }

  companion object {
    fun from(
        mainDocumentSnapshot: Map<String, Any>,
        profileDocumentSnapshot: DocumentSnapshot?,
        trackDocumentSnapshot: DocumentSnapshot
    ): ProfileTrackAssociation? {
      return if (trackDocumentSnapshot.exists()) {
        val profile: Profile? = profileDocumentSnapshot?.let { Profile.from(it) }
        val track: Track? = Track.from(trackDocumentSnapshot)
        val likersId = mainDocumentSnapshot["likersId"] as? List<String> ?: emptyList()
        val likes = (mainDocumentSnapshot["likes"] as? Long)?.toInt() ?: 0

        track?.let { track: Track -> ProfileTrackAssociation(profile, track, likersId, likes) }
      } else {
        null
      }
    }

  }
}
