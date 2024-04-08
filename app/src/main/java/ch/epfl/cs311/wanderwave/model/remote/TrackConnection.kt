package ch.epfl.cs311.wanderwave.model.remote

import android.util.Log
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.data.toTrack
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow

class TrackConnection : FirebaseConnectionInt<Track, Track> {

  override val collectionName: String = "tracks"

  override val getItemId = { track: Track -> track.id }

  private val db = FirebaseFirestore.getInstance()

  // Document to Track
  override fun documentToItem(document: DocumentSnapshot): Track {
    return document.toTrack()
  }

  override fun itemToHash(track: Track): HashMap<String, Any> {
    return track.toHash()
  }

  fun addList(tracks: List<Track>) {
    tracks.forEach { track ->
      addItem(track)
    }
  }
}
