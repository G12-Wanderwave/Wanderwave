package ch.epfl.cs311.wanderwave.model

import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.remote.BeaconConnection
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.coVerify
import io.mockk.every
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Before
import org.junit.Rule
import org.junit.Test

public class BeaconConnectionTest {

  @get:Rule val mockkRule = MockKRule(this)
  private lateinit var beaconConnection: BeaconConnection

  private lateinit var firestore: FirebaseFirestore
  private lateinit var documentReference: DocumentReference
  private lateinit var documentSnapshotTask: Task<DocumentSnapshot>
  private lateinit var documentSnapshot: DocumentSnapshot
  private lateinit var collectionReference: CollectionReference

  lateinit var beacon: Beacon

  @Before
  fun setup() {
    // Create the mocks
    firestore = mockk()
    documentReference = mockk<DocumentReference>(relaxed = true)
    collectionReference = mockk<CollectionReference>(relaxed = true)
    documentSnapshotTask = mockk<Task<DocumentSnapshot>>(relaxed = true)
    documentSnapshot = mockk<DocumentSnapshot>(relaxed = true)

    // Mock data
    beacon =
        Beacon(
            id = "testBeacon",
            location = Location(1.0, 1.0, "Test Location"),
            tracks = listOf(Track("testTrack", "Test Title", "Test Artist")))

    // Define behavior for the mocks
    every { firestore.collection(any()) } returns mockk(relaxed = true)
    every { collectionReference.document(beacon.id) } returns documentReference
    every { firestore.collection(any()) } returns collectionReference

    // Pass the mock Firestore instance to your BeaconConnection
    beaconConnection = BeaconConnection(firestore)
  }

  @Test
  fun testAddItem() {
    // Call the function under test
    beaconConnection.addItem(beacon)

    // Verify that either the set function is called
    verify { collectionReference.add(any()) }
  }

  @Test
  fun testUpdateItem() = runBlocking {
    // Call the function under test
    beaconConnection.updateItem(beacon)

    // Verify that the set function is called on the document with the correct id
    verify { documentReference.set(any()) }
  }

  @Test
  fun testGetItem() = runBlocking {
    withTimeout(3000) {

      every { documentReference.get() } returns documentSnapshotTask
      // Mock the DocumentSnapshotTask
      documentSnapshotTask = mockk<Task<DocumentSnapshot>>(relaxed = true) {
        every { isSuccessful } returns true
        every { result } returns documentSnapshot


      }
      every { documentSnapshot.data } returns mapOf(
        "id" to "testBeacon",
        "location" to mapOf(
          "latitude" to 1.0,
          "longitude" to 1.0,
          "name" to "Test Location"
        ),
        "tracks" to listOf<DocumentReference>()
      )
      every { documentReference.get() } returns documentSnapshotTask

      // Call the function under test
      val retrievedBeacon = beaconConnection.getItem("testBeacon").first()

      // Verify that the get function is called on the document with the correct id
      coVerify { documentReference.get() }
    }
  }

  @Test
  fun testDeleteItem() {
    // Call the function under test
    beaconConnection.deleteItem(beacon)

    // Verify that the delete function is called on the document with the correct id
    verify { documentReference.delete() }
  }
}
