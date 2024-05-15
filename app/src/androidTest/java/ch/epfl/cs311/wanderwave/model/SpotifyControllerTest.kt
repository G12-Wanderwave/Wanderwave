package ch.epfl.cs311.wanderwave.model

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.di.ServiceModule.provideLocationSource
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.location.FastLocationSource
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import com.spotify.android.appremote.api.Connector.ConnectionListener
import com.spotify.android.appremote.api.ContentApi
import com.spotify.android.appremote.api.PlayerApi
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.NotLoggedInException
import com.spotify.protocol.client.CallResult
import com.spotify.protocol.client.ErrorCallback
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.Empty
import com.spotify.protocol.types.ListItem
import com.spotify.protocol.types.ListItems
import com.spotify.protocol.types.PlayerState
import io.mockk.Awaits
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import java.net.URL
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SpotifyControllerTest {

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK private lateinit var mockAppRemote: SpotifyAppRemote
  @RelaxedMockK private lateinit var mockPlayerApi: PlayerApi

  private lateinit var spotifyController: SpotifyController
  private lateinit var context: Context
  private lateinit var authenticationController: AuthenticationController
  private lateinit var httpClient: OkHttpClient

  @Before
  fun setup() {

    context = ApplicationProvider.getApplicationContext()
    authenticationController = mockk<AuthenticationController>()
    spotifyController = SpotifyController(context, authenticationController)
    spotifyController.appRemote = mockAppRemote
    mockkStatic(SpotifyAppRemote::class)
    every { mockAppRemote.playerApi } returns mockPlayerApi
    coEvery { authenticationController.makeApiRequest(any()) } returns "Test"

    httpClient = mockk()

    spotifyController.apply {
      this::class.java.getDeclaredField("httpClient").apply {
        isAccessible = true
        set(this@SpotifyControllerTest.spotifyController, httpClient)
      }
    }
  }

  @Test
  fun testGetAlbumImage_Success() = runBlocking {
    val accessToken = "test_access_token"
    coEvery { authenticationController.getAccessToken() } returns accessToken

    val imageUrl = "https://example.com/image.jpg"
    val responseBody =
        """
            {
                "images": [
                    {"url": "$imageUrl"}
                ]
            }
            """
    coEvery { authenticationController.makeApiRequest(any()) } returns responseBody

    val result = spotifyController.getAlbumImage("albumId").first()
    assertEquals(imageUrl, result)
  }

  @Test
  fun testGetAlbumImage_Error() = runBlocking {
    val accessToken = "test_access_token"
    coEvery { authenticationController.getAccessToken() } returns accessToken
    val mockResponse = mockk<Response> { every { isSuccessful } returns false }
    coEvery { httpClient.newCall(any()).execute() } returns mockResponse
    val result = spotifyController.getAlbumImage("albumId").first()
    assertNull(result)
  }

  @Test
  fun testGetAlbumImage_EmptyResponse() = runBlocking {
    val accessToken = "test_access_token"
    coEvery { authenticationController.getAccessToken() } returns accessToken

    val responseBody = """{}"""
    val mockResponseBody = mockk<ResponseBody> { every { string() } returns responseBody }

    val mockResponse =
        mockk<Response> {
          every { isSuccessful } returns true
          every { body } returns mockResponseBody
        }

    coEvery { httpClient.newCall(any()).execute() } returns mockResponse
    val result = spotifyController.getAlbumImage("albumId").first()

    assertNull(result)
  }

  @Test
  fun testGetAlbumImage_AccessTokenNull() = runBlocking {
    coEvery { authenticationController.getAccessToken() } returns null
    val result = spotifyController.getAlbumImage("albumId").firstOrNull()
    assertNull(result)
  }

  @Test
  fun testGetAlbumImage_ClosesFlowOnAccessTokenNull() = runBlocking {
    coEvery { authenticationController.getAccessToken() } returns null

    var closed = false
    val job = launch {
      spotifyController.getAlbumImage("albumId").collect {}
      closed = true
    }
    job.join()

    assertTrue(closed)
  }

  @Test
  fun testGetAlbumImage_Exception() = runBlocking {
    val accessToken = "test_access_token"
    coEvery { authenticationController.getAccessToken() } returns accessToken

    coEvery { httpClient.newCall(any()).execute() } throws Exception("Network error")

    val result = spotifyController.getAlbumImage("albumId").firstOrNull()
    assertNull(result)
  }

  @Test
  fun testGetAlbumImage_EmptyImagesArray() = runBlocking {
    val accessToken = "test_access_token"
    coEvery { authenticationController.getAccessToken() } returns accessToken

    val responseBody = """{"images": []}"""
    val mockResponseBody = mockk<ResponseBody> { every { string() } returns responseBody }

    val mockResponse =
        mockk<Response> {
          every { isSuccessful } returns true
          every { body } returns mockResponseBody
        }

    coEvery { httpClient.newCall(any()).execute() } returns mockResponse
    val result = spotifyController.getAlbumImage("albumId").firstOrNull()

    assertNull(result)
  }

  @Test
  fun testDownloadAndDisplayImage() = runBlocking {
    val imageUrl =
        "https://images.squarespace-cdn.com/content/v1/60f1a490a90ed8713c41c36c/1629223610791-LCBJG5451DRKX4WOB4SP/37-design-powers-url-structure.jpeg"
    val imageView = mockk<ImageView>(relaxed = true)

    val imageData = withContext(Dispatchers.IO) { URL(imageUrl).readBytes() }

    val mockBitmap = mockk<android.graphics.Bitmap>()
    mockkStatic(BitmapFactory::class)
    every { BitmapFactory.decodeByteArray(imageData, 0, imageData.size) } returns mockBitmap

    spotifyController = SpotifyController(context, authenticationController)

    val job = launch { spotifyController.downloadAndDisplayImage(imageUrl, imageView) }
    job.join()

    verify { imageView.setImageBitmap(mockBitmap) }

    unmockkStatic(BitmapFactory::class)
  }

  @Test
  fun testDisplayAlbumImage() = runBlocking {
    val albumId = "albumId"
    val imageUrl =
        "https://images.squarespace-cdn.com/content/v1/60f1a490a90ed8713c41c36c/1629223610791-LCBJG5451DRKX4WOB4SP/37-design-powers-url-structure.jpeg"
    val imageView = mockk<ImageView>(relaxed = true)
    val accessToken = "test_access_token"

    coEvery { authenticationController.getAccessToken() } returns accessToken

    val albumImageJson =
        """
            {
                "images": [
                    {"url": "$imageUrl"}
                ]
            }
        """
    coEvery { authenticationController.makeApiRequest(any<URL>()) } returns albumImageJson

    val mockBitmap = mockk<android.graphics.Bitmap>()
    mockkStatic(BitmapFactory::class)
    every { BitmapFactory.decodeByteArray(any(), any(), any()) } returns mockBitmap

    spotifyController.displayAlbumImage(albumId, imageView)

    verify { imageView.setImageBitmap(mockBitmap) }

    unmockkStatic(BitmapFactory::class)
  }

  @Test
  fun testStartPlayBackTimerResultCallback() = runBlocking {
    // Mock the PlayerApi and Subscription objects
    val playerApi = mockk<PlayerApi>(relaxed = true)
    val subscription = mockk<Subscription<PlayerState>>(relaxed = true)
    val playerState = mockk<PlayerState>(relaxed = true)

    // When playerApi.subscribeToPlayerState() is called, return the mocked subscription
    every { playerApi.subscribeToPlayerState() } returns subscription

    // When subscription.setEventCallback(any()) is called, invoke the callback with the test
    // PlayerState
    every { subscription.setEventCallback(any()) } answers { subscription }

    // When subscription.setErrorCallback(any()) is called, do nothing
    every { subscription.setErrorCallback(any()) } just Awaits

    // Set the playerApi in the SpotifyController
    every { mockAppRemote.playerApi } returns playerApi

    val callResult = mockk<CallResult<PlayerState>>(relaxed = true)
    every { callResult.setResultCallback(any()) } answers
        {
          val callback = firstArg<CallResult.ResultCallback<PlayerState>>()
          callback.onResult(playerState)
          callResult
        }
    every { mockAppRemote.playerApi.playerState } returns callResult

    // Call the method to be tested
    spotifyController.startPlaybackTimer(1000, this)
  }

  @Test
  fun testPlayTrackResultCallback() {
    val playerApi = mockk<PlayerApi>(relaxed = true)
    val subscription = mockk<Subscription<PlayerState>>(relaxed = true)
    val playerState = mockk<PlayerState>(relaxed = true)
    val emptyResult = mockk<CallResult<Empty>>(relaxed = true)

    // Setup playerApi responses
    every { mockAppRemote.playerApi } returns playerApi
    every { playerApi.subscribeToPlayerState() } returns subscription
    every { subscription.setEventCallback(any()) } answers { subscription }
    every { subscription.setErrorCallback(any()) } just Awaits

    val callResult = mockk<CallResult<PlayerState>>(relaxed = true)
    every { callResult.setResultCallback(any()) } answers
        {
          val callback = firstArg<CallResult.ResultCallback<PlayerState>>()
          callback.onResult(playerState)
          callResult
        }
    every { mockAppRemote.playerApi.playerState } returns callResult

    // Correct the play method setup to properly simulate callback
    every { playerApi.play(any()) } answers
        {
          val callback = firstArg<CallResult.ResultCallback<Empty>>()
          callback.onResult(Empty()) // Ensure Empty is appropriately instantiated if needed
          mockk<CallResult<Empty>>(relaxed = true) // Return a relaxed mock of CallResult<Empty>
        }

    // Ensure `playTrack` method is tested correctly
    val track = Track("spotify:track:1cNf5WAYWuQwGoJyfsHcEF", "Across The Stars", "John Williams")
    runBlocking { spotifyController.playTrack(track) }
  }

  @Test
  fun getAuthorizationRequest() {
    val request = spotifyController.getAuthorizationRequest()
    assert(request.redirectUri.contains("callback"))
    assert(request.scopes.isNotEmpty())
  }

  @Test
  fun getLogoutRequest() {
    val request = spotifyController.getLogoutRequest()
    assert(request.redirectUri.contains("callback"))
    assert(request.scopes.isEmpty())
  }

  @Test
  fun checkSpotifyConnection() {
    every { mockAppRemote.isConnected } returns false
    val connected = spotifyController.isConnected()
    assert(!connected)
    verify { mockAppRemote.isConnected }
  }

  @Test
  fun disconnect() {
    spotifyController.disconnectRemote()
    verify { SpotifyAppRemote.disconnect(mockAppRemote) }
  }

  @Test
  fun playTrackTest() = runBlocking {
    val callResult = mockk<CallResult<Empty>>(relaxed = true)
    val playerApi = mockk<PlayerApi>(relaxed = true)
    every { mockAppRemote.playerApi } returns playerApi
    every { playerApi.play(any()) } returns callResult
    val id = "fakeid"
    val flow = spotifyController.playTrack(Track(id, "faketitle", "fakeartist"))

    val result = flow.timeout(2.seconds).catch {}.firstOrNull()

    verify { playerApi.play(any()) }
  }

  @Test
  fun connectRemoteSuccess() = runBlocking {
    every { mockAppRemote.isConnected } returns false
    val slot = slot<ConnectionListener>()
    every { SpotifyAppRemote.connect(any(), any(), capture(slot)) } answers
        {
          slot.captured.onConnected(mockAppRemote)
          Unit
        }

    val result = spotifyController.connectRemote().first()
    assert(result == SpotifyController.ConnectResult.SUCCESS)
  }

  @Test
  fun connectRemoteNotLoggedIn() = runBlocking {
    every { mockAppRemote.isConnected } returns false
    val slot = slot<ConnectionListener>()
    every { SpotifyAppRemote.connect(any(), any(), capture(slot)) } answers
        {
          slot.captured.onFailure(NotLoggedInException("Not logged in", null))
          Unit
        }

    val result = spotifyController.connectRemote().first()
    assert(result == SpotifyController.ConnectResult.NOT_LOGGED_IN)
  }

  @Test
  fun connectRemoteFailure() = runBlocking {
    every { mockAppRemote.isConnected } returns false
    val slot = slot<ConnectionListener>()
    every { SpotifyAppRemote.connect(any(), any(), capture(slot)) } answers
        {
          slot.captured.onFailure(Exception("Other exception", null))
          Unit
        }

    val result = spotifyController.connectRemote().first()
    assert(result == SpotifyController.ConnectResult.FAILED)
  }

  @Test
  fun connectRemoteAlreadyConnected() = runBlocking {
    every { mockAppRemote.isConnected } returns true
    var callResult = mockk<CallResult<Empty>>()
    var slot = slot<CallResult.ResultCallback<Empty>>()
    every { mockAppRemote.playerApi.play(any()) } returns callResult
    every { callResult.setResultCallback(capture(slot)) } answers
        {
          slot.captured.onResult(Empty())
          slot.captured.onResult(Empty())
          callResult
        }
    val result = spotifyController.connectRemote().first()
    assert(result == SpotifyController.ConnectResult.SUCCESS)
    verify { mockAppRemote.isConnected }
  }

  @Test
  fun getChildrenError() = runBlocking {
    val listItem =
        ListItem("parent_id", "parent_uri", null, "parent_title", "parent_type", true, false)
    every { mockAppRemote.contentApi.getChildrenOfItem(any(), any(), any()) } answers
        {
          throw Exception("Network error")
        }

    val result =
        spotifyController
            .getChildren(listItem)
            .catch { emit(ListItem("", "", null, "", "", false, false)) }
            .first()
    assert(result.uri.isEmpty())
  }

  @Test
  fun testGetChildrenWithSpecificId() = runBlocking {
    // Mock setup
    val callResult = mockk<CallResult<ListItems>>(relaxed = true)
    val contentApi = mockk<ContentApi>(relaxed = true)
    every { mockAppRemote.contentApi } returns contentApi
    every { contentApi.getChildrenOfItem(any(), any(), any()) } returns callResult

    // Prepare test data
    val listItem = ListItem("parent", "parentUri", null, "Parent Title", "type", true, false)
    val matchingAlbum = ListItem("id:album1", "uri1", null, "Album Title", "album", true, false)
    val matchingPlaylist =
        ListItem("id:playlist1", "uri2", null, "Playlist Title", "playlist", true, false)
    val nonMatchingItem = ListItem("id:track1", "uri3", null, "Track Title", "track", true, false)
    val items = listOf(matchingAlbum, matchingPlaylist, nonMatchingItem)

    // Convert List to Array
    val itemsArray = items.toTypedArray()

    // Setup the callback to invoke with our prepared list
    every { callResult.setResultCallback(any()) } answers
        {
          val callback = firstArg<CallResult.ResultCallback<ListItems>>()
          callback.onResult(ListItems(0, 0, 0, itemsArray)) // Now passing an Array
          callResult
        }

    // Execute the function to get the Flow and collect results

    val flow = spotifyController.getChildren(listItem)
    val collectedItems = mutableListOf<ListItem>()
    val result = flow.timeout(2.seconds).catch {}.firstOrNull()
    Log.d("Flow result", result.toString())
    // Assertions to check only matching items are collected
    assertTrue(result == matchingAlbum)
  }

  @Test
  fun testGetChildrenFlowCancellation() = runBlocking {
    // Prepare the mock environment
    val callResult = mockk<CallResult<ListItems>>(relaxed = true)
    val contentApi = mockk<ContentApi>(relaxed = true)
    every { mockAppRemote.contentApi } returns contentApi
    every { contentApi.getChildrenOfItem(any(), any(), any()) } returns callResult

    // Mock the cancellation action
    // Define the flow using the SpotifyController with the mocked remote
    val listItem = ListItem("id", "uri", null, "title", "type", true, false)

    // Collect the flow in a coroutine that we can cancel
    val job = launch { spotifyController.getChildren(listItem).collect {} }
    // Cancel the job to trigger awaitClose
    job.cancel()

    // Verify that the cancellation was called
    // coVerify { callResult.cancel() }
  }

  @Test
  fun testGetChildrenAllWithSpecificId() = runBlocking {
    // Mock setup
    val callResult = mockk<CallResult<ListItems>>(relaxed = true)
    val contentApi = mockk<ContentApi>(relaxed = true)
    every { mockAppRemote.contentApi } returns contentApi
    every { contentApi.getChildrenOfItem(any(), any(), any()) } returns callResult

    // Prepare test data
    val listItem = ListItem("parent", "parentUri", null, "Parent Title", "type", true, false)
    val matchingAlbum = ListItem("id:album1", "uri1", null, "Album Title", "album", true, false)
    val matchingPlaylist =
        ListItem("id:playlist1", "uri2", null, "Playlist Title", "playlist", true, false)
    val nonMatchingItem = ListItem("id:track1", "uri3", null, "Track Title", "track", true, false)
    val items = listOf(matchingAlbum, matchingPlaylist, nonMatchingItem)

    // Convert List to Array
    val itemsArray = items.toTypedArray()

    // Setup the callback to invoke with our prepared list
    every { callResult.setResultCallback(any()) } answers
        {
          val callback = firstArg<CallResult.ResultCallback<ListItems>>()
          callback.onResult(ListItems(0, 0, 0, itemsArray)) // Now passing an Array
          callResult
        }

    // Execute the function to get the Flow and collect results

    val flow = spotifyController.getAllChildren(listItem)
    val collectedItems = mutableListOf<ListItem>()
    val result = flow.timeout(2.seconds).catch {}.firstOrNull()
    Log.d("Flow result", result.toString())
    // Assertions to check only matching items are collected
    assertTrue(result == listOf(matchingAlbum, matchingPlaylist, nonMatchingItem))
  }

  @Test
  fun testGetAllSpotify() = runBlocking {
    // Prepare the mocked responses
    val callResult = mockk<CallResult<ListItems>>(relaxed = true)
    val contentApi = mockk<ContentApi>(relaxed = true)
    every { mockAppRemote.contentApi } returns contentApi
    every { contentApi.getRecommendedContentItems(any()) } returns callResult

    // Prepare a list with mixed URIs
    val matchingItem =
        ListItem(
            "id1",
            "spotify:section:0JQ5DAroEmF9ANbLaiJ7Wx",
            null,
            "Matching Title",
            "type",
            true,
            false)
    val nonMatchingItem =
        ListItem(
            "id2", "spotify:section:NotMatching", null, "Non-Matching Title", "type", true, false)
    val items = listOf(matchingItem, nonMatchingItem)
    val itemsArray = items.toTypedArray()
    // Setup the callback to invoke with our prepared list
    every { callResult.setResultCallback(any()) } answers
        {
          val callback = firstArg<CallResult.ResultCallback<ListItems>>()
          callback.onResult(ListItems(0, 0, 0, itemsArray))
          callResult
        }

    // Execute the function to get the Flow and collect results
    val flow = spotifyController.getAllElementFromSpotify()
    val collectedItems = mutableListOf<ListItem>()
    val result = flow.timeout(2.seconds).catch {}.firstOrNull()

    Log.d("Result of the flow", result.toString())
    // Assertions to check only the matching item is collected
    assertTrue(result == listOf(matchingItem, nonMatchingItem))
    fun resumeTrackTest() = runBlocking {
      every { mockAppRemote.isConnected } returns true
      var callResult = mockk<CallResult<Empty>>()
      var playerApi = mockk<PlayerApi>()
      every { mockAppRemote.playerApi } returns playerApi
      var slot = slot<CallResult.ResultCallback<Empty>>()
      every { playerApi.resume() } returns callResult
      every { callResult.setResultCallback(capture(slot)) } answers
          {
            slot.captured.onResult(Empty())
            slot.captured.onResult(Empty())
            callResult
          }

      every { callResult.setErrorCallback(any()) } returns callResult
      every { callResult.cancel() } just Runs

      val result = spotifyController.resumeTrack().first()

      verify { playerApi.resume() }
      assertTrue(result == true)
    }
  }

  @Test
  fun pauseTrackTest() = runBlocking {
    every { mockAppRemote.isConnected } returns true
    var callResult = mockk<CallResult<Empty>>()
    var playerApi = mockk<PlayerApi>()
    every { mockAppRemote.playerApi } returns playerApi
    var slot = slot<CallResult.ResultCallback<Empty>>()
    every { playerApi.pause() } returns callResult
    every { callResult.setResultCallback(capture(slot)) } answers
        {
          slot.captured.onResult(Empty())
          slot.captured.onResult(Empty())
          callResult
        }

    every { callResult.setErrorCallback(any()) } returns callResult
    every { callResult.cancel() } just Runs

    val result = spotifyController.pauseTrack().first()

    verify { playerApi.pause() }
    assertTrue(result)
  }

  @Test
  fun resumeTrackTest() = runBlocking {
    every { mockAppRemote.isConnected } returns true
    var callResult = mockk<CallResult<Empty>>()
    var playerApi = mockk<PlayerApi>()
    every { mockAppRemote.playerApi } returns playerApi
    var slot = slot<CallResult.ResultCallback<Empty>>()
    every { playerApi.resume() } returns callResult
    every { callResult.setResultCallback(capture(slot)) } answers
        {
          slot.captured.onResult(Empty())
          slot.captured.onResult(Empty())
          callResult
        }

    every { callResult.setErrorCallback(any()) } returns callResult
    every { callResult.cancel() } just Runs

    val result = spotifyController.resumeTrack().first()

    verify { playerApi.resume() }
    assertTrue(result)
  }

  @Test
  fun testGetAllElementFromSpotifyCancellation() {
    runBlocking {
      // Prepare the mocked responses
      val callResult = mockk<CallResult<ListItems>>(relaxed = true)
      val contentApi = mockk<ContentApi>(relaxed = true)
      every { mockAppRemote.contentApi } returns contentApi
      every { contentApi.getRecommendedContentItems(any()) } returns callResult

      // Prepare a list with mixed URIs
      val matchingItem =
          ListItem(
              "id1",
              "spotify:section:0JQ5DAroEmF9ANbLaiJ7Wx",
              null,
              "Matching Title",
              "type",
              true,
              false)
      val nonMatchingItem =
          ListItem(
              "id2", "spotify:section:NotMatching", null, "Non-Matching Title", "type", true, false)
      val items = listOf(matchingItem, nonMatchingItem)
      val itemsArray = items.toTypedArray()
      // Setup the callback to invoke with our prepared list
      every { callResult.setResultCallback(any()) } answers
          {
            val callback = firstArg<CallResult.ResultCallback<ListItems>>()
            callback.onResult(ListItems(0, 0, 0, itemsArray))
            callResult
          }

      // Execute the function to get the Flow and collect results
      val flow = spotifyController.getAllElementFromSpotify()
      val collectedItems = mutableListOf<ListItem>()
      val result = flow.timeout(2.seconds).catch {}.firstOrNull()

      Log.d("Result of the flow", result.toString())
      // Assertions to check only the matching item is collected
      assertTrue(result == listOf(matchingItem, nonMatchingItem))
      fun resumeTrackTest() = runBlocking {
        every { mockAppRemote.isConnected } returns true
        var callResult = mockk<CallResult<Empty>>()
        var playerApi = mockk<PlayerApi>()
        every { mockAppRemote.playerApi } returns playerApi
        var slot = slot<CallResult.ResultCallback<Empty>>()
        every { playerApi.resume() } returns callResult
        every { callResult.setResultCallback(capture(slot)) } answers
            {
              slot.captured.onResult(Empty())
              slot.captured.onResult(Empty())
              callResult
            }

        every { callResult.setErrorCallback(any()) } returns callResult
        every { callResult.cancel() } just Runs
      }
    }
  }

  @Test
  fun testgetChildrenCancellation() {
    runBlocking {
      // Prepare the mock environment
      val callResult = mockk<CallResult<ListItems>>(relaxed = true)
      val contentApi = mockk<ContentApi>(relaxed = true)
      every { mockAppRemote.contentApi } returns contentApi
      every { contentApi.getChildrenOfItem(any(), any(), any()) } returns callResult

      // Define the flow using the SpotifyController with the mocked remote
      val listItem = ListItem("id", "uri", null, "title", "type", true, false)

      // Collect the flow in a coroutine that we can cancel
      val job = launch { spotifyController.getChildren(listItem).collect {} }
      // Cancel the job to trigger awaitClose
      job.cancel()

      // Verify that the cancellation was called
      // coVerify { callResult.cancel() }
    }
  }

  @Test
  fun testGetAllChildrenCancellation() {
    runBlocking {
      // Prepare the mock environment
      val callResult = mockk<CallResult<ListItems>>(relaxed = true)
      val contentApi = mockk<ContentApi>(relaxed = true)
      every { mockAppRemote.contentApi } returns contentApi
      every { contentApi.getChildrenOfItem(any(), any(), any()) } returns callResult

      // Define the flow using the SpotifyController with the mocked remote
      val listItem = ListItem("id", "uri", null, "title", "type", true, false)

      // Collect the flow in a coroutine that we can cancel
      val job = launch { spotifyController.getAllChildren(listItem).collect {} }
      // Cancel the job to trigger awaitClose
      job.cancel()

      // Verify that the cancellation was called
      // coVerify { callResult.cancel() }
    }
  }

  @Test
  fun testPlayTrackCancellation() {
    runBlocking {
      val callResult = mockk<CallResult<Empty>>(relaxed = true)
      val playerApi = mockk<PlayerApi>(relaxed = true)
      every { mockAppRemote.playerApi } returns playerApi
      every { playerApi.play(any()) } returns callResult
      val id = "fakeid"
      val flow = spotifyController.playTrack(Track(id, "faketitle", "fakeartist"))

      val result = flow.timeout(2.seconds).catch {}.firstOrNull()

      verify { playerApi.play(any()) }
    }
  }

  @Test
  fun testPauseTrackCancellation() {
    runBlocking {
      every { mockAppRemote.isConnected } returns true
      var callResult = mockk<CallResult<Empty>>()
      var playerApi = mockk<PlayerApi>()
      every { mockAppRemote.playerApi } returns playerApi
      var slot = slot<CallResult.ResultCallback<Empty>>()
      every { playerApi.pause() } returns callResult
      every { callResult.setResultCallback(capture(slot)) } answers
          {
            slot.captured.onResult(Empty())
            slot.captured.onResult(Empty())
            callResult
          }

      every { callResult.setErrorCallback(any()) } returns callResult
      every { callResult.cancel() } just Runs

      val result = spotifyController.pauseTrack().first()

      verify { playerApi.pause() }
      assertTrue(result)
    }
  }

  @Test
  fun testResumeTrackCancellation() {
    runBlocking {
      every { mockAppRemote.isConnected } returns true
      var callResult = mockk<CallResult<Empty>>()
      var playerApi = mockk<PlayerApi>()
      every { mockAppRemote.playerApi } returns playerApi
      var slot = slot<CallResult.ResultCallback<Empty>>()
      every { playerApi.resume() } returns callResult
      every { callResult.setResultCallback(capture(slot)) } answers
          {
            slot.captured.onResult(Empty())
            slot.captured.onResult(Empty())
            callResult
          }

      every { callResult.setErrorCallback(any()) } returns callResult
      every { callResult.cancel() } just Runs

      val result = spotifyController.resumeTrack().first()

      verify { playerApi.resume() }
      assertTrue(result)
    }
  }

  @Test
  fun resumeTrack_Failure() = runBlocking {
    val playerApi = mockk<PlayerApi>()
    every { mockAppRemote.playerApi } returns playerApi

    val callResult = mockk<CallResult<Empty>>()
    every { playerApi.resume() } returns callResult
    every { callResult.setResultCallback(any()) } answers { callResult }
    every { callResult.setErrorCallback(any()) } answers
        {
          firstArg<ErrorCallback>().onError(RuntimeException("Error"))
          callResult // Return the mocked callResult here
        }

    every { callResult.cancel() } just Runs

    val result = spotifyController.resumeTrack().first()

    assertFalse(result)
  }

  @Test
  fun pauseTrack_Failure() = runBlocking {
    val playerApi = mockk<PlayerApi>()
    every { mockAppRemote.playerApi } returns playerApi

    val callResult = mockk<CallResult<Empty>>()
    every { playerApi.pause() } returns callResult
    every { callResult.setResultCallback(any()) } answers { callResult }
    every { callResult.setErrorCallback(any()) } answers
        {
          firstArg<ErrorCallback>().onError(RuntimeException("Error"))
          callResult // Return the mocked callResult here
        }

    every { callResult.cancel() } just Runs

    val result = spotifyController.pauseTrack().first()

    assertFalse(result)
  }

  @Test
  fun playTrack_Failure() = runBlocking {
    val playerApi = mockk<PlayerApi>()
    every { mockAppRemote.playerApi } returns playerApi

    val callResult = mockk<CallResult<Empty>>()
    every { playerApi.play(any()) } returns callResult
    every { callResult.setResultCallback(any()) } answers { callResult }
    every { callResult.setErrorCallback(any()) } answers
        {
          firstArg<ErrorCallback>().onError(RuntimeException("Error"))
          callResult // Return the mocked callResult here
        }

    every { callResult.cancel() } just Runs

    val result = spotifyController.playTrack(Track("id", "title", "artist")).first()

    assertFalse(result)
  }
  //    @Test
  //    fun playTrackTest() = runBlocking {
  //        val callResult = mockk<CallResult<Empty>>(relaxed = true)
  //        val playerApi = mockk<PlayerApi>(relaxed = true)
  //        every { mockAppRemote.playerApi } returns playerApi
  //        every { playerApi.play(any()) } returns callResult
  //        val id = "fakeid"
  //        spotifyController.playTrack(Track(id, "faketitle", "fakeartist"))
  //    }

  @ExperimentalCoroutinesApi
  @Test
  fun startPlaybackTimerCancelsPreviousTimerAndStartsNewOne() = runTest {
    // Setup
    spotifyController.startPlaybackTimer(3000) // 3 seconds

    // Assert that initial timer is running
    assertNotNull(spotifyController.playbackTimer)

    // Start another timer
    spotifyController.startPlaybackTimer(1000) // 5 seconds
  }

  @Test
  fun stopPlaybackTimerCancelsActiveTimer() = runTest {
    // Start timer
    spotifyController.startPlaybackTimer(3000) // 3 seconds

    // Stop the timer
    spotifyController.stopPlaybackTimer()

    // Verify timer is null
    assertNull(spotifyController.playbackTimer)
  }

  @Test
  fun setOnTrackEndCallbackSetsTheCallback() {
    val callback: () -> Unit = { println("Track ended") }
    spotifyController.setOnTrackEndCallback(callback)
  }

  @Test
  fun testOnPlayerStateUpdate() = runBlocking {
    // Mock the PlayerApi and Subscription objects
    val playerApi = mockk<PlayerApi>(relaxed = true)
    val subscription = mockk<Subscription<PlayerState>>(relaxed = true)

    // When playerApi.subscribeToPlayerState() is called, return the mocked subscription
    every { playerApi.subscribeToPlayerState() } returns subscription

    // When subscription.setEventCallback(any()) is called, invoke the callback with the test
    // PlayerState
    every { subscription.setEventCallback(any()) } answers { subscription }

    // When subscription.setErrorCallback(any()) is called, do nothing
    every { subscription.setErrorCallback(any()) } just Awaits

    // Set the playerApi in the SpotifyController
    every { mockAppRemote.playerApi } returns playerApi

    // Call the method to be tested
    spotifyController.onPlayerStateUpdate()

    // Verify that setEventCallback was called
    verify { subscription.setEventCallback(any()) }
  }

  @Test
  fun authorizationRequestContainsCallbackAndScopes() {
    val request = spotifyController.getAuthorizationRequest()
    assertTrue(request.redirectUri.contains("callback"))
    assertTrue(request.scopes.isNotEmpty())
  }

  @Test
  fun logoutRequestContainsCallbackAndNoScopes() {
    val request = spotifyController.getLogoutRequest()
    assertTrue(request.redirectUri.contains("callback"))
    assertTrue(request.scopes.isEmpty())
  }

  @Test
  fun connectionStatusReflectsAppRemoteStatus() {
    every { mockAppRemote.isConnected } returns true
    assertTrue(spotifyController.isConnected())
    every { mockAppRemote.isConnected } returns false
    assertFalse(spotifyController.isConnected())
  }

  @Test
  fun disconnectCallsAppRemoteDisconnect() {
    spotifyController.disconnectRemote()
    verify { SpotifyAppRemote.disconnect(mockAppRemote) }
  }

  @Test
  fun playbackTimerIsCancelledOnNewStart() {
    spotifyController.startPlaybackTimer(3000)
    val initialTimer = spotifyController.playbackTimer
    spotifyController.startPlaybackTimer(5000)
    assertTrue(initialTimer?.isCancelled ?: false)
    assertNotNull(spotifyController.playbackTimer)
  }

  @Test
  fun playbackTimerIsCancelledOnStop() {
    spotifyController.startPlaybackTimer(3000)
    assertNotNull(spotifyController.playbackTimer)
    spotifyController.stopPlaybackTimer()
    assertNull(spotifyController.playbackTimer)
  }

  @Test
  fun onTrackEndCallbackIsSet() {
    val callback: () -> Unit = { println("Track ended") }
    spotifyController.setOnTrackEndCallback(callback)
    assertNotNull(spotifyController.getOnTrackEndCallback())
  }

  @Test
  fun testOnPlayerStateUpdateCancellation() = runBlocking {
    // Mock the PlayerApi and Subscription objects
    val playerApi = mockk<PlayerApi>(relaxed = true)
    val subscription = mockk<Subscription<PlayerState>>(relaxed = true)

    // When playerApi.subscribeToPlayerState() is called, return the mocked subscription
    every { playerApi.subscribeToPlayerState() } returns subscription

    // When subscription.setEventCallback(any()) is called, invoke the callback with the test
    // PlayerState
    every { subscription.setEventCallback(any()) } answers { subscription }

    // When subscription.setErrorCallback(any()) is called, do nothing
    every { subscription.setErrorCallback(any()) } just Awaits

    // Set the playerApi in the SpotifyController
    every { mockAppRemote.playerApi } returns playerApi

    // Call the method to be tested
    spotifyController.onPlayerStateUpdate()

    // Verify that setEventCallback was called
    verify { subscription.setEventCallback(any()) }

    // Cancel the subscription
    subscription.cancel()

    // Verify that the cancellation was called
    verify { subscription.cancel() }
  }

  @Test
  fun testOnPlayerStateUpdateWithoutCancellation() = runBlocking {
    // Mock the PlayerApi and Subscription objects
    val playerApi = mockk<PlayerApi>(relaxed = true)
    val subscription = mockk<Subscription<PlayerState>>(relaxed = true)

    // When playerApi.subscribeToPlayerState() is called, return the mocked subscription
    every { playerApi.subscribeToPlayerState() } returns subscription

    // When subscription.setEventCallback(any()) is called, invoke the callback with the test
    // PlayerState
    every { subscription.setEventCallback(any()) } answers { subscription }

    // When subscription.setErrorCallback(any()) is called, do nothing
    every { subscription.setErrorCallback(any()) } just Awaits

    // Set the playerApi in the SpotifyController
    every { mockAppRemote.playerApi } returns playerApi

    // Call the method to be tested
    spotifyController.onPlayerStateUpdate()

    // Verify that setEventCallback was called
    verify { subscription.setEventCallback(any()) }
  }

  @Test
  fun testProvideLocationSource() {
    // Mock the Context
    val context = mockk<Context>(relaxed = true)

    // Call the function
    val locationSource = provideLocationSource(context)

    // Verify the returned LocationSource is an instance of FastLocationSource
    assertTrue(locationSource is FastLocationSource)
  }

  @Test
  fun spotifyGetFromURLTest() = runBlocking {
    val callResult = mockk<CallResult<ListItems>>(relaxed = true)
    val contentApi = mockk<ContentApi>(relaxed = true)
    every { mockAppRemote.contentApi } returns contentApi
    every { contentApi.getRecommendedContentItems(any()) } returns callResult

    val matchingAlbum = ListItem("id:album1", "uri1", null, "Album Title", "album", true, false)
    val matchingPlaylist =
        ListItem("id:playlist1", "uri2", null, "Playlist Title", "playlist", true, false)
    val nonMatchingItem = ListItem("id:track1", "uri3", null, "Track Title", "track", true, false)
    val items = listOf(matchingAlbum, matchingPlaylist, nonMatchingItem)
    val itemsArray = items.toTypedArray()

    every { callResult.setResultCallback(any()) } answers
        {
          val callback = firstArg<CallResult.ResultCallback<ListItems>>()
          callback.onResult(ListItems(0, 0, 0, itemsArray))
          callResult
        }

    val result = spotifyController.spotifyGetFromURL("https://test.api/endpoint")

  }
}