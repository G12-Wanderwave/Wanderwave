package ch.epfl.cs311.wanderwave.model.remote

import android.util.Log
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.model.data.Profile
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import ch.epfl.cs311.wanderwave.model.data.Track

class BeaconConnection : FirebaseConnectionInt<Beacon, Beacon> {

  override val collectionName: String = "beacons"

  override val getItemId = { beacon: Beacon -> beacon.id }

  private val db = FirebaseFirestore.getInstance()

  // Document to Beacon
  override fun documentToItem(document: DocumentSnapshot): Beacon {
    val id = document.id
    val location = document.getGeoPoint("location")?.let { Location(it.latitude, it.longitude) } ?: Location(0.0, 0.0)
    val tracks = listOf<Track>()

    return Beacon(
        id = id,
        location = location,
        tracks = tracks)
  }

  override fun itemToHash(beacon: Beacon): HashMap<String, Any> {
    val beaconMap: HashMap<String, Any> =
        hashMapOf(
            "id" to beacon.id,
            "location" to hashMapOf("latitude" to beacon.location.latitude, "longitude" to beacon.location.longitude),
            "tracks" to beacon.tracks.map { it.toHash() })
    return beaconMap
  }
}
