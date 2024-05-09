package ch.epfl.cs311.wanderwave.model.data

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot

data class Track(

    /** Spotify id */
    val id: String,

    /** Title of the track */
    val title: String,

    /** Artist of the track */
    val artist: String,
) {

  // No-argument constructor
  constructor() : this("", "", "")

  fun toMap(): Map<String, Any> {
    return hashMapOf("id" to id, "title" to title, "artist" to artist)
  }

  companion object {
    fun from(document: DocumentSnapshot): Track? {
      return if (document.exists()) {
        Track(
            id = document.id,
            title = document.getString("title") ?: "",
            artist = document.getString("artist") ?: "")
      } else {
        null
      }
    }
  }
}
