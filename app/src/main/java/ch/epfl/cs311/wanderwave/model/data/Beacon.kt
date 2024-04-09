package ch.epfl.cs311.wanderwave.model.data

data class Beacon(

    /** GUID of the beacon */
    val id: String,

    /** Location of the beacon */
    val location: Location,

    /** List of tracks that are broadcast from the beacon */
    val tracks: List<Track> = listOf(),
)
