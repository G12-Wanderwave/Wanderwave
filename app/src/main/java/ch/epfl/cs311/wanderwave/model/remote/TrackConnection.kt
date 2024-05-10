package ch.epfl.cs311.wanderwave.model.remote

import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.TrackRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class TrackConnection(private val database: FirebaseFirestore? = null) :
    FirebaseConnection<Track, Track>(), TrackRepository {

  // THe goal is to have the Id of the firebase document to match the id of the spotify track

  override val collectionName: String = "tracks"

  override val getItemId = { track: Track -> track.id }

  override val db = database ?: super.db

  // Document to Track
  override fun documentToItem(document: DocumentSnapshot): Track? {
    return Track.from(document)
  }

  override fun itemToMap(track: Track): Map<String, Any> {
    return track.toMap()
  }

  override fun addItemsIfNotExist(tracks: List<Track>) {
    // The goal of this function is to add only if the spotify id of the track is not already in the
    // database, for now I just check the normal ID
    tracks.forEach { track ->
      db.collection(collectionName).whereEqualTo("id", track.id).get().addOnSuccessListener {
          documentSnapshot ->
        if (documentSnapshot.isEmpty) {
          addItemWithId(track)
        }
      }
    }
  }

  override fun getAll(): Flow<List<Track>> {
    val stateFlow = MutableStateFlow<List<Track>>(listOf())
    db.collection(collectionName).addSnapshotListener { value, error ->
      if (error != null) {
        return@addSnapshotListener
      }
      val tracks = value?.documents?.mapNotNull { documentToItem(it) } ?: listOf()
      stateFlow.value = tracks
    }
    return stateFlow
  }

  override fun getTrackById(trackId: String): Flow<Track?> {
    val stateFlow = MutableStateFlow<Track?>(null)
    db.collection(collectionName).document(trackId).addSnapshotListener { snapshot, error ->
      if (error != null) {
        stateFlow.value = null
      } else if (snapshot != null && snapshot.exists()) {
        stateFlow.value = documentToItem(snapshot)
      } else {
        stateFlow.value = null
      }
    }
    return stateFlow
  }
}
