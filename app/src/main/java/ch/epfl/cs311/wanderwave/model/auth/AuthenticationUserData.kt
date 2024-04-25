package ch.epfl.cs311.wanderwave.model.auth

data class AuthenticationUserData(
    val id: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?
)
