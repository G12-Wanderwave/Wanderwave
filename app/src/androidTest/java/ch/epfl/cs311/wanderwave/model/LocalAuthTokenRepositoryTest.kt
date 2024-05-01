package ch.epfl.cs311.wanderwave.model

import ch.epfl.cs311.wanderwave.model.localDb.AppDatabase
import ch.epfl.cs311.wanderwave.model.localDb.AuthTokenDao
import ch.epfl.cs311.wanderwave.model.localDb.AuthTokenEntity
import ch.epfl.cs311.wanderwave.model.localDb.LocalAuthTokenRepository
import ch.epfl.cs311.wanderwave.model.repository.AuthTokenRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class LocalAuthTokenRepositoryTest {

  @get:Rule val mockkRule = MockKRule(this)

  @MockK lateinit var mockDatabase: AppDatabase

  @MockK lateinit var mockAuthTokenDao: AuthTokenDao

  private lateinit var localAuthTokenRepository: LocalAuthTokenRepository

  @Before
  fun setup() {
    every { mockDatabase.authTokenDao() } returns mockAuthTokenDao
    every { mockAuthTokenDao.setAuthToken(any()) } returns Unit
    every { mockAuthTokenDao.deleteAuthToken(any()) } returns Unit
    localAuthTokenRepository = LocalAuthTokenRepository(mockDatabase)

    val now = System.currentTimeMillis() / 1000 + 3600

    every {
      mockAuthTokenDao.getAuthToken(AuthTokenRepository.AuthTokenType.SPOTIFY_REFRESH_TOKEN.id)
    } returns
        AuthTokenEntity(
            "spotifyRefreshToken",
            now + 789,
            AuthTokenRepository.AuthTokenType.SPOTIFY_REFRESH_TOKEN.id)
    every {
      mockAuthTokenDao.getAuthToken(AuthTokenRepository.AuthTokenType.SPOTIFY_ACCESS_TOKEN.id)
    } returns
        AuthTokenEntity(
            "spotifyAccessToken",
            now + 456,
            AuthTokenRepository.AuthTokenType.SPOTIFY_ACCESS_TOKEN.id)
    every {
      mockAuthTokenDao.getAuthToken(AuthTokenRepository.AuthTokenType.FIREBASE_TOKEN.id)
    } returns
        AuthTokenEntity(
            "firebaseToken", now + 123, AuthTokenRepository.AuthTokenType.FIREBASE_TOKEN.id)
  }

  @Test
  fun canSetTokens() = runBlocking {
    localAuthTokenRepository.setAuthToken(
        AuthTokenRepository.AuthTokenType.FIREBASE_TOKEN, "firebaseToken", 123L)

    verify {
      mockAuthTokenDao.setAuthToken(
          AuthTokenEntity(
              "firebaseToken", 123L, AuthTokenRepository.AuthTokenType.FIREBASE_TOKEN.id))
    }

    localAuthTokenRepository.setAuthToken(
        AuthTokenRepository.AuthTokenType.SPOTIFY_ACCESS_TOKEN, "spotifyAccessToken", 456L)

    verify {
      mockAuthTokenDao.setAuthToken(
          AuthTokenEntity(
              "spotifyAccessToken",
              456L,
              AuthTokenRepository.AuthTokenType.SPOTIFY_ACCESS_TOKEN.id))
    }

    localAuthTokenRepository.setAuthToken(
        AuthTokenRepository.AuthTokenType.SPOTIFY_REFRESH_TOKEN, "spotifyRefreshToken", 789L)

    verify {
      mockAuthTokenDao.setAuthToken(
          AuthTokenEntity(
              "spotifyRefreshToken",
              789L,
              AuthTokenRepository.AuthTokenType.SPOTIFY_REFRESH_TOKEN.id))
    }
  }

  @Test
  fun canGetTokens() = runBlocking {
    val firebaseToken =
        localAuthTokenRepository.getAuthToken(AuthTokenRepository.AuthTokenType.FIREBASE_TOKEN)
    assert(firebaseToken == "firebaseToken")

    val spotifyAccessToken =
        localAuthTokenRepository.getAuthToken(
            AuthTokenRepository.AuthTokenType.SPOTIFY_ACCESS_TOKEN)
    assert(spotifyAccessToken == "spotifyAccessToken")

    val spotifyRefreshToken =
        localAuthTokenRepository.getAuthToken(
            AuthTokenRepository.AuthTokenType.SPOTIFY_REFRESH_TOKEN)
    assert(spotifyRefreshToken == "spotifyRefreshToken")
  }

  @Test
  fun canDeleteTokens() = runBlocking {
    localAuthTokenRepository.deleteAuthToken(AuthTokenRepository.AuthTokenType.FIREBASE_TOKEN)
    verify { mockAuthTokenDao.deleteAuthToken(AuthTokenRepository.AuthTokenType.FIREBASE_TOKEN.id) }

    localAuthTokenRepository.deleteAuthToken(AuthTokenRepository.AuthTokenType.SPOTIFY_ACCESS_TOKEN)
    verify {
      mockAuthTokenDao.deleteAuthToken(AuthTokenRepository.AuthTokenType.SPOTIFY_ACCESS_TOKEN.id)
    }

    localAuthTokenRepository.deleteAuthToken(
        AuthTokenRepository.AuthTokenType.SPOTIFY_REFRESH_TOKEN)
    verify {
      mockAuthTokenDao.deleteAuthToken(AuthTokenRepository.AuthTokenType.SPOTIFY_REFRESH_TOKEN.id)
    }
  }

  @Test
  fun doNotGetExpiredToken() = runBlocking {
    every {
      mockAuthTokenDao.getAuthToken(AuthTokenRepository.AuthTokenType.SPOTIFY_REFRESH_TOKEN.id)
    } returns
        AuthTokenEntity(
            "spotifyRefreshToken", 0L, AuthTokenRepository.AuthTokenType.SPOTIFY_REFRESH_TOKEN.id)

    val spotifyRefreshToken =
        localAuthTokenRepository.getAuthToken(
            AuthTokenRepository.AuthTokenType.SPOTIFY_REFRESH_TOKEN)
    verify {
      mockAuthTokenDao.deleteAuthToken(AuthTokenRepository.AuthTokenType.SPOTIFY_REFRESH_TOKEN.id)
    }
    assert(spotifyRefreshToken == null)
  }
}
