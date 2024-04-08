package ch.epfl.cs311.wanderwave.model.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

data class Beacon(

  /** GUID of the beacon */
  val id: String,

  /** Location of the beacon */
  val location: Location,

  /** List of tracks that are broadcast from the beacon */
  /** Don't know if it's a good thing to use flows here, but as the tracks are red asynchronously, it might be useful */
  val tracks: List<Track> = listOf<Track>(),
)
