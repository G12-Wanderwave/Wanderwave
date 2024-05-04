package ch.epfl.cs311.wanderwave.model.localDb

import ch.epfl.cs311.wanderwave.model.repository.AuthTokenRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class LocalAuthTokenRepository(
    private val database: AppDatabase,
    private val ioDispatcher: CoroutineDispatcher
) : AuthTokenRepository {

  private val authTokenDao = database.authTokenDao()

  override suspend fun getAuthToken(tokenType: AuthTokenRepository.AuthTokenType): String? {
    return withContext(ioDispatcher) {
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
    withContext(ioDispatcher) {
      authTokenDao.setAuthToken(AuthTokenEntity(token, expirationTime, tokenType.id))
    }
  }

  override suspend fun deleteAuthToken(tokenType: AuthTokenRepository.AuthTokenType) {
    withContext(ioDispatcher) { authTokenDao.deleteAuthToken(tokenType.id) }
  }
}
