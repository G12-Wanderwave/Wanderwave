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
data class ProfileTrackAssociation(val profile: Profile, val track: Track) {

  fun toMap(): Map<String, Any> {
    return hashMapOf("profile" to profile.toMap(), "track" to track.toMap())
  }
}
