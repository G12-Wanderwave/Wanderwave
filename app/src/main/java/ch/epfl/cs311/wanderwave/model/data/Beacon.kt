package ch.epfl.cs311.wanderwave.model.data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

data class Beacon(

    /** GUID of the beacon */
    val id: String,

    /** Location of the beacon */
    val location: Location,

    /** List of tracks that are broadcast from the beacon */
    val profileAndTrack: List<ProfileTrackAssociation> = listOf<ProfileTrackAssociation>(),

    /** Number of likes the beacon has */
    val likes: Int = 0
) {

  fun toMap(db: FirebaseFirestore): HashMap<String, Any> =
      hashMapOf(
          "id" to id,
          "location" to location.toMap(),
          "likes" to likes,
          "tracks" to profileAndTrack.map { it.toMap(db) })

  companion object {
    fun from(document: DocumentSnapshot): Beacon? {
      return if (document.exists()) {
        val id = document.id
        val locationMap = document["location"] as? Map<String, Any> ?: null
        val latitude = locationMap?.get("latitude") as? Double ?: 0.0
        val longitude = locationMap?.get("longitude") as? Double ?: 0.0
        val name = locationMap?.get("name") as? String ?: ""
        val location = Location(latitude, longitude, name)

        val profileAndTrack = listOf<ProfileTrackAssociation>()

        val likes = document.getLong("likes")?.toInt() ?: 0

        Beacon(id = id, location = location, profileAndTrack = profileAndTrack, likes = likes)
      } else {
        null
      }
    }
  }
}
