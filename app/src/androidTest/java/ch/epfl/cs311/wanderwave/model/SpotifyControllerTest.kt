package ch.epfl.cs311.wanderwave.model

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import com.spotify.android.appremote.api.Connector.ConnectionListener
import com.spotify.android.appremote.api.ContentApi
import com.spotify.android.appremote.api.PlayerApi
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.NotLoggedInException
import com.spotify.protocol.client.CallResult
import com.spotify.protocol.types.Empty
import com.spotify.protocol.types.ListItem
import com.spotify.protocol.types.ListItems
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlin.Exception
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SpotifyControllerTest {

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK private lateinit var mockAppRemote: SpotifyAppRemote

  private lateinit var spotifyController: SpotifyController

  @Before
  fun setup() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    spotifyController = SpotifyController(context)
    spotifyController.appRemote = mockAppRemote
    mockkStatic(SpotifyAppRemote::class)
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
  fun playTrack() = runBlocking {
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
  fun getTrackAndChildren() {
    runBlocking {
      val listItem = ListItem("id", "uri", null, "title", "type", true, false)
      val emptyListItem = ListItem("", "", null, "", "", false, false)
      val callResult = mockk<CallResult<ListItems>>(relaxed = true)

      val contentApi = mockk<ContentApi>(relaxed = true)
      every { mockAppRemote.contentApi } returns contentApi
      every { contentApi.getRecommendedContentItems(any()) } returns callResult

      val flow = spotifyController.getTrack()

      val result = flow.timeout(2.seconds).catch {}.firstOrNull()
      spotifyController.getChildren(listItem).timeout(2.seconds).catch {}.firstOrNull()

      verify { contentApi.getRecommendedContentItems(any()) }
    }
  }

  @Test
  fun getTrackError() = runBlocking {
    every { mockAppRemote.contentApi.getRecommendedContentItems(any()) } answers
        {
          throw Exception("Network error")
        }

    val result =
        spotifyController
            .getTrack()
            .catch { emit(ListItem("", "", null, "", "", false, false)) }
            .first()
    assert(result.uri.isEmpty())
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
  fun testGetTrackWithSpecificUri() = runBlocking {
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
    val flow = spotifyController.getTrack()
    val collectedItems = mutableListOf<ListItem>()
    val result = flow.timeout(2.seconds).catch {}.firstOrNull()

    Log.d("Result of the flow", result.toString())
    // Assertions to check only the matching item is collected
    assertTrue(result == matchingItem)
    assertFalse(result == nonMatchingItem)
  }
}
// @Test
// fun testGetChildrenWithSpecificId() = runBlocking {
//    // Mock setup
//    val mockAppRemote = mockk<SpotifyAppRemote>(relaxed = true)
//    val mockContentApi = mockk<ContentApi>(relaxed = true)
//    val callResult = mockk<CallResult<ListItems>>(relaxed = true)
//
//    every { mockAppRemote.contentApi } returns mockContentApi
//    every { mockContentApi.getChildrenOfItem(any(), any(), any()) } returns callResult
//
//    // Prepare test data
//    val listItem = ListItem("parent", "parentUri", null, "Parent Title", "type", true, false)
//    val matchingAlbum = ListItem("id:album1", "uri1", null, "Album Title", "album", true, false)
//    val matchingPlaylist = ListItem("id:playlist1", "uri2", null, "Playlist Title", "playlist",
// true, false)
//    val nonMatchingItem = ListItem("id:track1", "uri3", null, "Track Title", "track", true, false)
//    val items = listOf(matchingAlbum, matchingPlaylist, nonMatchingItem)
//
//    // Convert List to Array
//    val itemsArray = items.toTypedArray()
//
//    // Setup the callback to invoke with our prepared list
//    every { callResult.setResultCallback(any()) } answers {
//        val callback = firstArg<CallResult.ResultCallback<ListItems>>()
//        callback.onResult(ListItems(0, 0, 0, itemsArray)) // Now passing an Array
//        callResult
//    }
//    every { callResult.setErrorCallback(any()) } just Runs
//
//    // Execute the function to get the Flow and collect results
//    val flow = SpotifyController(mockAppRemote).getChildren(listItem)
//    val collectedItems = mutableListOf<ListItem>()
//    flow.collect { collectedItems.add(it) }
//
//    // Assertions to check only matching items are collected
//    assertTrue(collectedItems.contains(matchingAlbum))
//    assertTrue(collectedItems.contains(matchingPlaylist))
//    assertFalse(collectedItems.contains(nonMatchingItem))
//    verify { mockContentApi.getChildrenOfItem(eq(listItem), eq(50), eq(0)) } // Verify that the
// method was called with expected args
// }
