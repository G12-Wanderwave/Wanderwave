package ch.epfl.cs311.wanderwave.model.remote

import android.util.Log
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepository
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

/**
 * This class is responsible for the connection to the Firestore database for the profiles. It is a
 * subclass of FirebaseConnection and implements the ProfileRepository interface. It is used to
 * fetch, add, update and delete profiles from the Firestore database. It also contains a reference
 * to the TrackConnection class to fetch the tracks associated with the profiles.
 *
 * @param db The Firestore database instance
 * @param ioDispatcher The dispatcher used for IO operations
 * @param trackConnection The connection to the tracks
 * @param appDatabase The local database instance
 */
class ProfileConnection(
    private val db: FirebaseFirestore,
    private val ioDispatcher: CoroutineDispatcher,
    val trackConnection: TrackConnection
) : FirebaseConnection<Profile, Profile>(db, ioDispatcher), ProfileRepository {

  override val collectionName: String = "users"

  override val getItemId = { profile: Profile -> profile.firebaseUid }

  private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

  override fun documentToItem(document: DocumentSnapshot): Profile? {
    return Profile.from(document)
  }

  override fun itemToMap(profile: Profile): Map<String, Any> {
    val profileMap: Map<String, Any> = profile.toMap(db)
    return profileMap
  }

  override fun addItem(item: Profile) {
    super.addItem(item)
    trackConnection.addItemsIfNotExist(item.topSongs)
    trackConnection.addItemsIfNotExist(item.chosenSongs)
    trackConnection.addItemsIfNotExist(item.bannedSongs)
    trackConnection.addItemsIfNotExist(item.likedSongs)
  }

  override fun addItemWithId(item: Profile) {
    super.addItemWithId(item)
    trackConnection.addItemsIfNotExist(item.topSongs)
    trackConnection.addItemsIfNotExist(item.chosenSongs)
    trackConnection.addItemsIfNotExist(item.bannedSongs)
    trackConnection.addItemsIfNotExist(item.likedSongs)
  }

  override fun updateItem(item: Profile) {
    super.updateItem(item)
    trackConnection.addItemsIfNotExist(item.topSongs)
    trackConnection.addItemsIfNotExist(item.chosenSongs)
    trackConnection.addItemsIfNotExist(item.bannedSongs)
    trackConnection.addItemsIfNotExist(item.likedSongs)
  }

  override fun documentTransform(
      document: DocumentSnapshot,
      item: Profile?
  ): Flow<Result<Profile>> =
      callbackFlow<Result<Profile>> {
        if (!document.exists()) {
          trySend(Result.failure<Profile>(Exception("Document does not exist")))
        } else {
          val profile: Profile = item ?: Profile.from(document)!!

          val likedSongsObject = document["likedSongs"]
          val topSongsObject = document["topSongs"]
          val chosenSongsObject = document["chosenSongs"]
          val bannedSongsObject = document["bannedSongs"]

          val likedSongRefs = castToListOfReferences(likedSongsObject)
          val topSongRefs = castToListOfReferences(topSongsObject)
          val chosenSongRefs = castToListOfReferences(chosenSongsObject)
          val bannedSongRefs = castToListOfReferences(bannedSongsObject)

          coroutineScope.launch {

            // The goal is to : map the references to the actual tracks by fetching, this gives
            // a list of flow,
            // then reduce the list of flow to a single flow that contains the list of tracks
            // and then combine the two lists of tracks to update the profile
            val chosenSongs = documentReferencesToFlows(chosenSongRefs, trackConnection)
            val topSongs = documentReferencesToFlows(topSongRefs, trackConnection)
            val bannedSongs = documentReferencesToFlows(bannedSongRefs, trackConnection)
            val likedSongs = documentReferencesToFlows(likedSongRefs, trackConnection)
            Log.i("ProfileConnection", "flows created")
            Log.i("ProfileConnection", "chosenSongs: $chosenSongs")
            Log.i("ProfileConnection", "topSongs: $topSongs")
            Log.i("ProfileConnection", "bannedSongs: $bannedSongs")
            Log.i("ProfileConnection", "likedSongs: $likedSongs")
            val updatedProfile =
                combine(topSongs, chosenSongs, bannedSongs, likedSongs) {
                    topSongs,
                    chosenSongs,
                    bannedSongs,
                    likedSongs ->
                  Log.i("ProfileUpdate", "Combining profiles...")

                  Log.i("ProfileUpdate", "Top Songs: Attempting to retrieve or default.")
                  val updatedTopSongs = topSongs.getOrNull()
                  Log.i("ProfileUpdate", "Chosen Songs: Attempting to retrieve or default.")
                  val updatedChosenSongs = chosenSongs.getOrNull()

                  Log.i("ProfileUpdate", "Banned Songs: Attempting to retrieve or default.")
                  val updatedBannedSongs = bannedSongs.getOrNull()

                  Log.i("ProfileUpdate", "Liked Songs: Attempting to retrieve or default.")
                  val updatedLikedSongs = likedSongs.getOrNull()

                  // Create the updated profile
                  profile
                      .copy(
                          topSongs = updatedTopSongs ?: profile.topSongs,
                          chosenSongs = updatedChosenSongs ?: profile.chosenSongs,
                          bannedSongs = updatedBannedSongs ?: profile.bannedSongs,
                          likedSongs = updatedLikedSongs ?: profile.likedSongs)
                      .also { Log.e("ProfileUpdate", "Profile combined and updated.") }
                }

            // would like to keep the flow without collecting it, but I don't know how to do
            // it...
            updatedProfile
                .map { Result.success(it) }
                .collect { result ->
                  result.onSuccess { profile ->
                    Log.i("ProfileConnection", "profile fetched")
                    trySend(Result.success(profile))
                  }
                }
          }
        }
        awaitClose {}
      }

  fun documentReferencesToFlows(
      documentReferences: List<DocumentReference>?,
      trackConnection: TrackConnection
  ): Flow<Result<List<Track>>> {
    return documentReferences
        // map to a list of flow
        ?.map { trackRef -> trackConnection.fetchTrack(trackRef) }
        // Extract the track from Result or return null if it's a failure
        ?.map { flow ->
          flow.mapNotNull { result ->
            Log.i("ProfileConnection", "track fetched")
            result.getOrNull()
          }
        }
        // map to a list of track
        ?.fold(flowOf(Result.success(listOf<Track>()))) { acc, track ->
          acc.combine(track) { accTracks, track -> accTracks.map { tracks -> tracks + track } }
        } ?: flowOf(Result.failure(Exception("Could not retrieve topSongs")))
    // reduce the lists of flows to a
    // single flows that contains the
    // list of tracks
  }

  fun castToListOfReferences(obj: Any?): List<DocumentReference> {
    return if (obj is List<*> && obj.all { it is DocumentReference }) {
      obj as List<DocumentReference>
    } else {
      emptyList()
    }
  }
}
