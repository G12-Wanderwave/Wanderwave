package ch.epfl.cs311.wanderwave.model.localDb

import ch.epfl.cs311.wanderwave.model.repository.AuthTokenRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalAuthTokenRepository(database: AppDatabase) : AuthTokenRepository {

  private val authTokenDao = database.authTokenDao()

  override suspend fun getAuthToken(tokenType: AuthTokenRepository.AuthTokenType): String? {
    return withContext(Dispatchers.IO) {
      authTokenDao.getAuthToken(tokenType.id)?.let { authTokenEntity ->
        if (authTokenEntity.expirationDate > System.currentTimeMillis() / 1000) {
          authTokenEntity.token
        } else {
          authTokenDao.deleteAuthToken(tokenType.id)
          null
        }
      }
    }
  }

  override suspend fun setAuthToken(
      tokenType: AuthTokenRepository.AuthTokenType,
      token: String,
      expirationTime: Long
  ) {
    withContext(Dispatchers.IO) {
      authTokenDao.setAuthToken(AuthTokenEntity(token, expirationTime, tokenType.id))
    }
  }

  override suspend fun deleteAuthToken(tokenType: AuthTokenRepository.AuthTokenType) {
    withContext(Dispatchers.IO) { authTokenDao.deleteAuthToken(tokenType.id) }
  }
}
