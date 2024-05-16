package ch.epfl.cs311.wanderwave.model.remote

import android.util.Log
import androidx.compose.runtime.collectAsState
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepository
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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

  override fun documentTransform(document: DocumentSnapshot,item: Profile?): Flow<Result<Profile>> = callbackFlow<Result<Profile>>{
      if (document == null || !document.exists()) {
          Result.failure<Profile>(Exception("Document does not exist"))
      }
      val profile: Profile? = item ?: Profile.from(document)

      profile?.let { profile ->
          val topSongsObject = document["topSongs"]
          val chosenSongsObject = document["chosenSongs"]

          if (topSongsObject is List<*> && topSongsObject.all { it is DocumentReference } &&
              chosenSongsObject is List<*> && chosenSongsObject.all { it is DocumentReference }) {
              val topSongRefs = topSongsObject as? List<DocumentReference>
              val chosenSongRefs = chosenSongsObject as? List<DocumentReference>

              coroutineScope.launch {

                // The goal is to : map the references to the actual tracks by fetching, this gives a list of flow,
                // then reduce the list of flow to a single flow that contains the list of tracks
                // and then combine the two lists of tracks to update the profile

                  val chosenSongs = chosenSongRefs
                    ?.map { trackRef -> trackConnection.fetchTrack(trackRef) }
                    ?.map { flow ->
                      flow.mapNotNull { result ->
                        result.getOrNull()
                      }
                    }
                    ?.fold(flowOf(Result.success(listOf<Track>()))) { acc, track ->
                      acc.combine(track) { accTracks, track ->
                        accTracks.map { tracks -> tracks + track }
                      }
                    } ?: flowOf(Result.failure(Exception("Could not retrieve chosenSongs")))

                  val topSongs = topSongRefs
                    ?.map { trackRef -> trackConnection.fetchTrack(trackRef) } // map to a list of flow
                    ?.map { flow ->
                      flow.mapNotNull { result ->
                        result.getOrNull() // Extract the track from Result or return null if it's a failure
                      }
                    } // map to a list of track
                    ?.fold(flowOf(Result.success(listOf<Track>()))) { acc, track ->
                      acc.combine(track) { accTracks, track ->
                        accTracks.map { tracks -> tracks + track }
                      }
                    } ?: flowOf(Result.failure(Exception("Could not retrieve topSongs")))// reduce the list of flow to a single flow that contains the list of tracks

                  val updatedProfile = topSongs.combine(chosenSongs) { topSongs, chosenSongs ->
                    // if one of the two or the two have a success value, we update the profile, else we return the profile as is
                      if (topSongs.isSuccess || chosenSongs.isSuccess) {
                        profile.copy(topSongs = topSongs.getOrNull() ?: profile.topSongs, chosenSongs = chosenSongs.getOrNull() ?: profile.chosenSongs)
                      } else {
                        profile
                      }
                  }
                  updatedProfile.map { Result.success(it) }
              }
          } else {
              Result.failure<Profile>(Exception("Songs lists have a Wrong Firebase Format"))
          }
      } ?: run {
          Result.failure<Profile>(Exception("The profile is not in the correct format or could not be fetched"))
      }
  }
}
