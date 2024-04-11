package ch.epfl.cs311.wanderwave.model

import android.util.Log
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.remote.TrackConnection
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
import org.junit.Before
import org.junit.Test

class TrackConnectionTest {
  // backend testing of the track connection
  private lateinit var trackConnection: TrackConnection

  @Before
  fun setup() {
    trackConnection = TrackConnection()
  }

  @Test
  fun testAddAndGetItem() = runBlocking {
    withTimeout(20000) {
      val track = Track("testTrack", "Test Title", "Test Artist")

      trackConnection.addItemWithId(track)

      Log.d("Firestore", "Added item")
      val retrievedTrack = trackConnection.getItem("testTrack").first()

      assert(track == retrievedTrack)
    }
  }

  @Test
  fun testGetNonexistentItem() = runBlocking {
    withTimeout(20000) {
      var valueEmitted = false

      measureTimeMillis {
        withTimeoutOrNull(2000) {
          trackConnection.getItem("nonexistentTrack").collect { valueEmitted = true }
        }
      }

      assert(valueEmitted.not()) { "Flow emitted unexpected value" }
    }
  }

  @Test
  fun testAddItemTwice() = runBlocking {
    withTimeout(20000) {
      val track = Track("testTrack", "Test Title", "Test Artist")

      trackConnection.addItemWithId(track)
      trackConnection.addItemWithId(track)

      val retrievedTrack = trackConnection.getItem("testTrack").first()

      assert(track == retrievedTrack)
    }
  }

  @Test
  fun AddDeleteAndGetItem() = runBlocking {
    withTimeout(20000) {
      val track = Track("testTrack1", "Test Title", "Test Artist")

      trackConnection.addItemWithId(track)
      trackConnection.deleteItem("testTrack1")

      var valueEmitted = false

      measureTimeMillis {
        withTimeoutOrNull(2000) {
          trackConnection.getItem("testTrack1").collect { valueEmitted = true }
        }
      }

      assert(valueEmitted.not()) { "Flow emitted unexpected value" }
    }
  }

  @After
  fun cleanupTestData() = runBlocking {
    trackConnection.deleteItem("testTrack")
    trackConnection.deleteItem("testTrack1")
    trackConnection.deleteItem("testTrack2")
    trackConnection.deleteItem("nonexistentTrack")
  }
}
