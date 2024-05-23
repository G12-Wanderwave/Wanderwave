package ch.epfl.cs311.wanderwave.model.data

/**
 * This class represents the association between a profile and a track.
 *
 * @param profile The profile associated with the track.
 * @param track The track associated with the profile.
 * @author Menzo Bouaissi
 * @since 2.0
 * @last update 2.0
 */
data class ProfileTrackAssociation(
    val profile: Profile? = null,
    val track: Track,
    val likersId: List<String> = emptyList(),
    val likes: Int = 0
) {

  fun toMap(): Map<String, Any?> {
    return hashMapOf("profile" to profile?.toMap(), "track" to track.toMap())
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
}
