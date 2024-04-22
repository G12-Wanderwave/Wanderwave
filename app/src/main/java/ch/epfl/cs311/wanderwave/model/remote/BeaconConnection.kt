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

class BeaconConnection(private val database: FirebaseFirestore? = null) :
    FirebaseConnection<Beacon, Beacon>() {

  override val collectionName: String = "beacons"

  override val getItemId = { beacon: Beacon -> beacon.id }

  val trackConnection = TrackConnection()

  override val db = database ?: super.db

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
          Log.d("Firestore", "DocumentSnapshot data: ${document.data}")
          if (document != null && document.data != null) {
            Log.d("Firestore", "DocumentSnapshot data: ${document.data}")
            documentToItem(document)?.let { beacon ->
              val trackRefs = document.get("tracks") as? List<DocumentReference>
              val tracks = mutableListOf<Track>()
              trackRefs?.forEach { trackRef ->
                trackRef.get().addOnSuccessListener { trackDocument ->
                  val trackData = trackDocument.data
                  if (trackData != null) {
                    Track.from(trackDocument)?.let { tracks.add(it) }
                  }
                  val updatedBeacon = beacon.copy(tracks = tracks)
                  dataFlow.value = updatedBeacon
                }
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
