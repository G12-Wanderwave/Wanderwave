package ch.epfl.cs311.wanderwave.model.data

import com.google.firebase.firestore.DocumentSnapshot

data class Beacon(

    /** GUID of the beacon */
    val id: String,

    /** Location of the beacon */
    val location: Location,

    /** List of tracks that are broadcast from the beacon */
    val profileAndTrack: List<ProfileTrackAssociation> = listOf<ProfileTrackAssociation>(),
) {

  companion object {
    fun sampleBeacon(): Beacon {
      return Beacon(
          "UAn8OUadgrUOKYagf8a2",
          Location(46.519653, 6.632273, "Lausanne"),
          listOf(
              ProfileTrackAssociation(
                  Profile(
                      "Sample First Name",
                      "Sample last name",
                      "Sample desc",
                      0,
                      false,
                      null,
                      "Sample Profile ID",
                      "Sample Track ID"),
                  Track("Sample Track ID", "Sample Track Title", "Sample Artist Name"))))
    }

    fun from(document: DocumentSnapshot): Beacon? {
      return if (document.exists()) {
        val id = document.id
        val locationMap = document.get("location") as? Map<String, Any>
        val latitude = locationMap?.get("latitude") as? Double ?: 0.0
        val longitude = locationMap?.get("longitude") as? Double ?: 0.0
        val name = locationMap?.get("name") as? String ?: ""
        val location = Location(latitude, longitude, name)

        val profileAndTrack = listOf<ProfileTrackAssociation>()

        Beacon(id = id, location = location, profileAndTrack = profileAndTrack)
      } else {
        null
      }
    }
  }
}
