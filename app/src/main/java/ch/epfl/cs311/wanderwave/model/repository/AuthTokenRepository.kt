package ch.epfl.cs311.wanderwave.model.repository

interface AuthTokenRepository {

  suspend fun getAuthToken(tokenType: AuthTokenType): String?

  suspend fun setAuthToken(tokenType: AuthTokenType, token: String, expirationTime: Long)

  suspend fun deleteAuthToken(tokenType: AuthTokenType)

  enum class AuthTokenType(val id: Int) {
    SPOTIFY_ACCESS_TOKEN(0),
    SPOTIFY_REFRESH_TOKEN(1),
    FIREBASE_TOKEN(2)
  }
}
