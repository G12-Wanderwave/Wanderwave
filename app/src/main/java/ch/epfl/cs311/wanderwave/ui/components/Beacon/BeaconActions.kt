package ch.epfl.cs311.wanderwave.ui.components.Beacon

import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.remote.FirebaseConnection
import com.google.firebase.firestore.DocumentSnapshot


class BeaconActions : FirebaseConnection<Beacon, Track>() {

    override val getItemId: (Beacon) -> String = { beacon -> beacon.id }

    override val collectionName = "beacons"

    override fun documentToItem(document: DocumentSnapshot): Beacon? {
        return if (document.exists() && document.data != null) {
            val id = document.id
            val locationMap = document.get("location") as? Map<String, Any>
            val location = locationMap?.let {
                Location(
                    latitude = it["latitude"] as? Double ?: 0.0,
                    longitude = it["longitude"] as? Double ?: 0.0,
                    name = it["name"] as? String ?: ""
                )
            } ?: throw IllegalStateException("Missing location")

            val tracks = document.get("tracks") as? List<Map<String, Any>> ?: emptyList()
            val trackList = tracks.map { mapToTrack(it) }

            Beacon(id, location, trackList)
        } else {
            null
        }
    }

    private fun mapToTrack(data: Map<String, Any>): Track {
        // Assuming Track has fields such as title and artist
        val title = data["title"] as String? ?: ""
        val artist = data["artist"] as String? ?: ""
        val id = data["id"] as String? ?: ""
        return Track(id, title, artist)
    }

    override fun itemToMap(beacon: Beacon): Map<String, Any> {
        return mapOf(
            "id" to beacon.id,
            "location" to beacon.location, // Assuming location is a map-convertible object
            "tracks" to beacon.tracks.map { track -> track.toMap() } // Ensure Track has a toMap method
        )
    }

    fun addTrackToBeacon(beaconId: String, track: Track, onComplete: (Boolean) -> Unit) {
        val beaconRef = db.collection("beacons").document(beaconId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(beaconRef)
            val beacon = snapshot.toObject(Beacon::class.java)
            beacon?.let {
                val newTracks = ArrayList(it.tracks).apply { add(track) }
                transaction.update(beaconRef, "tracks", newTracks.map { track -> track.toMap() })
            } ?: throw Exception("Beacon not found")
        }.addOnSuccessListener {
            onComplete(true)
        }.addOnFailureListener {
            onComplete(false)
        }
    }
}


