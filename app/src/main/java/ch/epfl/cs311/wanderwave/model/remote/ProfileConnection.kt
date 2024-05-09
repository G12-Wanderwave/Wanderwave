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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileConnection(
    private val database: FirebaseFirestore? = null,
    val trackConnection: TrackConnection
) : FirebaseConnection<Profile, Profile>(), ProfileRepository {

  override val collectionName: String = "users"

  override val getItemId = { profile: Profile -> profile.firebaseUid }

  override val db = database ?: super.db
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

  override fun itemToMap(profile: Profile): HashMap<String, Any> {
    val profileMap: HashMap<String, Any> = profile.toMap(db)
    Log.d("ProfileConnection", "profileMap: $profileMap")
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

  fun addProfilesIfNotExist(profiles: List<Profile?>) {
    coroutineScope.launch {
      profiles.filterNotNull().forEach { profile ->
        val querySnapshot =
            db.collection(collectionName)
                .whereEqualTo("firebaseUid", profile.firebaseUid)
                .get()
                .await()
        if (querySnapshot.isEmpty) {
          addItemWithId(profile)
        }
      }
    }
  }

  override fun getItem(itemId: String): Flow<Profile> {
    Log.d("Firestore", "getItem 1: $itemId")
    return getItem(itemId) { _, _ -> }
  }

  override fun getItem(
      itemId: String,
      onSuccess: (DocumentSnapshot, MutableStateFlow<Profile?>) -> Unit
  ): Flow<Profile> {

    Log.d("Firestore", "getItem 2: $itemId")

    val onSuccessWrapper: (DocumentSnapshot, MutableStateFlow<Profile?>) -> Unit =
        { document, dataFlow ->
          val profile = dataFlow.value ?: Profile.from(document)

          Log.d("Firestore", "profile : $profile")

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

              Log.d("Firestore", "refs : $topSongRefs $chosenSongRefs")

              // Use a coroutine to perform asynchronous operations
              coroutineScope.launch {
                val TopSongsDeferred =
                    topSongRefs?.map { trackRef -> async { trackConnection.fetchTrack(trackRef) } }
                val chosenSongsDeffered =
                    chosenSongRefs?.map { trackRef ->
                      async { trackConnection.fetchTrack(trackRef) }
                    }

                // Wait for all tracks to be fetched
                val TopSongs = TopSongsDeferred?.mapNotNull { it?.await() }
                val ChosenSongs = chosenSongsDeffered?.mapNotNull { it?.await() }

                // Update the beacon with the complete list of tracks
                val updatedBeacon =
                    profile.copy(
                        topSongs = TopSongs ?: emptyList(),
                        chosenSongs = ChosenSongs ?: emptyList())
                dataFlow.value = updatedBeacon
              }
              onSuccess(document, dataFlow)
            } else {
              Log.e("Firestore", "songs lists have a Wrong Firebase Format")
            }
          }
        }

    return super.getItem(itemId, onSuccessWrapper)
  }
}
