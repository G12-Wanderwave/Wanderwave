package ch.epfl.cs311.wanderwave.model.data

data class User (

    /** Spotify id */
    val id: String,

    /** Name of the user */
    val name: String,

    /** Is the profile public */
    val isProfilePublic: Boolean,

    /** Profile picture of the user type unknown ??? */
    val profilePicture: String,

)