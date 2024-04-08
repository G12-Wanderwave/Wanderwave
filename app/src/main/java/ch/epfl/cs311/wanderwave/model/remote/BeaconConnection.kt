package ch.epfl.cs311.wanderwave.model.remote

import android.util.Log
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import ch.epfl.cs311.wanderwave.model.data.Track
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow

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

    val trackRefs = document.get("tracks") as? List<DocumentReference>
    val tracks = listOf<Track>()

    return Beacon(
        id = id,
        location = location,
        tracks = tracks)
  }

  override fun getItem(itemId: String): Flow<Beacon> {
    val dataFlow = MutableStateFlow<Beacon?>(null)
    db.collection(collectionName)
        .document(itemId)
        .get()
        .addOnSuccessListener { document ->
            if (document != null && document.data != null) {
                val beacon = documentToItem(document)
                val trackRefs = document.get("tracks") as? List<DocumentReference>
                val tracks = mutableListOf<Track>()
                trackRefs?.forEach { trackRef ->
                    trackRef.get().addOnSuccessListener { trackDocument ->
                        val trackData = trackDocument.data
                        Log.d("Firestore", "TrackData: $trackData")
                        if (trackData != null) {
                            val track = Track(
                                id = trackDocument.id as? String ?: "",
                                title = trackData["title"] as? String ?: "",
                                artist = trackData["artist"] as? String ?: ""
                            )
                            // Log.d("Firestore", "Track: $track")
                            tracks.add(track)
                        }
                        val updatedBeacon = beacon.copy(tracks = tracks)
                        dataFlow.value = updatedBeacon
                        Log.d("Firestore", "Updated Beacon: $updatedBeacon")
                    }
                }
            }
        }
        .addOnFailureListener { e -> Log.e("Firestore", "Error getting document: ", e) }

    return dataFlow.filterNotNull()
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
