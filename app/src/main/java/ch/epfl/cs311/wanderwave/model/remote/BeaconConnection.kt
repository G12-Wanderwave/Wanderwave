package ch.epfl.cs311.wanderwave.model.remote

import android.util.Log
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.ProfileTrackAssociation
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.data.TrackRecord
import ch.epfl.cs311.wanderwave.model.localDb.AppDatabase
import ch.epfl.cs311.wanderwave.model.repository.BeaconRepository
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

/**
 * This class is responsible for the connection to the Firestore database for the beacons. It is a
 * subclass of FirebaseConnection and implements the BeaconRepository interface. It is used to
 * fetch, add, update and delete beacons from the Firestore database. It also contains a reference
 * to the TrackConnection and ProfileConnection classes to fetch the tracks and profiles associated
 * with the beacons.
 *
 * @param db The Firestore database instance
 * @param ioDispatcher The dispatcher used for IO operations
 * @param trackConnection The connection to the tracks
 * @param profileConnection The connection to the profiles
 * @param appDatabase The local database instance
 */
class BeaconConnection(
    private val db: FirebaseFirestore,
    private val ioDispatcher: CoroutineDispatcher,
    val trackConnection: TrackConnection,
    val profileConnection: ProfileConnection,
    private val appDatabase: AppDatabase,
) : FirebaseConnection<Beacon, Beacon>(db, ioDispatcher), BeaconRepository {

  override val collectionName: String = "beacons"

  override val getItemId = { beacon: Beacon -> beacon.id }

  // You can create a CoroutineScope instance
  private val coroutineScope = CoroutineScope(Dispatchers.Main)

  // Document to Beacon
  override fun documentToItem(document: DocumentSnapshot): Beacon? {
    return Beacon.from(document)
  }

  override fun addItem(item: Beacon) {
    super.addItem(item)
    trackConnection.addItemsIfNotExist(item.profileAndTrack.map { it.track })
  }

  override fun addItemWithId(item: Beacon) {
    super.addItemWithId(item)
    trackConnection.addItemsIfNotExist(item.profileAndTrack.map { it.track })
  }

  override suspend fun addItemAndGetId(item: Beacon): String? {
    val id = super.addItemAndGetId(item)
    id?.let { trackConnection.addItemsIfNotExist(item.profileAndTrack.map { it.track }) }
    return id
  }

  override fun updateItem(item: Beacon) {
    super.updateItem(item)
    trackConnection.addItemsIfNotExist(item.profileAndTrack.map { it.track })
  }

  override fun getItem(itemId: String): Flow<Result<Beacon>> {
    return super.getItem(itemId)
  }

  override fun documentTransform(document: DocumentSnapshot, item: Beacon?): Flow<Result<Beacon>> =
      callbackFlow {
        if (!document.exists()) {
          trySend(Result.failure(Exception("Document does not exist")))
        } else {
          val beacon: Beacon = item ?: Beacon.from(document)!!

          val tracksObject = document["tracks"]

          if (tracksObject is List<*> && tracksObject.all { it is Map<*, *> }) {
            val profileAndTrackRefs = tracksObject as? List<Map<String, Any>>

            coroutineScope.launch {
              val profileAndTracks =
                  profileAndTrackRefs
                      // Fetch the profile and track from the references
                      ?.mapNotNull { profileAndTrackRef ->
                        trackConnection.fetchProfileAndTrack(profileAndTrackRef)
                      }
                      // map the list of flow of Result<ProfileTrackAssociation> to a flow of
                      // Result<List<ProfileTrackAssociation>>
                      ?.mapNotNull { flow -> flow.mapNotNull { result -> result.getOrNull() } }
                      // reduce the list of flow to a single flow that contains the list of
                      // ProfileTrackAssociation
                      ?.fold(flowOf(Result.success(listOf<ProfileTrackAssociation>()))) { acc, track
                        ->
                        acc.combine(track) { accTracks, track ->
                          accTracks.map { tracks -> tracks + track }
                        }
                      } ?: flowOf(Result.failure(Exception("Could not retrieve chosenSongs")))

              // Send the original beacon first
              trySend(Result.success(beacon))

              // Update the beacon with the profile and track
              profileAndTracks.collect { result ->
                result.onSuccess { profileAndTracks ->
                  val updatedBeacon = beacon.copy(profileAndTrack = profileAndTracks)
                  trySend(Result.success(updatedBeacon))
                }
                result.onFailure { exception ->
                  trySend(Result.success(beacon))
                  Log.e("Firestore", "Error getting profile and track: ", exception)
                }
              }
            }
          } else {
            trySend(Result.success(beacon))
            Log.e("Firestore", "Tracks are not in the correct format ")
          }
        }
        awaitClose {}
      }

  override fun getAll(): Flow<List<Beacon>> {
    val dataFlow = MutableStateFlow<List<Beacon>?>(null)

    CoroutineScope(ioDispatcher).launch {
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
    }

    return dataFlow.filterNotNull()
  }

  override fun itemToMap(beacon: Beacon): Map<String, Any> = beacon.toMap(db)

  override fun addTrackToBeacon(
      beaconId: String,
      track: Track,
      profileUid: String,
      onComplete: (Boolean) -> Unit
  ) {
    val beaconRef = db.collection(collectionName).document(beaconId)
    val profileRef = db.collection(profileConnection.collectionName).document(profileUid)
    db.runTransaction { transaction ->
          val snapshot: DocumentSnapshot = transaction[beaconRef]
          val beacon = Beacon.from(snapshot)
          val tracks = snapshot.get("tracks") as? List<Map<String, Any>> ?: listOf()
          val associations =
              tracks.mapNotNull {
                val creatorRef = it["creator"] as? DocumentReference
                val trackRef = it["track"] as? DocumentReference

                val creatorSnapshot = creatorRef?.let { transaction[it] }
                val trackSnapshot = trackRef?.let { transaction[it] }

                trackSnapshot?.let { trackSnapshot ->
                  ProfileTrackAssociation.from(
                      it,
                      profileDocumentSnapshot = creatorSnapshot,
                      trackDocumentSnapshot = trackSnapshot)
                } ?: null
              }

          beacon?.let { beaconNotNull ->
            val trackId =
                if (track.id.contains("spotify:track:")) track.id else "spotify:track:" + track.id
            val trackRef = db.collection(trackConnection.collectionName).document(trackId)
            val newTrack =
                hashMapOf(
                    "creator" to profileRef,
                    "track" to trackRef,
                    "likersId" to emptyList<String>(),
                    "likes" to 0)

            transaction.update(beaconRef, "tracks", FieldValue.arrayUnion(newTrack))

            // After updating Firestore, save the track addition locally
            coroutineScope.launch {
              appDatabase
                  .trackRecordDao()
                  .insertTrackRecord(
                      TrackRecord(
                          beaconId = beaconId,
                          trackId = track.id,
                          timestamp = System.currentTimeMillis()))
            }
          } ?: Log.e("Firestore", "Error getting profile")
        }
        .addOnSuccessListener { onComplete(true) }
        .addOnFailureListener { onComplete(false) }
  }
}
