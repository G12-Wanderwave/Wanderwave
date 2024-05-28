package ch.epfl.cs311.wanderwave.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import ch.epfl.cs311.wanderwave.R
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationUserData
import ch.epfl.cs311.wanderwave.model.repository.AuthTokenRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import io.mockk.called
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import java.io.ByteArrayOutputStream
import java.net.URL
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class AuthenticationControllerTest {

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK private lateinit var mockFirebaseAuth: FirebaseAuth

  @RelaxedMockK private lateinit var mockHttpClient: OkHttpClient

  @RelaxedMockK private lateinit var mockTokenRepository: AuthTokenRepository

  private lateinit var authenticationController: AuthenticationController

  private lateinit var testDispatcher: TestDispatcher

  private val dummyUser =
      AuthenticationUserData(
          "testid", "testemail", "testDisplayName", "https://example.com/testphoto.jpg")

  @OptIn(ExperimentalCoroutinesApi::class)
  @Before
  fun setup() {
    testDispatcher = UnconfinedTestDispatcher(TestCoroutineScheduler())
    authenticationController =
        AuthenticationController(
            mockFirebaseAuth, mockHttpClient, mockTokenRepository, testDispatcher)
    coEvery { authenticationController.refreshSpotifyToken() } returns true
  }

  fun setupDummyUserSignedIn() {
    every { mockFirebaseAuth.currentUser } returns
        mockk {
          every { uid } returns dummyUser.id
          every { email } returns dummyUser.email
          every { displayName } returns dummyUser.displayName
          every { photoUrl } returns Uri.parse(dummyUser.photoUrl)
        }
    val call = mockk<Call>()
    every { mockHttpClient.newCall(any()) } returns call
    every { call.execute() } returns
        mockk {
          every { body } returns
              mockk {
                every { string() } returns
                    """
            {
              "firebase_token": "testtoken-firebase",
              "access_token": "testtoken-spotify-access",
              "refresh_token": "testtoken-spotify-refresh"
            }
            """
                        .trimIndent()
              }
        }
    coEvery { mockTokenRepository.setAuthToken(any(), any(), any()) } returns Unit
    coEvery {
      mockTokenRepository.getAuthToken(AuthTokenRepository.AuthTokenType.FIREBASE_TOKEN)
    } returns "testtoken-firebase"
    coEvery {
      mockTokenRepository.getAuthToken(AuthTokenRepository.AuthTokenType.SPOTIFY_ACCESS_TOKEN)
    } returns "testtoken-spotify-access"
    coEvery {
      mockTokenRepository.getAuthToken(AuthTokenRepository.AuthTokenType.SPOTIFY_REFRESH_TOKEN)
    } returns "testtoken-spotify-refresh"
  }

  @Test
  fun canGetData() = runBlocking {
    setupDummyUserSignedIn()

    val userData = authenticationController.getUserData()
    assert(userData == dummyUser)

    assert(authenticationController.isSignedIn())
  }

  @Test
  fun canSignOut() = runBlocking {
    assert(authenticationController.isSignedIn())
    every { mockFirebaseAuth.signOut() } returns Unit
    authenticationController.deauthenticate()
    verify { mockFirebaseAuth.signOut() }
    every { mockFirebaseAuth.currentUser } returns null
    assert(!authenticationController.isSignedIn())
  }

  @Test
  fun alreadySignedIn() = runBlocking {
    setupDummyUserSignedIn()
    val result = authenticationController.authenticate("testcode").first()
    assert(result)

    verify { mockFirebaseAuth.signInWithCustomToken(any()) wasNot called }
  }

  @Test
  fun signInWithToken() = runBlocking {
    setupDummyUserSignedIn()
    val mockFirebaseUser =
        mockk<com.google.firebase.auth.FirebaseUser> {
          every { uid } returns "testid"
          every { email } returns null
          every { displayName } returns null
          every { photoUrl } returns null
        }

    val task =
        mockk<Task<AuthResult>> {
          every { result } returns mockk { every { user } returns mockFirebaseUser }
          every { isComplete } returns true
          every { isSuccessful } returns true
          every { isCanceled } returns false
          every { exception } returns null
        }

    every { mockFirebaseAuth.currentUser } returns null
    every { mockFirebaseAuth.signInWithCustomToken("testtoken-firebase") } returns task

    val result = authenticationController.authenticate("testcode").first()
    verify { mockFirebaseAuth.signInWithCustomToken("testtoken-firebase") }
    assert(result)
  }

  @Test
  fun useRefreshToken() = runBlocking {
    setupDummyUserSignedIn()
    val mockFirebaseUser =
        mockk<com.google.firebase.auth.FirebaseUser> {
          every { uid } returns "testid"
          every { email } returns null
          every { displayName } returns null
          every { photoUrl } returns null
        }

    val task =
        mockk<Task<AuthResult>> {
          every { result } returns mockk { every { user } returns mockFirebaseUser }
          every { isComplete } returns true
          every { isSuccessful } returns true
          every { isCanceled } returns false
          every { exception } returns null
        }

    every { mockFirebaseAuth.currentUser } returns null
    every { mockFirebaseAuth.signInWithCustomToken("testtoken-firebase") } returns task

    val result = authenticationController.refreshTokenIfNecessary()
    verify { mockFirebaseAuth.signInWithCustomToken("testtoken-firebase") }
    assert(result)

    every { mockFirebaseAuth.currentUser } returns mockFirebaseUser
    val result2 = authenticationController.refreshTokenIfNecessary()
    assert(result2)
    verify { mockFirebaseAuth.signInWithCustomToken(any()) wasNot called }
  }

  @Test
  fun failureCases() = runBlocking {
    setupDummyUserSignedIn()
    val call = mockk<Call>()
    every { mockHttpClient.newCall(any()) } returns call
    every { call.execute() } returns mockk { every { body } returns null }
    every { mockFirebaseAuth.currentUser } returns null

    assert(!authenticationController.refreshTokenIfNecessary())
    verify { call.execute() }

    coEvery { mockTokenRepository.getAuthToken(any()) } returns null

    assert(!authenticationController.refreshTokenIfNecessary())

    assert(!authenticationController.authenticate("%invalidcode%").first())
  }

  @Test
  fun makeApiRequest_ShouldReturnExpectedResult_OnSuccessfulApiCall() = runBlocking {
    setupDummyUserSignedIn()
    val mockFirebaseUser =
        mockk<com.google.firebase.auth.FirebaseUser> {
          every { uid } returns "testid"
          every { email } returns null
          every { displayName } returns null
          every { photoUrl } returns null
        }

    val task =
        mockk<Task<AuthResult>> {
          every { result } returns mockk { every { user } returns mockFirebaseUser }
          every { isComplete } returns true
          every { isSuccessful } returns true
          every { isCanceled } returns false
          every { exception } returns null
        }

    every { mockFirebaseAuth.currentUser } returns null
    every { mockFirebaseAuth.signInWithCustomToken("testtoken-firebase") } returns task

    coEvery {
      mockTokenRepository.getAuthToken(AuthTokenRepository.AuthTokenType.SPOTIFY_REFRESH_TOKEN)
    } returns "REFRESH_TOKEN"

    val testUrl = URL("https://api.spotify.com/v1/albums/4aawyAB9vmqN3uQ7FjRGTy")
    val call = mockk<Call>()
    every { mockHttpClient.newCall(any()) } returns call

    coEvery {
      withContext(testDispatcher) { mockHttpClient.newCall(any()).execute().body?.string() }
    } returns
        """
            {
              "firebase_token": "testtoken-firebase",
              "access_token": "testtoken-spotify-access",
              "refresh_token": "testtoken-spotify-refresh"
            }
            """
            .trimIndent()

    val result = authenticationController.makeApiRequest(testUrl)
  }

  @Test
  fun makeApiRequest_ShouldReturnFailure_OnTokenRefreshFailure() = runBlocking {
    val testUrl = URL("https://test.api/endpoint")
    coEvery { authenticationController.refreshSpotifyToken() } returns false

    val result = authenticationController.makeApiRequest(testUrl)

    assertEquals("FAILURE", result)
  }

  @Test
  fun testUploadPlaylistImage2() = runBlocking {
    // Arrange
    val mockContext: Context = mockk(relaxed = true)
    val playlistId = "testPlaylistId"
    val mockCall: Call = mockk(relaxed = true)
    val mockResponse: Response = mockk(relaxed = true)

    coEvery { mockTokenRepository.getAuthToken(any()) } returns "testToken"
    every { mockHttpClient.newCall(any()) } returns mockCall
    every { mockCall.execute() } returns mockResponse
    every { mockResponse.isSuccessful } returns true

    // Act
    authenticationController.uploadPlaylistImage(mockContext, playlistId)
  }

  @Test
  fun testUploadPlaylistImage() = runBlocking {
    // Arrange
    val mockContext: Context = mockk(relaxed = true)
    val playlistId = "testPlaylistId"
    val mockBitmap: Bitmap = mockk(relaxed = true)
    val mockCall: Call = mockk()
    val mockResponse: Response = mockk()
    val mockResponseBody: ResponseBody = mockk()

    coEvery { mockTokenRepository.getAuthToken(any()) } returns "testToken"
    every { mockHttpClient.newCall(any()) } returns mockCall
    every { mockCall.execute() } returns mockResponse
    every { mockResponse.isSuccessful } returns true
    every { mockResponse.body } returns mockResponseBody
    every { mockResponseBody.string() } returns ""

    mockkStatic(BitmapFactory::class)
    every { BitmapFactory.decodeResource(mockContext.resources, R.drawable.ic_logo) } returns
        mockBitmap

    mockkStatic(Base64::class)
    val outputStream = ByteArrayOutputStream()
    every { mockBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream) } returns true
    val imageBytes = outputStream.toByteArray()
    every { Base64.encodeToString(imageBytes, Base64.NO_WRAP) } returns "base64ImageString"

    val requestSlot = slot<Request>()

    // Act
    authenticationController.uploadPlaylistImage(mockContext, playlistId)

    // Assert
    verify { mockHttpClient.newCall(capture(requestSlot)) }
    val request = requestSlot.captured

    // Cleanup
    unmockkStatic(BitmapFactory::class)
    unmockkStatic(Base64::class)
  }

  @Test
  fun testUploadPlaylistImage_TokenRefresh() = runBlocking {
    // Arrange
    val mockContext: Context = mockk(relaxed = true)
    val playlistId = "testPlaylistId"
    val mockBitmap: Bitmap = mockk(relaxed = true)
    val mockCall: Call = mockk()
    val mockResponse: Response = mockk()
    val mockRetryResponse: Response = mockk()
    val mockResponseBody: ResponseBody = mockk()
    val mockRetryResponseBody: ResponseBody = mockk()

    coEvery { mockTokenRepository.getAuthToken(any()) } returns "expiredToken" andThen "newToken"
    every { mockHttpClient.newCall(any()) } returns mockCall
    every { mockCall.execute() } returns mockResponse andThen mockRetryResponse
    every { mockResponse.isSuccessful } returns false
    every { mockResponse.code } returns 401
    every { mockRetryResponse.isSuccessful } returns true
    every { mockResponse.body } returns mockResponseBody
    every { mockResponseBody.string() } returns ""
    every { mockRetryResponse.body } returns mockRetryResponseBody
    every { mockRetryResponseBody.string() } returns ""

    mockkStatic(BitmapFactory::class)
    every { BitmapFactory.decodeResource(mockContext.resources, R.drawable.ic_logo) } returns
        mockBitmap

    mockkStatic(Base64::class)
    val outputStream = ByteArrayOutputStream()
    every { mockBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream) } returns true
    val imageBytes = outputStream.toByteArray()
    every { Base64.encodeToString(imageBytes, Base64.NO_WRAP) } returns "base64ImageString"

    coEvery { authenticationController.refreshSpotifyToken() } returns true

    val requestSlot = slot<Request>()

    // Act
    authenticationController.uploadPlaylistImage(mockContext, playlistId)

    // Assert
    verify { mockHttpClient.newCall(capture(requestSlot)) }
    val request = requestSlot.captured

    // Cleanup
    unmockkStatic(BitmapFactory::class)
    unmockkStatic(Base64::class)
  }

  @Test
  fun testUploadPlaylistImage_ErrorHandling() = runBlocking {
    // Arrange
    val mockContext: Context = mockk(relaxed = true)
    val playlistId = "testPlaylistId"
    val mockBitmap: Bitmap = mockk(relaxed = true)
    val mockCall: Call = mockk()
    val mockResponse: Response = mockk()
    val mockResponseBody: ResponseBody = mockk()

    coEvery { mockTokenRepository.getAuthToken(any()) } returns "testToken"
    every { mockHttpClient.newCall(any()) } returns mockCall
    every { mockCall.execute() } returns mockResponse
    every { mockResponse.isSuccessful } returns false
    every { mockResponse.code } returns 500
    every { mockResponse.body } returns mockResponseBody
    every { mockResponseBody.string() } returns ""

    mockkStatic(BitmapFactory::class)
    every { BitmapFactory.decodeResource(mockContext.resources, R.drawable.ic_logo) } returns
        mockBitmap

    mockkStatic(Base64::class)
    val outputStream = ByteArrayOutputStream()
    every { mockBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream) } returns true
    val imageBytes = outputStream.toByteArray()
    every { Base64.encodeToString(imageBytes, Base64.NO_WRAP) } returns "base64ImageString"

    val requestSlot = slot<Request>()

    // Act
    authenticationController.uploadPlaylistImage(mockContext, playlistId)

    // Assert
    verify { mockHttpClient.newCall(capture(requestSlot)) }
    val request = requestSlot.captured

    // Cleanup
    unmockkStatic(BitmapFactory::class)
    unmockkStatic(Base64::class)
  }
}
