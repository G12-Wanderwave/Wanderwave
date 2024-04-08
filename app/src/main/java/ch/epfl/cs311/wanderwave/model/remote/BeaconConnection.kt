package ch.epfl.cs311.wanderwave.model.remote

import android.util.Log
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
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
    val locationMap = document.get("location") as? Map<String, Any>
    val latitude = locationMap?.get("latitude") as? Double ?: 0.0
    val longitude = locationMap?.get("longitude") as? Double ?: 0.0
    val name = locationMap?.get("name") as? String ?: ""
    val location = Location(latitude, longitude,name)
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
            "location" to hashMapOf("latitude" to beacon.location.latitude, "longitude" to beacon.location.longitude, "name" to beacon.location.name),
            "tracks" to beacon.tracks.map { it.toHash() })
    return beaconMap
  }
}
