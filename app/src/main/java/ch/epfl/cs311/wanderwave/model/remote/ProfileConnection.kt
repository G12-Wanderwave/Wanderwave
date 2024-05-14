package ch.epfl.cs311.wanderwave.model.remote

import android.util.Log
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepository
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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

  override fun documentTransform(document: DocumentSnapshot, dataFlow: MutableStateFlow<Profile?>) {
    val profile = dataFlow.value ?: Profile.from(document)

    profile?.let { profile ->
      val topSongsObject = document["topSongs"]
      val chosenSongsObject = document["chosenSongs"]

      var topSongRefs: List<DocumentReference>?
      var chosenSongRefs: List<DocumentReference>?

      if (topSongsObject is List<*> &&
          topSongsObject.all { it is DocumentReference } &&
          chosenSongsObject is List<*> &&
          chosenSongsObject.all { it is DocumentReference }) {
        topSongRefs = topSongsObject as? List<DocumentReference>
        chosenSongRefs = chosenSongsObject as? List<DocumentReference>

        // Use a coroutine to perform asynchronous operations
        coroutineScope.launch {
          val TopSongsDeferred =
              topSongRefs?.map { trackRef -> async { trackConnection.fetchTrack(trackRef) } }
          val chosenSongsDeffered =
              chosenSongRefs?.map { trackRef -> async { trackConnection.fetchTrack(trackRef) } }

          // Wait for all tracks to be fetched
          val TopSongs = TopSongsDeferred?.mapNotNull { it?.await() }
          val ChosenSongs = chosenSongsDeffered?.mapNotNull { it?.await() }

          // Update the beacon with the complete list of tracks
          val updatedBeacon =
              profile.copy(
                  topSongs = TopSongs ?: emptyList(), chosenSongs = ChosenSongs ?: emptyList())
          dataFlow.value = updatedBeacon
        }
      } else {
        Log.e("Firestore", "songs lists have a Wrong Firebase Format")
      }
    }
  }
}
