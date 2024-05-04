package ch.epfl.cs311.wanderwave.model.remote

import android.util.Log
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.data.ProfileTrackAssociation
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.TrackRepository
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

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

  // Fetch a track from a DocumentReference asynchronously
  suspend fun fetchProfileAndTrack(
    profileAndTrackRef: Map<String, DocumentReference>?
  ): ProfileTrackAssociation? {
    if (profileAndTrackRef == null) return null
    return withContext(Dispatchers.IO) {
      try {

        var profile: Profile? = null
        var track: Track? = null
        val trackDocument = profileAndTrackRef["track"]?.get()?.await()
        trackDocument?.let { track = Track.from(it) }
        val profileDocument = profileAndTrackRef["creator"]?.get()?.await()
        profileDocument?.let { profile = Profile.from(it) }
        if (profile == null || track == null) {
          return@withContext null
        }

        ProfileTrackAssociation(
          profile = profileDocument?.let { Profile.from(it) }!!,
          track = trackDocument?.let { Track.from(it) }!!)
      } catch (e: Exception) {
        // Handle exceptions
        Log.e("Firestore", "Error fetching track:${e.message}")
        null
      }
    }
  }

  // Fetch a track from a DocumentReference asynchronously
  suspend fun fetchTrack(
    TrackRef: DocumentReference?
  ): Track? {
    if (TrackRef == null) return null
    return withContext(Dispatchers.IO) {
      try {
        Log.d("Firestore", "Fetching track ${TrackRef}")
        val trackDocument = TrackRef.get()?.await()
        Log.d("Firestore", "Fetched track ${trackDocument}")
        trackDocument?.let { Track.from(it) }

      } catch (e: Exception) {
        // Handle exceptions
        Log.e("Firestore", "Error fetching track:${e.message}")
        null
      }
    }
  }
}
