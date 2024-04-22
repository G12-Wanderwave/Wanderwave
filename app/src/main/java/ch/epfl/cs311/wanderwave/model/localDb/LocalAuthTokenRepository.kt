package ch.epfl.cs311.wanderwave.model.localDb

import ch.epfl.cs311.wanderwave.model.repository.AuthTokenRepository

class LocalAuthTokenRepository(database: AppDatabase) : AuthTokenRepository {

  private val authTokenDao = database.authTokenDao()

  override fun getAuthToken(tokenType: AuthTokenRepository.AuthTokenType): String? {
    return authTokenDao.getAuthToken(tokenType.id)?.token
  }

  override fun setAuthToken(
      tokenType: AuthTokenRepository.AuthTokenType,
      token: String,
      expirationTime: Long
  ) {
    authTokenDao.setAuthToken(AuthTokenEntity(token, expirationTime, tokenType.id))
  }

  override fun deleteAuthToken(tokenType: AuthTokenRepository.AuthTokenType) {
    authTokenDao.deleteAuthToken(tokenType.id)
  }
}
