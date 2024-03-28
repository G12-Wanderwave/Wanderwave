package ch.epfl.cs311.wanderwave.model.data

import android.net.Uri

data class Profile(
    var firstName: String,
    var lastName: String,
    var description: String,
    var numberOfLikes: Int,
    var isPublic: Boolean,
    var profilePictureUri: Uri? = null,
)
