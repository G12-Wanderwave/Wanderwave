package ch.epfl.cs311.wanderwave.model.remote

import android.util.Log
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.data.ProfileTrackAssociation
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.TrackRepository
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow

class TrackConnection(private val database: FirebaseFirestore) :
    FirebaseConnection<Track, Track>(database), TrackRepository {

  // THe goal is to have the Id of the firebase document to match the id of the spotify track

  override val collectionName: String = "tracks"

  override val getItemId = { track: Track -> track.id }

  override val db = database

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
      val trackId =
          if (track.id.contains("spotify:track:")) track.id else "spotify:track:" + track.id
      val correctTrack = track.copy(id = trackId)
      db.collection(collectionName)
          .whereEqualTo("id", correctTrack.id)
          .get()
          .addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.isEmpty) {
              addItemWithId(correctTrack)
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
  fun fetchProfileAndTrack(
      profileAndTrackRef: Map<String, DocumentReference>?
  ): Flow<Result<ProfileTrackAssociation>> = callbackFlow {
    Log.d("Firestore", "Fetching profile and track ${profileAndTrackRef.toString()}")
    if (profileAndTrackRef == null) {
      trySend(Result.failure(Exception("Profile and Track reference is null")))
    } else {
      try {
        profileAndTrackRef["track"]?.addSnapshotListener { trackDocument, error ->
          val track = trackDocument?.let { Track.from(it) }
          profileAndTrackRef["creator"]?.addSnapshotListener { profileDocument, error ->
            val profile = profileDocument?.let { Profile.from(it) }
            if (track == null) {
              trySend(
                  Result.failure(Exception("Error fetching the track, firebase format is wrong")))
            } else {
              trySend(Result.success(ProfileTrackAssociation(profile = profile, track = track)))
            }
          }
        }
      } catch (e: Exception) {
        // Handle exceptions
        Log.e("Firestore", "Error fetching profile and track:${e.message}")
        trySend(Result.failure(e))
      }
    }
    awaitClose { close() }
  }

  // Fetch a track from a DocumentReference asynchronously
  fun fetchTrack(TrackRef: DocumentReference?): Flow<Result<Track>> = callbackFlow {
    if (TrackRef == null) {
      trySend(Result.failure(Exception("Track reference is null")))
    } else {
      try {
        TrackRef.addSnapshotListener { trackDocument, error ->
          val track = trackDocument?.let { Track.from(it) }
          if (track != null) {
            trySend(Result.success(track))
          } else {
            trySend(Result.failure(Exception("Track could not be fetched")))
          }
        }
      } catch (e: Exception) {
        // Handle exceptions
        Log.e("Firestore", "Error fetching track:${e.message}")
        trySend(Result.failure(e))
      }
    }
    awaitClose {}
  }
}
