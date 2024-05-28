package ch.epfl.cs311.wanderwave.model.utils

import android.util.Log
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.BeaconRepository
import ch.epfl.cs311.wanderwave.model.repository.TrackRepository

fun addTrackToBeacon(
    beaconId: String,
    track: Track,
    trackRepository: TrackRepository,
    beaconRepository: BeaconRepository,
    authenticationController: AuthenticationController,
    onComplete: (Boolean) -> Unit,
) {
  // Call the BeaconConnection's addTrackToBeacon with the provided beaconId and trac
  val correctTrack = track.copy(id = "spotify:track:" + track.id)
    Log.d("BeaconInteraction", "Adding track to beacon$correctTrack")
  trackRepository.addItemsIfNotExist(listOf(correctTrack))
  beaconRepository.addTrackToBeacon(
      beaconId, correctTrack, authenticationController.getUserData()!!.id, onComplete)
}
