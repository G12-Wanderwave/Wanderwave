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
import io.mockk.Runs
import io.mockk.called
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
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
import okhttp3.Response
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

  @RelaxedMockK private lateinit var mockContext: Context

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
  fun uploadPlaylistImage_ShouldUploadImageSuccessfully() = runBlocking {
    val playlistId = "testPlaylistId"
    val dummyBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
    val dummyByteArrayOutputStream = ByteArrayOutputStream()
    dummyBitmap.compress(Bitmap.CompressFormat.JPEG, 100, dummyByteArrayOutputStream)
    val dummyByteArray = dummyByteArrayOutputStream.toByteArray()
    val dummyBase64Image = Base64.encodeToString(dummyByteArray, Base64.NO_WRAP)

    // Mock BitmapFactory.decodeResource
    mockkStatic(BitmapFactory::class)
    every { BitmapFactory.decodeResource(mockContext.resources, R.drawable.ic_logo) } returns
        dummyBitmap

    // Mock ByteArrayOutputStream
    mockkConstructor(ByteArrayOutputStream::class)
    every { anyConstructed<ByteArrayOutputStream>().toByteArray() } returns dummyByteArray
    coEvery { anyConstructed<ByteArrayOutputStream>().write(any(), any(), any()) } just Runs

    val accessToken = "testAccessToken"
    coEvery {
      mockTokenRepository.getAuthToken(AuthTokenRepository.AuthTokenType.SPOTIFY_ACCESS_TOKEN)
    } returns accessToken

    // Mock OkHttpClient call
    val mockCall = mockk<Call>()
    val mockResponse = mockk<Response>()
    every { mockHttpClient.newCall(any()) } returns mockCall
    every { mockResponse.isSuccessful } returns true
    every { mockCall.execute() } returns mockResponse

    // Run the method
    authenticationController.uploadPlaylistImage(mockContext, playlistId)

    // Verify interactions
    verify {
      mockHttpClient.newCall(
          withArg { request ->
            assert(request.method == "PUT")
            assert(
                request.url.toString() == "https://api.spotify.com/v1/playlists/$playlistId/images")
            assert(request.header("Authorization") == "Bearer $accessToken")
            assert(request.header("Content-Type") == "image/jpeg")
            val buffer = okio.Buffer()
            request.body?.writeTo(buffer)
            val requestBodyString = buffer.readUtf8()
            assert(requestBodyString == dummyBase64Image)
          })
    }

    unmockkStatic(BitmapFactory::class)
  }

  @Test
  fun uploadPlaylistImage_ShouldRetryOnTokenExpiration() = runBlocking {
    val playlistId = "testPlaylistId"
    val dummyBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
    val dummyByteArrayOutputStream = ByteArrayOutputStream()
    dummyBitmap.compress(Bitmap.CompressFormat.JPEG, 100, dummyByteArrayOutputStream)
    val dummyByteArray = dummyByteArrayOutputStream.toByteArray()
    val dummyBase64Image = Base64.encodeToString(dummyByteArray, Base64.NO_WRAP)

    // Mock BitmapFactory.decodeResource
    mockkStatic(BitmapFactory::class)
    every { BitmapFactory.decodeResource(mockContext.resources, R.drawable.ic_logo) } returns
        dummyBitmap

    // Mock ByteArrayOutputStream
    mockkConstructor(ByteArrayOutputStream::class)
    every { anyConstructed<ByteArrayOutputStream>().toByteArray() } returns dummyByteArray

    var accessToken = "expiredAccessToken"
    coEvery {
      mockTokenRepository.getAuthToken(AuthTokenRepository.AuthTokenType.SPOTIFY_ACCESS_TOKEN)
    } answers { accessToken }
    coEvery { authenticationController.refreshSpotifyToken() } answers
        {
          accessToken = "newAccessToken"
          true
        }

    // Mock OkHttpClient call
    val mockCall = mockk<Call>()
    val mockResponse = mockk<Response>()
    every { mockHttpClient.newCall(any()) } returns mockCall
    every { mockCall.execute() } returnsMany
        listOf(
            mockResponse.apply {
              every { isSuccessful } returns false
              every { code } returns 401
            },
            mockResponse.apply { every { isSuccessful } returns true })

    // Run the method
    authenticationController.uploadPlaylistImage(mockContext, playlistId)

    // Verify interactions
    verify {
      mockHttpClient.newCall(
          withArg { request ->
            assert(request.header("Authorization") == "Bearer expiredAccessToken")
          })
      mockHttpClient.newCall(
          withArg { request -> assert(request.header("Authorization") == "Bearer newAccessToken") })
    }

    unmockkStatic(BitmapFactory::class)
  }

  @Test
  fun uploadPlaylistImage_ShouldFailOnRefreshFailure() = runBlocking {
    val playlistId = "testPlaylistId"
    val dummyBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)

    // Mock BitmapFactory.decodeResource
    mockkStatic(BitmapFactory::class)
    every { BitmapFactory.decodeResource(mockContext.resources, R.drawable.ic_logo) } returns
        dummyBitmap

    coEvery { authenticationController.refreshSpotifyToken() } returns false

    // Run the method
    authenticationController.uploadPlaylistImage(mockContext, playlistId)

    // Verify interactions
    verify(exactly = 0) { mockHttpClient.newCall(any()) }

    unmockkStatic(BitmapFactory::class)
  }
}
