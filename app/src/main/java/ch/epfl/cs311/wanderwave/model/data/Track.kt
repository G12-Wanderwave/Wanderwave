package ch.epfl.cs311.wanderwave.model.data

import com.google.firebase.firestore.DocumentSnapshot

data class Track(

    /** Spotify id */
    val id: String,

    /** Title of the track */
    val title: String,

    /** Artist of the track */
    val artist: String,
    
) {
    fun toHash(): HashMap<String, Any> {
        return hashMapOf(
            "id" to id,
            "title" to title,
            "artist" to artist
        )
    }
}


fun DocumentSnapshot.toTrack(): Track? {
    val id = getString("id") ?: return null
    val title = getString("title") ?: return null
    val artist = getString("artist") ?: return null

    return Track(id, title, artist)
}