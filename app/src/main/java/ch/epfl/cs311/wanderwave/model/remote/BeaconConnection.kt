package ch.epfl.cs311.wanderwave.model.remote

import android.util.Log
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.data.ProfileTrackAssociation
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.BeaconRepository
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class BeaconConnection(
    private val database: FirebaseFirestore? = null,
    val trackConnection: TrackConnection,
    val profileConnection: ProfileConnection
) : FirebaseConnection<Beacon, Beacon>(), BeaconRepository {

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
    trackConnection.addItemsIfNotExist(item.profileAndTrack.map { it.track })
    profileConnection.addProfilesIfNotExist(item.profileAndTrack.map { it.profile })
    Log.d("BeaconConnection","Beacon added successfully")
  }

  override fun addItemWithId(item: Beacon) {
    super.addItemWithId(item)
    trackConnection.addItemsIfNotExist(item.profileAndTrack.map { it.track })
    profileConnection.addProfilesIfNotExist(item.profileAndTrack.map { it.profile })
  }

  override fun updateItem(item: Beacon) {
    super.updateItem(item)
    trackConnection.addItemsIfNotExist(item.profileAndTrack.map { it.track })
    profileConnection.addProfilesIfNotExist(item.profileAndTrack.map { it.profile })
  }

  override fun getItem(itemId: String): Flow<Beacon> {
    return getItem(itemId) { _, _ -> }
  }

  override fun getItem(
      itemId: String,
      onSuccess: (DocumentSnapshot, MutableStateFlow<Beacon?>) -> Unit
  ): Flow<Beacon> {
    val onSuccessWrapper: (DocumentSnapshot, MutableStateFlow<Beacon?>) -> Unit =
        { document, dataFlow ->
          val beacon =
              dataFlow.value
                  ?: Beacon.from(document)
                  ?: Beacon(
                      id = document.id,
                      location = Location(0.0, 0.0),
                      profileAndTrack = emptyList())

          val tracksObject = document["tracks"]

          var profileAndTrackRefs: List<Map<String, DocumentReference>>?

          if (tracksObject is List<*> && tracksObject.all { it is Map<*, *> }) {
            profileAndTrackRefs = tracksObject as? List<Map<String, DocumentReference>>

            // Use a coroutine to perform asynchronous operations
            coroutineScope.launch {
              val profileAndTracksDeferred =
                  profileAndTrackRefs?.map { profileAndTrackRef ->
                    async { trackConnection.fetchProfileAndTrack(profileAndTrackRef) }
                  }

              // Wait for all tracks to be fetched
              val profileAndTracks = profileAndTracksDeferred?.mapNotNull { it?.await() }

              // Update the beacon with the complete list of tracks
              val updatedBeacon = beacon.copy(profileAndTrack = profileAndTracks ?: emptyList())
              dataFlow.value = updatedBeacon
            }
            onSuccess(document, dataFlow)
          } else {
            Log.e("Firestore", "tracks has Wrong Firebase Format")
          }
        }

    return super.getItem(itemId, onSuccessWrapper)
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
                beacon.profileAndTrack.map { profileAndTrack ->
                  hashMapOf(
                      "creator" to
                          db.collection("users").document(profileAndTrack.profile.firebaseUid),
                      "track" to db.collection("tracks").document(profileAndTrack.track.id))
                })
    return beaconMap
  }

  override fun addTrackToBeacon(beaconId: String, track: Track, onComplete: (Boolean) -> Unit) {
    val beaconRef = db.collection("beacons").document(beaconId)
    db.runTransaction { transaction ->
          val snapshot = transaction.get(beaconRef)
          val beacon = Beacon.from(snapshot)
          beacon?.let {
            val newTracks =
                ArrayList(it.profileAndTrack).apply {
                  add(
                      ProfileTrackAssociation(
                          Profile(
                              "Sample First Name",
                              "Sample last name",
                              "Sample desc",
                              0,
                              false,
                              null,
                              "My Firebase UID",
                              track.id),
                          track))
                }
            transaction.update(beaconRef, "tracks", newTracks.map { it.toMap() })
          } ?: throw Exception("Beacon not found")
        }
        .addOnSuccessListener { onComplete(true) }
        .addOnFailureListener { onComplete(false) }
  }
}
