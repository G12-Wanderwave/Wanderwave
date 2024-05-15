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

  override fun documentTransform(document: DocumentSnapshot, dataFlow: MutableStateFlow<Result<Profile>>) {
      if (document == null || !document.exists()) {
          dataFlow.value = Result.failure(Exception("Document does not exist"))
          return
      }

      val profile: Profile? = if (dataFlow.value.isSuccess) {
          dataFlow.value.getOrNull()
      } else {
          Profile.from(document)
      }

      profile?.let { profile ->
          val topSongsObject = document["topSongs"]
          val chosenSongsObject = document["chosenSongs"]

          if (topSongsObject is List<*> && topSongsObject.all { it is DocumentReference } &&
              chosenSongsObject is List<*> && chosenSongsObject.all { it is DocumentReference }) {
              val topSongRefs = topSongsObject as? List<DocumentReference>
              val chosenSongRefs = chosenSongsObject as? List<DocumentReference>

              coroutineScope.launch {
                  val topSongsDeferred = topSongRefs?.map { trackRef -> async { trackConnection.fetchTrack(trackRef) } }
                  val chosenSongsDeffered = chosenSongRefs?.map { trackRef -> async { trackConnection.fetchTrack(trackRef) } }

                  val topSongs = topSongsDeferred?.mapNotNull { it.await() }
                  val chosenSongs = chosenSongsDeffered?.mapNotNull { it.await() }

                  val updatedProfile = profile.copy(topSongs = topSongs ?: emptyList(), chosenSongs = chosenSongs ?: emptyList())
                  dataFlow.value = Result.success(updatedProfile)
              }
          } else {
              dataFlow.value = Result.failure(Exception("Songs lists have a Wrong Firebase Format"))
          }
      } ?: run {
          dataFlow.value = Result.failure(Exception("The profile is not in the correct format or could not be fetched"))
      }
  }
}
