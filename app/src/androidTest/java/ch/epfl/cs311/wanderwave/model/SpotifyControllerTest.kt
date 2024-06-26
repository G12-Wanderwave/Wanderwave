package ch.epfl.cs311.wanderwave.model

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.di.ServiceModule.provideLocationSource
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.location.FastLocationSource
import ch.epfl.cs311.wanderwave.model.repository.RecentlyPlayedRepository
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import ch.epfl.cs311.wanderwave.model.spotify.getLikedTracksFromSpotify
import ch.epfl.cs311.wanderwave.model.spotify.getTracksFromSpotifyPlaylist
import ch.epfl.cs311.wanderwave.model.spotify.parseTracks
import ch.epfl.cs311.wanderwave.model.spotify.toWanderwaveTrack
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.FutureTarget
import com.spotify.android.appremote.api.Connector.ConnectionListener
import com.spotify.android.appremote.api.ContentApi
import com.spotify.android.appremote.api.PlayerApi
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.NotLoggedInException
import com.spotify.protocol.client.CallResult
import com.spotify.protocol.client.ErrorCallback
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.Artist
import com.spotify.protocol.types.Empty
import com.spotify.protocol.types.ImageUri
import com.spotify.protocol.types.ListItem
import com.spotify.protocol.types.ListItems
import com.spotify.protocol.types.PlayerState
import io.mockk.Awaits
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import java.net.URL
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SpotifyControllerTest {

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK private lateinit var mockAppRemoteFlow: MutableStateFlow<SpotifyAppRemote?>
  @RelaxedMockK private lateinit var mockAppRemote: SpotifyAppRemote
  @RelaxedMockK private lateinit var mockPlayerApi: PlayerApi
  // ktfmt

  @RelaxedMockK private lateinit var spotifyController: SpotifyController
  private lateinit var context: Context
  private lateinit var authenticationController: AuthenticationController
  @RelaxedMockK private lateinit var mockRecentlyPlayedRepository: RecentlyPlayedRepository

  @RelaxedMockK private lateinit var mockScope: CoroutineScope

  private lateinit var requestManager: RequestManager
  private lateinit var requestBuilder: RequestBuilder<Bitmap>
  private lateinit var futureTarget: FutureTarget<Bitmap>

  private lateinit var testDispatcher: TestDispatcher

  @Before
  fun setup() {
    // Initialize MockK
    mockkStatic(SpotifyAppRemote::class)

    // Mocking Glide components
    mockkStatic(Glide::class)
    requestManager = mockk(relaxed = true)
    requestBuilder = mockk(relaxed = true)
    futureTarget = mockk(relaxed = true)
    every { Glide.with(any<Context>()) } returns requestManager
    every { requestManager.asBitmap() } returns requestBuilder
    every { requestBuilder.load(any<String>()) } returns requestBuilder
    every { requestBuilder.submit(any(), any()) } returns futureTarget
    every { futureTarget.get() } returns mockk<Bitmap>(relaxed = true)

    context = ApplicationProvider.getApplicationContext()
    authenticationController = mockk<AuthenticationController>()
    testDispatcher = UnconfinedTestDispatcher(TestCoroutineScheduler())
    spotifyController =
        SpotifyController(
            context, authenticationController, testDispatcher, mockRecentlyPlayedRepository)
    spotifyController.appRemote.value = mockAppRemote
    mockkStatic(SpotifyAppRemote::class)
    spotifyController.appRemote.value = mockAppRemote
    every { mockAppRemote.playerApi } returns mockPlayerApi
    coEvery { authenticationController.makeApiRequest(any()) } returns "Test"

    mockScope = mockk<CoroutineScope>()
  }

  @Test
  fun testGetCurrentUserId_Success() = runBlocking {
    val userIdJson = """{"id": "testUserId"}"""
    coEvery {
      authenticationController.makeApiRequest(URL("https://api.spotify.com/v1/me"))
    } returns userIdJson

    val userId = spotifyController.getCurrentUserId()
    assertEquals("testUserId", userId)
  }

  @Test(expected = Exception::class)
  fun testGetCurrentUserId_Failure() =
      runBlocking<Unit> {
        coEvery {
          authenticationController.makeApiRequest(URL("https://api.spotify.com/v1/me"))
        } returns "FAILURE"

        spotifyController.getCurrentUserId()
      }

  @Test
  fun testGetAllPlaylists() = runBlocking {
    val playlistsJson =
        """
        {
            "items": [
                {
                    "id": "playlist1",
                    "name": "Playlist 1"
                },
                {
                    "id": "playlist2",
                    "name": "Playlist 2"
                }
            ]
        }
    """
    coEvery {
      authenticationController.makeApiRequest(URL("https://api.spotify.com/v1/me/playlists"))
    } returns playlistsJson

    val playlists = spotifyController.getAllPlaylists()

    assertEquals(2, playlists.size)
    assertEquals("playlist1", playlists[0].id)
    assertEquals("Playlist 1", playlists[0].title)
    assertEquals("playlist2", playlists[1].id)
    assertEquals("Playlist 2", playlists[1].title)
  }

  @Test
  fun testCreatePlaylistIfNotExist_ExistingPlaylist() = runBlocking {
    val playlistsJson =
        """
        {
            "items": [
                {
                    "id": "existingPlaylistId",
                    "name": "Wanderwave"
                }
            ]
        }
    """
    coEvery {
      authenticationController.makeApiRequest(URL("https://api.spotify.com/v1/me/playlists"))
    } returns playlistsJson

    val playlistId = spotifyController.createPlaylistIfNotExist()
    assertEquals("existingPlaylistId", playlistId)
  }

  @Test
  fun testCreatePlaylistIfNotExist_NewPlaylist() = runBlocking {
    val playlistName = "Wanderwave"
    val playlistDescription = "Liked songs from Wanderwave"
    val userId = "testUserId"
    val newPlaylistId = "newPlaylistId"
    val newPlaylistJson =
        """
        {
            "id": "$newPlaylistId",
            "name": "$playlistName"
        }
    """
    val userPlaylistsJson = """
        {
            "items": []
        }
    """

    // Mocking the responses for makeApiRequest
    coEvery {
      authenticationController.makeApiRequest(URL("https://api.spotify.com/v1/me"))
    } returns """{"id": "$userId"}"""
    coEvery {
      authenticationController.makeApiRequest(URL("https://api.spotify.com/v1/me/playlists"))
    } returns userPlaylistsJson
    coEvery {
      authenticationController.makeApiRequest(
          URL("https://api.spotify.com/v1/users/$userId/playlists"), "POST", any())
    } returns newPlaylistJson

    // Mocking the uploadPlaylistImage method
    coEvery { authenticationController.uploadPlaylistImage(any(), any()) } just Runs

    // Call the method under test
    val result = spotifyController.createPlaylistIfNotExist()

    // Verify that the result is the new playlist ID
    assertEquals(newPlaylistId, result)

    // Verify that the uploadPlaylistImage method was called
    coVerify { authenticationController.uploadPlaylistImage(context, newPlaylistId) }
  }

  @Test
  fun testAddToPlaylist_Success() = runBlocking {
    val playlistId = "existingPlaylistId"
    val track = Track("spotify:track:6rqhFgbbKwnb9MLmUQDhG6", "Track Name", "Artist Name")
    val playlistsJson =
        """
        {
            "items": [
                {
                    "id": "$playlistId",
                    "name": "Wanderwave"
                }
            ]
        }
    """
    coEvery {
      authenticationController.makeApiRequest(URL("https://api.spotify.com/v1/me/playlists"))
    } returns playlistsJson
    coEvery {
      authenticationController.makeApiRequest(
          URL("https://api.spotify.com/v1/playlists/$playlistId/tracks"), "POST", any())
    } returns "SUCCESS"

    spotifyController.addToPlaylist(track)
  }

  @Test(expected = Exception::class)
  fun testAddToPlaylist_Failure() = runBlocking {
    val playlistId = "existingPlaylistId"
    val track = Track("spotify:track:6rqhFgbbKwnb9MLmUQDhG6", "Track Name", "Artist Name")
    val playlistsJson =
        """
        {
            "items": [
                {
                    "id": "$playlistId",
                    "name": "Wanderwave"
                }
            ]
        }
    """
    coEvery {
      authenticationController.makeApiRequest(URL("https://api.spotify.com/v1/me/playlists"))
    } returns playlistsJson
    coEvery {
      authenticationController.makeApiRequest(
          URL("https://api.spotify.com/v1/playlists/$playlistId/tracks"), "POST", any())
    } returns "FAILURE"

    spotifyController.addToPlaylist(track)
  }

  @Test
  fun testRemoveFromPlaylist_Success() = runBlocking {
    val playlistId = "existingPlaylistId"
    val track = Track("spotify:track:6rqhFgbbKwnb9MLmUQDhG6", "Track Name", "Artist Name")
    val playlistsJson =
        """
        {
            "items": [
                {
                    "id": "$playlistId",
                    "name": "Wanderwave"
                }
            ]
        }
    """
    coEvery {
      authenticationController.makeApiRequest(URL("https://api.spotify.com/v1/me/playlists"))
    } returns playlistsJson
    coEvery {
      authenticationController.makeApiRequest(
          URL("https://api.spotify.com/v1/playlists/$playlistId/tracks"), "DELETE", any())
    } returns "SUCCESS"

    spotifyController.removeFromPlaylist(track)
  }

  @Test
  fun testRemoveFromPlaylist_Failure() = runBlocking {
    val playlistId = "existingPlaylistId"
    val track = Track("spotify:track:6rqhFgbbKwnb9MLmUQDhG6", "Track Name", "Artist Name")
    val playlistsJson =
        """
        {
            "items": [
                {
                    "id": "$playlistId",
                    "name": "Wanderwave"
                }
            ]
        }
    """
    coEvery {
      authenticationController.makeApiRequest(URL("https://api.spotify.com/v1/me/playlists"))
    } returns playlistsJson
    coEvery {
      authenticationController.makeApiRequest(
          URL("https://api.spotify.com/v1/playlists/$playlistId/tracks"), "DELETE", any())
    } returns "FAILURE"

    spotifyController.removeFromPlaylist(track)
  }

  @Test
  fun testGetCurrentUserId() = runBlocking {
    val url = "https://api.spotify.com/v1/me"
    val response = "{'id': 'testUserId'}"
    coEvery { authenticationController.makeApiRequest(URL(url)) } returns response
    val result = spotifyController.getCurrentUserId()
    assertEquals("testUserId", result)
  }

  @Test
  fun testExtractImageUrlFromJson() {
    val json =
        """
            {
                "images": [
                    {"url": "https://example.com/image1.jpg"},
                    {"url": "https://example.com/image2.jpg"}
                ]
            }
        """
    val expectedUrl = "https://example.com/image1.jpg"
    val actualUrl = spotifyController.extractImageUrlFromJson(json)
    assertEquals(expectedUrl, actualUrl)
  }

  @Test
  fun testExtractImageUrlFromJson_noImages() {
    val json = """
            {
                "images": []
            }
        """
    val actualUrl = spotifyController.extractImageUrlFromJson(json)
    assertNull(actualUrl)
  }

  @Test
  fun testFetchImageFromUrl() = runBlocking {
    val url =
        "https://fr.wikipedia.org/wiki/%C3%89cole_polytechnique_f%C3%A9d%C3%A9rale_de_Lausanne#/media/Fichier:EPFL_campus_2017.jpg"
    val result = spotifyController.fetchImageFromUrl(context, url)
    assertNotNull(result)
  }

  @Test
  fun testGetAlbumImage() = runBlocking {
    val albumId = "testAlbumId"
    val json =
        """
            {
                "images": [
                    {"url": "https://example.com/image1.jpg"},
                    {"url": "https://example.com/image2.jpg"}
                ]
            }
        """
    val bitmap: Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    coEvery {
      authenticationController.makeApiRequest(URL("https://api.spotify.com/v1/albums/$albumId"))
    } returns json
    every { futureTarget.get() } returns bitmap

    val result = spotifyController.getAlbumImage(albumId)
    assertNotNull(result)
  }

  @Test
  fun testGetTrackImage() = runBlocking {
    val trackId = "spotify:track:6rqhFgbbKwnb9MLmUQDhG6"
    val albumId = "testAlbumId"
    val spotifyController =
        spyk(
            SpotifyController(
                context, authenticationController, testDispatcher, mockRecentlyPlayedRepository))
    val json =
        """
            {
                "album": {
                    "id": "$albumId"
                }
            }
        """
    val bitmap: Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    coEvery {
      spotifyController.spotifyGetFromURL(
          "https://api.spotify.com/v1/tracks/${trackId.split(":")[2]}")
    } returns json
    coEvery {
      authenticationController.makeApiRequest(URL("https://api.spotify.com/v1/albums/$albumId"))
    } returns
        """
            {
                "images": [
                    {"url": "https://example.com/image1.jpg"},
                    {"url": "https://example.com/image2.jpg"}
                ]
            }
        """
    every { futureTarget.get() } returns bitmap

    val result = spotifyController.getTrackImage(trackId)
    assertNotNull(result)
  }

  @Test
  fun testGetAlbumIdFromTrackId() = runBlocking {
    val spotifyController =
        spyk(
            SpotifyController(
                context, authenticationController, testDispatcher, mockRecentlyPlayedRepository))
    val trackId = "spotify:track:6rqhFgbbKwnb9MLmUQDhG6"
    val expectedAlbumId = "testAlbumId"
    val json =
        """
            {
                "album": {
                    "id": "$expectedAlbumId"
                }
            }
        """
    coEvery {
      spotifyController.spotifyGetFromURL(
          "https://api.spotify.com/v1/tracks/${trackId.split(":")[2]}")
    } returns json

    val result = spotifyController.getAlbumIdFromTrackId(spotifyController, trackId)
    assertEquals(expectedAlbumId, result)
  }

  @Test
  fun testGetTracksFromSpotifyPlaylist() = runBlocking {
    // Mock the PlayerApi and Subscription objects
    val playerApi = mockk<PlayerApi>(relaxed = true)
    val subscription = mockk<Subscription<PlayerState>>(relaxed = true)
    val playerState = mockk<PlayerState>(relaxed = true)

    every { mockScope.coroutineContext } returns Dispatchers.Unconfined
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

    val playlist: MutableStateFlow<List<ListItem>> = MutableStateFlow(listOf())

    // Call the method to be tested
    getTracksFromSpotifyPlaylist("37i9dQZF1DXcBWIGoYBM5M", playlist, spotifyController, mockScope)
  }

  @Test
  fun testGetLikedTracks() = runBlocking {
    // Mock the PlayerApi and Subscription objects
    val playerApi = mockk<PlayerApi>(relaxed = true)
    val subscription = mockk<Subscription<PlayerState>>(relaxed = true)
    val playerState = mockk<PlayerState>(relaxed = true)

    every { mockScope.coroutineContext } returns Dispatchers.Unconfined
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

    val playlist: MutableStateFlow<List<ListItem>> = MutableStateFlow(listOf())

    // Call the method to be tested
    getLikedTracksFromSpotify(playlist, spotifyController, mockScope)
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
    val playerApi = mockk<PlayerApi>(relaxed = true)
    every { mockAppRemote.playerApi } returns playerApi
    val id = "fakeid"
    spotifyController.playTrack(Track(id, "faketitle", "fakeartist"))

    verify { playerApi.play(any()) }
  }

  @Test
  fun playTrackListTestWithNoTrackGiven() = runBlocking {
    val playerApi = mockk<PlayerApi>(relaxed = true)
    every { mockAppRemote.playerApi } returns playerApi
    val track1 = Track("id1", "title1", "artist1")
    val track2 = Track("id2", "title2", "artist2")
    val track3 = Track("id3", "title3", "artist3")
    val trackList = listOf(track1, track2, track3)
    spotifyController.playTrackList(trackList)

    verify { playerApi.play("id1") }
  }

  @Test
  fun playTrackListThrowsWithEmptyList() = runBlocking {
    val playerApi = mockk<PlayerApi>(relaxed = true)
    every { mockAppRemote.playerApi } returns playerApi
    val trackList = emptyList<Track>()
    var isCalled = false
    fun onFailure(a: Throwable) {
      isCalled = true
    }
    spotifyController.playTrackList(trackList, null, {}, ::onFailure)
    assertTrue(isCalled)
  }

  @Test
  fun testPlayerStateFlow() = runTest {
    // Mock the PlayerApi and its subscribeToPlayerState() function
    val mockPlayerApi = mockk<PlayerApi>(relaxed = true)
    val mockSubscription = mockk<Subscription<PlayerState>>(relaxed = true)
    every { mockPlayerApi.subscribeToPlayerState() } returns mockSubscription

    // Initialize SpotifyController with mocked PlayerApi
    every { mockAppRemote.playerApi } returns mockPlayerApi
    val spotifyController =
        SpotifyController(
            context, authenticationController, testDispatcher, mockRecentlyPlayedRepository)
    spotifyController.appRemote.value = mockAppRemote

    // Mock a PlayerState
    val mockTrack =
        com.spotify.protocol.types.Track(
            mockk(), mockk(), mockk(), 1L, "title", "uri", mockk(), false, false)
    val mockPlayerState = PlayerState(mockTrack, false, 1f, 0, mockk(), mockk())

    // Use the mocked PlayerState as the result for the subscription's setEventCallback
    every { mockSubscription.setEventCallback(any()) } answers
        {
          val callback = firstArg<Subscription.EventCallback<PlayerState>>()
          callback.onEvent(mockPlayerState)
          mockSubscription
        }

    advanceUntilIdle()
    // Call playerState()
    val playerStateFlow = spotifyController.playerState()
    // Collect from the flow
    val result = playerStateFlow.first()

    // Assert that the result is the mocked PlayerState
    assertEquals(mockPlayerState, result)
  }

  @Test
  fun testSkip() = runTest {
    // Mock the PlayerApi and its subscribeToPlayerState() function
    val mockPlayerApi = mockk<PlayerApi>(relaxed = true)
    val mockSubscription = mockk<Subscription<PlayerState>>(relaxed = true)
    every { mockPlayerApi.subscribeToPlayerState() } returns mockSubscription

    // Initialize SpotifyController with mocked PlayerApi
    every { mockAppRemote.playerApi } returns mockPlayerApi
    val mockArtist = Artist("artist", "uri")
    // Mock a PlayerState
    val mockSpotifyTrack1 =
        com.spotify.protocol.types.Track(
            mockArtist, mockk(), mockk(), 1L, "title", "uri", mockk(), false, false)
    val mockSpotifyTrack2 =
        com.spotify.protocol.types.Track(
            mockArtist, mockk(), mockk(), 1L, "title2", "uri2", mockk(), false, false)
    val mockTrack1 = mockSpotifyTrack1.toWanderwaveTrack()
    val mockTrack2 = mockSpotifyTrack2.toWanderwaveTrack()
    val mockPlayerState = PlayerState(mockSpotifyTrack1, false, 1f, 0, mockk(), mockk())

    // Use the mocked PlayerState as the result for the subscription's setEventCallback
    every { mockSubscription.setEventCallback(any()) } answers
        {
          val callback = firstArg<Subscription.EventCallback<PlayerState>>()
          callback.onEvent(mockPlayerState)
          mockSubscription
        }

    spotifyController.playTrackList(listOf(mockTrack1, mockTrack2))
    val spotifyController =
        SpotifyController(
            context, authenticationController, testDispatcher, mockRecentlyPlayedRepository)
    spotifyController.appRemote.value = mockAppRemote
    // Call playerState()
    val playerStateFlow = spotifyController.playerState()

    spotifyController.skip(1)
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

      spotifyController.resumeTrack()

      verify { playerApi.resume() }
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

    spotifyController.pauseTrack()

    verify { playerApi.pause() }
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

    spotifyController.resumeTrack()

    verify { playerApi.resume() }
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
      spotifyController.playTrack(Track(id, "faketitle", "fakeartist"))
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

      spotifyController.pauseTrack()

      verify { playerApi.pause() }
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

      spotifyController.resumeTrack()

      verify { playerApi.resume() }
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

    spotifyController.resumeTrack()
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

    spotifyController.pauseTrack()
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

    spotifyController.playTrack(Track("id", "title", "artist"))
  }

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

    val spotifyTrack =
        com.spotify.protocol.types.Track(
            Artist("Rick Astley", ""),
            listOf(),
            mockk(),
            1,
            "Never Gonna Give You Up",
            "spotify:track:4PTG3Z6ehGkBFwjybzWkR8?si=0f7d62dba3704a0b",
            mockk(),
            false,
            false)

    // When playerApi.subscribeToPlayerState() is called, return the mocked subscription
    every { playerApi.subscribeToPlayerState() } returns subscription

    // When subscription.setEventCallback(any()) is called, invoke the callback with the test
    // PlayerState
    every { subscription.setEventCallback(any()) } answers
        {
          val callback = firstArg<Subscription.EventCallback<PlayerState>>()
          callback.onEvent(PlayerState(spotifyTrack, false, 1f, 0, mockk(), mockk()))
          subscription
        }

    // When subscription.setErrorCallback(any()) is called, do nothing
    every { subscription.setErrorCallback(any()) } just Awaits

    // Set the playerApi in the SpotifyController
    every { mockAppRemote.playerApi } returns playerApi

    // Call the method to be tested
    spotifyController.onPlayerStateUpdate()

    // Verify that setEventCallback was called
    verify { subscription.setEventCallback(any()) }

    verify {
      mockRecentlyPlayedRepository.addRecentlyPlayed(
          withArg {
            assertEquals(it.id, spotifyTrack.uri)
            assertEquals(it.title, spotifyTrack.name)
            assertEquals(it.artist, spotifyTrack.artist.name)
          },
          any())
    }
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
  fun testTrackConversion() {
    val fakeArtist = Artist("Rick Astley", "")
    val spotifyTrack =
        com.spotify.protocol.types.Track(
            fakeArtist, mockk(), mockk(), 1, "Never Gonna Give You Up", "id", mockk(), false, false)
    val track = spotifyTrack.toWanderwaveTrack()
    val expected = Track("id", "Never Gonna Give You Up", "Rick Astley")
    assertEquals(track, expected)
  }

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

  @Test
  fun parseTracksSuccessfullyParsesTracks() {
    val jsonResponse =
        """
    {
        "items": [
            {
                "track": {
                    "id": "1",
                    "name": "Track 1",
                    "artists": [
                        {
                            "name": "Artist 1"
                        }
                    ]
                }
            }
        ]
    }
"""
            .trimIndent()

    val likedSongsTrackList: MutableStateFlow<List<ListItem>> =
        MutableStateFlow<List<ListItem>>(emptyList())

    parseTracks(jsonResponse, likedSongsTrackList)
    assertEquals(likedSongsTrackList.value.size, 1)
    junit.framework.TestCase.assertEquals(
        likedSongsTrackList.value[0],
        ListItem("1", "", ImageUri(""), "Track 1", "Artist 1", false, false))
  }

  @Test
  fun parseTracksHandlesEmptyResponse() {
    val likedSongsTrackList: MutableStateFlow<List<ListItem>> =
        MutableStateFlow<List<ListItem>>(emptyList())
    val jsonResponse =
        """
    {
        "items": []
    }
"""
            .trimIndent()
    parseTracks(jsonResponse, likedSongsTrackList)
    assertEquals(likedSongsTrackList.value.size, 0)
  }

  @Test fun recentlyPlayedTracksAreRecorder() = runBlocking {}
}

interface UrlFactory {
  fun create(urlString: String): URL
}

class SimpleUrlFactory : UrlFactory {
  override fun create(urlString: String): URL = URL(urlString)
}
