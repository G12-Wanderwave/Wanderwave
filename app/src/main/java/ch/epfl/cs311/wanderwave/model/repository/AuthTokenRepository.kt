package ch.epfl.cs311.wanderwave.model.repository

interface AuthTokenRepository {

  fun getAuthToken(tokenType: AuthTokenType): String?

  fun setAuthToken(tokenType: AuthTokenType, token: String, expirationTime: Long)

  fun deleteAuthToken(tokenType: AuthTokenType)

  enum class AuthTokenType(val id: Int) {
    SPOTIFY_ACCESS_TOKEN(0),
    SPOTIFY_REFRESH_TOKEN(1),
    FIREBASE_TOKEN(2)
  }
}
