package ch.epfl.cs311.wanderwave.model.auth

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import ch.epfl.cs311.wanderwave.R
import ch.epfl.cs311.wanderwave.model.repository.AuthTokenRepository
import com.google.firebase.auth.FirebaseAuth
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
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

  suspend fun makeApiRequest(url: URL, requestType: String = "GET", data: String = ""): String {
    Log.d("AuthenticationController", "Making API request: $url")
    Log.d("AuthenticationController", "Request type: $requestType")
    Log.d("AuthenticationController", "Data: $data")

    if (!refreshSpotifyToken()) {
      Log.e("AuthenticationController", "Failed to refresh Spotify token")
      return "FAILURE"
    }

    val accessToken =
        tokenRepository.getAuthToken(AuthTokenRepository.AuthTokenType.SPOTIFY_ACCESS_TOKEN)
    Log.i("AuthenticationController", "Access token available: ${accessToken != null}")

    return withContext(ioDispatcher) {
      try {
        (url.openConnection() as HttpURLConnection).run {
          requestMethod = requestType
          setRequestProperty("Authorization", "Bearer $accessToken")
          setRequestProperty("Content-Type", "application/json")

          if (requestType == "POST" || requestType == "DELETE") {
            doOutput = true
            outputStream.bufferedWriter().use { it.write(data) }
          }

          val responseCode = responseCode
          val responseMessage = responseMessage
          Log.d("AuthenticationController", "Response code: $responseCode")
          Log.d("AuthenticationController", "Response message: $responseMessage")

          if (responseCode == 403) {
            Log.e(
                "AuthenticationController",
                "Insufficient client scope. Please re-authenticate with the required permissions.")
            return@withContext "FAILURE: Insufficient client scope"
          }

          if (responseCode in 200..299) {
            inputStream.bufferedReader().use {
              return@withContext it.readText()
            }
          } else {
            errorStream?.bufferedReader()?.use {
              Log.e("AuthenticationController", "Error response: ${it.readText()}")
            }
            return@withContext "FAILURE"
          }
        }
      } catch (e: FileNotFoundException) {
        Log.e("AuthenticationController", "Failed to make API request: ${e.message}")
        return@withContext "FAILURE"
      } catch (e: IOException) {
        Log.e("AuthenticationController", "IO Exception: ${e.message}")
        return@withContext "FAILURE"
      }
    }
  }

  suspend fun uploadPlaylistImage(context: Context, playlistId: String) {
    withContext(ioDispatcher) {
      if (!refreshSpotifyToken()) {
        Log.e("AuthenticationController", "Failed to refresh Spotify token")
        return@withContext
      }

      val accessToken =
          tokenRepository.getAuthToken(AuthTokenRepository.AuthTokenType.SPOTIFY_ACCESS_TOKEN)
      Log.i("AuthenticationController", "Access token available: ${accessToken != null}")

      val client = OkHttpClient()

      // Decode the drawable resource to a Bitmap
      val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_logo)

      // Convert the Bitmap to a Base64 string
      val outputStream = ByteArrayOutputStream()
      bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
      val imageBytes = outputStream.toByteArray()
      val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

      // Create the request body
      val mediaType = "image/jpeg".toMediaTypeOrNull()
      val requestBody: RequestBody = base64Image.toRequestBody(mediaType)

      // Create the request
      val request =
          Request.Builder()
              .url("https://api.spotify.com/v1/playlists/$playlistId/images")
              .put(requestBody)
              .addHeader("Authorization", "Bearer $accessToken")
              .addHeader("Content-Type", "image/jpeg")
              .build()

      // Execute the request
      try {
        client.newCall(request).execute().use { response ->
          if (!response.isSuccessful) {
            if (response.code == 401) {
              // Token might be expired, try to refresh and retry once
              if (refreshSpotifyToken()) {
                val newAccessToken =
                    tokenRepository.getAuthToken(
                        AuthTokenRepository.AuthTokenType.SPOTIFY_ACCESS_TOKEN)
                val retryRequest =
                    request.newBuilder().header("Authorization", "Bearer $newAccessToken").build()
                client.newCall(retryRequest).execute().use { retryResponse ->
                  if (!retryResponse.isSuccessful) {
                    throw IOException("Unexpected code $retryResponse")
                  } else {
                    println("Image uploaded successfully after token refresh!")
                  }
                }
              } else {
                throw IOException("Failed to refresh token")
              }
            } else {
              throw IOException("Unexpected code $response")
            }
          } else {
            println("Image uploaded successfully!")
          }
        }
      } catch (e: IOException) {
        Log.e("AuthenticationController", "Error uploading image: ${e.message}")
      }
    }
  }
}
