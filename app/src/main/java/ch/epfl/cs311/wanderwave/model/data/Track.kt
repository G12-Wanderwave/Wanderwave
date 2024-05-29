package ch.epfl.cs311.wanderwave.model.data

import com.google.firebase.firestore.DocumentSnapshot

data class Track(
    /** Firebase id = Spotify id */
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
        val trackId =
            if (document.id.contains("spotify:track:")) document.id
            else "spotify:track:" + document.id
        Track(
            id = trackId,
            title = document.getString("title") ?: "",
            artist = document.getString("artist") ?: "")
      } else {
        null
      }
    }
  }
}
