package ch.epfl.cs311.wanderwave.model.remote

import android.util.Log
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepository
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
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

class ProfileConnection(
    private val database: FirebaseFirestore,
    val trackConnection: TrackConnection
) : FirebaseConnection<Profile, Profile>(database), ProfileRepository {

  override val collectionName: String = "users"

  override val getItemId = { profile: Profile -> profile.firebaseUid }

  override val db = database
  private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

  override fun isUidExisting(spotifyUid: String, callback: (Boolean, Profile?) -> Unit) {
    db.collection("users")
        .whereEqualTo("spotifyUid", spotifyUid)
        .get()
        .addOnSuccessListener { documents ->
          val isExisting = documents.size() > 0
          callback(isExisting, if (isExisting) documentToItem(documents.documents[0]) else null)
        }
        .addOnFailureListener { exception ->
          Log.w("Firestore", "Error getting documents: ", exception)
          callback(false, null) // Assuming failure means document doesn't exist
        }
  }

  // Document to Profile
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
  }

  override fun addItemWithId(item: Profile) {
    super.addItemWithId(item)
    trackConnection.addItemsIfNotExist(item.topSongs)
    trackConnection.addItemsIfNotExist(item.chosenSongs)
  }

  override fun documentTransform(
      document: DocumentSnapshot,
      item: Profile?
  ): Flow<Result<Profile>> =
      callbackFlow<Result<Profile>> {
        if (!document.exists()) {
          trySend(Result.failure<Profile>(Exception("Document does not exist")))
        } else {
          val profile: Profile? = item ?: Profile.from(document)

          profile!!.let { profile ->
            val topSongsObject = document["topSongs"]
            val chosenSongsObject = document["chosenSongs"]
            val bannedSongsObject = document["bannedSongs"]
            val likedSongsObject = document["likedSongs"]

            if (isValidObject(topSongsObject) || isValidObject(chosenSongsObject) || isValidObject(
                    bannedSongsObject
                ) || isValidObject(likedSongsObject)  ){
              val topSongRefs = topSongsObject as? List<DocumentReference> ?: emptyList()
              val chosenSongRefs = chosenSongsObject as? List<DocumentReference> ?: emptyList()
              val bannedSongRefs = bannedSongsObject as? List<DocumentReference> ?: emptyList()
              val likedSongRefs = likedSongsObject as? List<DocumentReference> ?: emptyList()

              coroutineScope.launch {

                // The goal is to : map the references to the actual tracks by fetching, this gives
                // a list of flow,
                // then reduce the list of flow to a single flow that contains the list of tracks
                // and then combine the two lists of tracks to update the profile

                val chosenSongs = documentReferencesToFlows(chosenSongRefs, trackConnection)
                val topSongs = documentReferencesToFlows(topSongRefs, trackConnection)
                val bannedSongs = documentReferencesToFlows(bannedSongRefs, trackConnection)
                val likedSongs = documentReferencesToFlows(likedSongRefs, trackConnection)

                val updatedProfile = topSongs.combine(chosenSongs) { topSongs, chosenSongs ->
                  Pair(topSongs, chosenSongs)
                }.combine(bannedSongs) { pair, bannedSongs ->
                  Triple(pair.first, pair.second, bannedSongs)
                }.combine(likedSongs) { triple, likedSongs ->
                  // if any of the four have a success value, we update the profile,
                  // else we return the profile as is
                  if (triple.first.isSuccess || triple.second.isSuccess || triple.third.isSuccess || likedSongs.isSuccess) {
                    profile.copy(
                      topSongs = triple.first.getOrNull() ?: profile.topSongs,
                      chosenSongs = triple.second.getOrNull() ?: profile.chosenSongs,
                      bannedSongs = triple.third.getOrNull() ?: profile.bannedSongs,
                      likedSongs = likedSongs.getOrNull() ?: profile.likedSongs
                    )
                  } else {
                    profile
                  }
                }

                // would like to keep the flow without collecting it, but I don't know how to do
                // it...
                updatedProfile
                    .map { Result.success(it) }
                    .collect { result ->
                      result.onSuccess { profile -> trySend(Result.success(profile)) }
                    }
              }
            } else {
              trySend(
                  Result.failure<Profile>(Exception("Songs lists have a Wrong Firebase Format")))
            }
          }
        }
        awaitClose {}
      }

  private fun documentReferencesToFlows(
      documentReferences: List<DocumentReference>?,
      trackConnection: TrackConnection
  ): Flow<Result<List<Track>>> {
    return documentReferences
        // map to a list of flow
        ?.map { trackRef -> trackConnection.fetchTrack(trackRef) }
        // Extract the track from Result or return null if it's a failure
        ?.map { flow -> flow.mapNotNull { result -> result.getOrNull() } }
        // map to a list of track
        ?.fold(flowOf(Result.success(listOf<Track>()))) { acc, track ->
          acc.combine(track) { accTracks, track ->
            accTracks.map { tracks -> tracks + track }
          }
        } ?: flowOf(Result.failure(Exception("Could not retrieve topSongs")))
    // reduce the lists of flows to a
    // single flows that contains the
    // list of tracks
  }

  fun isValidObject(obj: Any?): Boolean {
    return obj is List<*> && obj.all { it is Map<*, *> }
  }
}
