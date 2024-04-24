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
  // TODO: Implement the methods: we have a beacon => in the beacon store the id of the track and
  // the id of the profile
  // When we are near a beacon, retrieve the tuple (trackID, profileID), and then recover the
  // different data from the latter
  // and associate it

}
