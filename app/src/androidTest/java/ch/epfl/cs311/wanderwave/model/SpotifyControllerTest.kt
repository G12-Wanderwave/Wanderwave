package ch.epfl.cs311.wanderwave.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import com.spotify.android.appremote.api.Connector.ConnectionListener
import com.spotify.android.appremote.api.PlayerApi
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.NotLoggedInException
import com.spotify.protocol.client.CallResult
import com.spotify.protocol.types.Empty
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
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
    assertTrue(result == true)
  }
}
