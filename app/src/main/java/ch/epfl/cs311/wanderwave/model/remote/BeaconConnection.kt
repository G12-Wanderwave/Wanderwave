package ch.epfl.cs311.wanderwave.model.remote

import android.util.Log
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.BeaconRepository
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class BeaconConnection
@Inject
constructor(private val database: FirebaseFirestore? = null, val trackConnection: TrackConnection) :
    FirebaseConnection<Beacon, Beacon>(), BeaconRepository {

  override val collectionName: String = "beacons"

  override val getItemId = { beacon: Beacon -> beacon.id }

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
              dataFlow.value = beacon

              val tracksObject = document.get("tracks")

              var trackRefs: List<Map<String, DocumentReference>>? = null

              if (tracksObject is List<*> && tracksObject.all { it is Map<*, *> }) {
                trackRefs = tracksObject as List<Map<String, DocumentReference>>
                // Continue with your code
              } else {
                Log.e("Firestore", "tracks is not a List<Map<String, DocumentReference>> (Wrong Firebase Format)")
              }

              // Use a coroutine to perform asynchronous operations
              coroutineScope.launch {
                val tracksDeferred =
                    trackRefs?.map { trackRef ->
                      // track ref is a map with a single key "track" and a DocumentReference value
                        async { fetchTrack(trackRef.get("track")) }
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

  // Fetch a track from a DocumentReference asynchronously
  suspend fun fetchTrack(trackRef: DocumentReference?): Track? {
    if(trackRef == null) return null
    return withContext(Dispatchers.IO) {
      try {
        val trackDocument = trackRef?.get()?.await()
        if (trackDocument != null) {
          Track.from(trackDocument)
        } else {
          null
        }
      } catch (e: Exception) {
        Log.e("Firestore", "Error fetching track: ${e.message}")
        null
      }
    }
  }

  override fun getAll(): Flow<List<Beacon>> {
    val dataFlow = MutableStateFlow<List<Beacon>?>(null)

    db.collection(collectionName)
        .get()
        .addOnSuccessListener { documents ->
          val beacons = documents.mapNotNull { documentToItem(it) }
          dataFlow.value = beacons
        }
        .addOnFailureListener { e ->
          dataFlow.value = null
          Log.e("Firestore", "Error getting documents: ", e)
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

  override fun addTrackToBeacon(beaconId: String, track: Track, onComplete: (Boolean) -> Unit) {
    val beaconRef = db.collection("beacons").document(beaconId)
    db.runTransaction { transaction ->
          val snapshot = transaction.get(beaconRef)
          val beacon = Beacon.from(snapshot)
          beacon?.let {
            val newTracks = ArrayList(it.tracks).apply { add(track) }
            transaction.update(beaconRef, "tracks", newTracks.map { it.toMap() })
          } ?: throw Exception("Beacon not found")
        }
        .addOnSuccessListener { onComplete(true) }
        .addOnFailureListener { onComplete(false) }
  }
}
