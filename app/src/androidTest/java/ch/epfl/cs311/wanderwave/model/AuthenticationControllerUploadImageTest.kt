import android.content.Context
import androidx.test.core.app.ApplicationProvider
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
import ch.epfl.cs311.wanderwave.model.repository.AuthTokenRepository
import com.google.firebase.auth.FirebaseAuth
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthenticationControllerUploadImageTest {

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK private lateinit var mockFirebaseAuth: FirebaseAuth

  @RelaxedMockK private lateinit var mockHttpClient: OkHttpClient

  @RelaxedMockK private lateinit var mockTokenRepository: AuthTokenRepository

  private lateinit var authenticationController: AuthenticationController
  private val testDispatcher = UnconfinedTestDispatcher()

  @Before
  fun setup() {
    MockKAnnotations.init(this, relaxed = true)
    authenticationController =
        AuthenticationController(
            mockFirebaseAuth, mockHttpClient, mockTokenRepository, testDispatcher)
  }

  private fun mockSuccessfulResponse() {
    val call = mockk<Call>()
    every { mockHttpClient.newCall(any()) } returns call
    val responseBody = mockk<ResponseBody> { every { string() } returns "{}" }
    val response =
        mockk<Response> {
          every { isSuccessful } returns true
          every { body } returns responseBody
        }
    every { call.execute() } returns response
  }

  private fun mockFailureResponse(code: Int) {
    val call = mockk<Call>()
    every { mockHttpClient.newCall(any()) } returns call
    val responseBody = mockk<ResponseBody> { every { string() } returns "{}" }
    val response =
        mockk<Response> {
          every { isSuccessful } returns false
          every { this@mockk.code } returns code
          every { body } returns responseBody
        }
    every { call.execute() } returns response
  }

  private fun mockIOException() {
    val call = mockk<Call>()
    every { mockHttpClient.newCall(any()) } returns call
    every { call.execute() } throws IOException("Network Error")
  }

  @Test
  fun uploadPlaylistImage_SuccessfulUpload() = runBlockingTest {
    mockSuccessfulResponse()
    coEvery { mockTokenRepository.getAuthToken(any()) } returns "VALID_TOKEN"
    coEvery { authenticationController.refreshSpotifyToken() } returns true

    val context = ApplicationProvider.getApplicationContext<Context>()
    authenticationController.uploadPlaylistImage(context, "test_playlist")

    verify { mockHttpClient.newCall(any<Request>()) }
  }

  @Test
  fun uploadPlaylistImage_TokenRefreshAndRetry() = runBlockingTest {
    mockFailureResponse(401)
    coEvery { mockTokenRepository.getAuthToken(any()) } returns "EXPIRED_TOKEN"
    coEvery {
      mockTokenRepository.getAuthToken(AuthTokenRepository.AuthTokenType.SPOTIFY_ACCESS_TOKEN)
    } returns "NEW_VALID_TOKEN"
    coEvery { authenticationController.refreshSpotifyToken() } returns true
    mockSuccessfulResponse()

    val context = ApplicationProvider.getApplicationContext<Context>()
    authenticationController.uploadPlaylistImage(context, "test_playlist")

    verify { mockHttpClient.newCall(any<Request>()) }
  }

  @Test
  fun uploadPlaylistImage_FailedTokenRefresh() = runBlockingTest {
    mockFailureResponse(401)
    coEvery { mockTokenRepository.getAuthToken(any()) } returns "EXPIRED_TOKEN"
    coEvery { authenticationController.refreshSpotifyToken() } returns false

    val context = ApplicationProvider.getApplicationContext<Context>()
    authenticationController.uploadPlaylistImage(context, "test_playlist")

    // No need to verify calls as the function should return early
  }

  @Test
  fun uploadPlaylistImage_UnexpectedResponseCode() = runBlockingTest {
    mockFailureResponse(500)
    coEvery { mockTokenRepository.getAuthToken(any()) } returns "VALID_TOKEN"
    coEvery { authenticationController.refreshSpotifyToken() } returns true

    val context = ApplicationProvider.getApplicationContext<Context>()
    try {
      authenticationController.uploadPlaylistImage(context, "test_playlist")
    } catch (e: IOException) {
      assert(e.message?.contains("Unexpected code") == true)
    }

    verify { mockHttpClient.newCall(any<Request>()) }
  }

  @Test
  fun uploadPlaylistImage_NetworkError() = runBlockingTest {
    mockIOException()
    coEvery { mockTokenRepository.getAuthToken(any()) } returns "VALID_TOKEN"
    coEvery { authenticationController.refreshSpotifyToken() } returns true

    val context = ApplicationProvider.getApplicationContext<Context>()
    try {
      authenticationController.uploadPlaylistImage(context, "test_playlist")
    } catch (e: IOException) {
      assert(e.message?.contains("Network Error") == true)
    }

    verify { mockHttpClient.newCall(any<Request>()) }
  }
}
