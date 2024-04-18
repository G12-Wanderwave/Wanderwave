package ch.epfl.cs311.wanderwave.ui.screens

import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.ui.components.Beacon.BeaconActions
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class BeaconActionsTest {

  private lateinit var beaconActions: BeaconActions
  private val db = mockk<FirebaseFirestore>()
  private val documentSnapshot = mockk<DocumentSnapshot>(relaxed = true)
  private val documentReference = mockk<DocumentReference>(relaxed = true)

  @Before
  fun setup() {
    MockKAnnotations.init(this)
    mockkStatic(FirebaseFirestore::class)
    every { FirebaseFirestore.getInstance() } returns db
    every { db.collection(any()).document(any()) } returns documentReference

    beaconActions = BeaconActions()
  }

  @Test
  fun testDocumentToItem() = runBlockingTest {
    every { documentSnapshot.exists() } returns true
    every { documentSnapshot.id } returns "beaconId"
    every { documentSnapshot.get("location") } returns
        mapOf("latitude" to 46.0, "longitude" to 7.0, "name" to "Test Location")
    every { documentSnapshot.get("tracks") } returns
        listOf(mapOf("title" to "Track 1", "artist" to "Artist 1", "id" to "trackId1"))

    val beacon = beaconActions.documentToItem(documentSnapshot)

    assertNotNull(beacon)
    assertEquals("Test Location", beacon?.location?.name)
    assertEquals(1, beacon?.tracks?.size)
  }

  @Test
  fun testItemToMap() = runBlockingTest {
    val location = Location(46.0, 7.0, "Test Location")
    val track = Track("trackId1", "Track 1", "Artist 1")
    val beacon = Beacon("beaconId", location, listOf(track))

    val map = beaconActions.itemToMap(beacon)

    assertNotNull(map)
    assertTrue(map.containsKey("id"))
    assertTrue(map.containsKey("location"))
    assertTrue(map.containsKey("tracks"))
  }
}
