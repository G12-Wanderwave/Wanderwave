package ch.epfl.cs311.wanderwave.model.remote

import android.util.Log
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Track
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class BeaconConnection(private val database: FirebaseFirestore? = null) :
    FirebaseConnection<Beacon, Beacon>() {

  override val collectionName: String = "beacons"

  override val getItemId = { beacon: Beacon -> beacon.id }

  val trackConnection = TrackConnection()

  override val db = database ?: super.db

  // You can create a CoroutineScope instance
  private val coroutineScope = CoroutineScope(Dispatchers.Main)

  // Document to Beacon
  override fun documentToItem(document: DocumentSnapshot): Beacon? {
    return Beacon.from(document)
  }

  override fun addItem(item: Beacon) {
    super.addItem(item)
    trackConnection.addItemsIfNotExist(item.tracks)
  }

  override fun addItemWithId(item: Beacon) {
    super.addItemWithId(item)
    trackConnection.addItemsIfNotExist(item.tracks)
  }

  override fun updateItem(item: Beacon) {
    super.updateItem(item)
    trackConnection.addItemsIfNotExist(item.tracks)
  }

  override fun getItem(itemId: String): Flow<Beacon> {
    val dataFlow = MutableStateFlow<Beacon?>(null)


    db.collection(collectionName)
        .document(itemId)
        .get()
        .addOnSuccessListener { document ->
          if (document != null && document.data != null) {
            documentToItem(document)?.let { beacon ->

              val trackRefs = document.get("tracks") as? List<DocumentReference>
              val tracks = mutableListOf<Track>()

              // Use a coroutine to perform asynchronous operations
              coroutineScope.launch {
                val tracksDeferred = trackRefs?.map { trackRef ->
                  async(Dispatchers.IO) {
                    try {
                      val trackDocument = trackRef.get().await()
                      trackDocument.toObject(Track::class.java)
                    } catch (e: Exception) {
                      // Handle exceptions
                      Log.e("Firestore", "Error fetching track: ${e.message}")
                      null
                    }
                  }
                }

                // Wait for all tracks to be fetched
                val tracks = tracksDeferred?.mapNotNull { it?.await() }

                // Update the beacon with the complete list of tracks
                val updatedBeacon = beacon.copy(tracks = tracks ?: emptyList())
                dataFlow.value = updatedBeacon
              }

            }
          } else {
            dataFlow.value = null
          }
        }
        .addOnFailureListener { e ->
          dataFlow.value = null
          Log.e("Firestore", "Error getting document: ", e)
        }

    return dataFlow.filterNotNull()
  }

  override fun itemToMap(beacon: Beacon): Map<String, Any> {
    val beaconMap: HashMap<String, Any> =
        hashMapOf(
            "id" to beacon.id,
            "location" to beacon.location.toMap(),
            "tracks" to
                beacon.tracks.map { track ->
                  db.collection(trackConnection.collectionName).document(track.id)
                })
    return beaconMap
  }
}
