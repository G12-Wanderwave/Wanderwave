package ch.epfl.cs311.wanderwave.model.auth

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import ru.gildor.coroutines.okhttp.await

class AuthenticationController
@Inject
constructor(private val auth: FirebaseAuth, private val httpClient: OkHttpClient) {

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
    if (auth.currentUser != null) {
      return flowOf(true)
    }
    if (authenticationCode.matches(AUTH_CODE_PATTERN).not()) {
      return flowOf(false)
    }
    return flow {
      val request =
          Request.Builder()
              .url(AUTH_SERVICE_TOKEN)
              .header("Content-Type", "application/x-www-form-urlencoded")
              .post("code=$authenticationCode".toRequestBody())
              .build()

      val responseJson = httpClient.newCall(request).await().body!!.string()
      val response = JSONObject(responseJson)
      val firebaseToken = response.getString("firebase_token")

      val result = auth.signInWithCustomToken(firebaseToken).await()
      if (result.user != null) {
        emit(true)
      } else {
        emit(false)
      }
    }
  }

  fun deauthenticate() {
    auth.signOut()
  }

  private data class TokenResponse(
      val accessToken: String,
      val refreshToken: String?,
      val firebaseToken: String
  )

  private data class State(val isSignedIn: Boolean = false)
}
