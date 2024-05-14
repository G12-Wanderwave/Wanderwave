package ch.epfl.cs311.wanderwave.model.auth

import android.util.Log
import ch.epfl.cs311.wanderwave.model.repository.AuthTokenRepository
import com.google.firebase.auth.FirebaseAuth
import java.io.FileNotFoundException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject

class AuthenticationController
@Inject
constructor(
    private val auth: FirebaseAuth,
    private val httpClient: OkHttpClient,
    private val tokenRepository: AuthTokenRepository,
    private val ioDispatcher: CoroutineDispatcher
) {

  private val AUTH_SERVICE_URL = "https://us-central1-wanderwave-95743.cloudfunctions.net"
  private val AUTH_SERVICE_TOKEN = "$AUTH_SERVICE_URL/token"
  private val AUTH_SERVICE_REFRESH = "$AUTH_SERVICE_URL/refresh"

  private val AUTH_CODE_PATTERN = "^[a-zA-Z0-9_-]+$".toRegex()

  fun isSignedIn(): Boolean {
    return auth.currentUser != null
  }

  fun getUserData(): AuthenticationUserData? {
    return auth.currentUser?.let { firebaseUser ->
      AuthenticationUserData(
          firebaseUser.uid,
          firebaseUser.email,
          firebaseUser.displayName,
          firebaseUser.photoUrl?.toString())
    }
  }

  fun authenticate(authenticationCode: String): Flow<Boolean> {
    return flow { emit(getTokensFromCode(authenticationCode)) }
  }

  suspend fun refreshTokenIfNecessary(): Boolean {
    if (auth.currentUser == null) {
      return refreshSpotifyToken()
    }
    return true
  }

  private suspend fun getTokensFromCode(authenticationCode: String): Boolean {
    if (auth.currentUser != null) {
      return true
    }
    if (authenticationCode.matches(AUTH_CODE_PATTERN).not()) {
      return false
    }
    val request =
        Request.Builder()
            .url(AUTH_SERVICE_TOKEN)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .post("code=$authenticationCode".toRequestBody())
            .build()

    val responseJson =
        withContext(ioDispatcher) { httpClient.newCall(request).execute().body?.string() }
            ?: return false
    return storeAndUseNewTokens(responseJson)
  }

  private suspend fun storeAndUseNewTokens(responseJson: String): Boolean {
    if (responseJson.isEmpty()) {
      Log.e("AuthenticationController", "Received empty JSON response")
      return false
    }

    return try {
      val response = JSONObject(responseJson)
      val firebaseToken = response.getString("firebase_token")
      val spotifyAccessToken = response.getString("access_token")
      val spotifyRefreshToken = response.getString("refresh_token")

      tokenRepository.setAuthToken(
          AuthTokenRepository.AuthTokenType.SPOTIFY_ACCESS_TOKEN,
          spotifyAccessToken,
          System.currentTimeMillis() / 1000L + 3600)
      tokenRepository.setAuthToken(
          AuthTokenRepository.AuthTokenType.SPOTIFY_REFRESH_TOKEN,
          spotifyRefreshToken,
          System.currentTimeMillis() / 1000L + 3600 * 100000)
      tokenRepository.setAuthToken(
          AuthTokenRepository.AuthTokenType.FIREBASE_TOKEN,
          firebaseToken,
          System.currentTimeMillis() / 1000L + 3600)

      signInWithCustomToken()
    } catch (e: JSONException) {
      Log.e("AuthenticationController", "Error parsing JSON: ${e.message}")
      false
    }
  }

  suspend fun refreshSpotifyToken(): Boolean {
    val refreshToken =
        tokenRepository.getAuthToken(AuthTokenRepository.AuthTokenType.SPOTIFY_REFRESH_TOKEN)
            ?: return false

    val request =
        Request.Builder()
            .url(AUTH_SERVICE_REFRESH)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .post("refresh_token=$refreshToken".toRequestBody())
            .build()

    val responseJson =
        withContext(ioDispatcher) { httpClient.newCall(request).execute().body?.string() }
            ?: return false
    return storeAndUseNewTokens(responseJson)
  }

  private suspend fun signInWithCustomToken(): Boolean {
    val firebaseToken =
        tokenRepository.getAuthToken(AuthTokenRepository.AuthTokenType.FIREBASE_TOKEN)
            ?: return false
    val result = auth.signInWithCustomToken(firebaseToken).await()
    return result.user != null
  }

  fun deauthenticate() {
    auth.signOut()
  }

  suspend fun getAccessToken(): String? {
    return tokenRepository.getAuthToken(AuthTokenRepository.AuthTokenType.SPOTIFY_ACCESS_TOKEN)
  }

  suspend fun makeApiRequest(url: URL): String {

    // refresh the access token if necessary
    if (!refreshSpotifyToken()) {
      Log.e("AuthenticationController", "Failed to refresh Spotify token")
      return "FAILURE"
    }
    // Get the access token from the AuthTokenRepository
    val accessToken =
        tokenRepository.getAuthToken(AuthTokenRepository.AuthTokenType.SPOTIFY_ACCESS_TOKEN)

    Log.i("AuthenticationController", "Access token available: ${accessToken != null}")
    return try {
      (withContext(ioDispatcher) { url.openConnection() } as HttpURLConnection).run {
        requestMethod = "GET"
        setRequestProperty("Authorization", "Bearer $accessToken")
        inputStream.bufferedReader().use {
          return it.readText()
        }
      }
    } catch (e: FileNotFoundException) {
      Log.e("AuthenticationController", "Failed to make API request: ${e.message}")
      "FAILURE"
    }
  }
}
