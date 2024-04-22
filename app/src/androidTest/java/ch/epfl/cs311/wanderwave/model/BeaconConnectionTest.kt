package ch.epfl.cs311.wanderwave.model

import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.remote.BeaconConnection
import com.google.android.gms.tasks.OnSuccessListener
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
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
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
  private lateinit var collectionReference: CollectionReference

  lateinit var beacon: Beacon

  @Before
  fun setup() {
    // Create the mocks
    firestore = mockk()
    documentReference = mockk<DocumentReference>(relaxed = true)
    collectionReference = mockk<CollectionReference>(relaxed = true)

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
      // Mock the Task
      val mockTask = mockk<Task<DocumentSnapshot>>()
      val mockDocumentSnapshot = mockk<DocumentSnapshot>()

      val getTestBeacon =
          Beacon(
              id = "testBeacon", location = Location(1.0, 1.0, "Test Location"), tracks = listOf())

      every { mockDocumentSnapshot.getData() } returns getTestBeacon.toMap()
      every { mockDocumentSnapshot.exists() } returns true
      every { mockDocumentSnapshot.id } returns getTestBeacon.id
      every { mockDocumentSnapshot.get("location") } returns getTestBeacon.location.toMap()
      every { mockDocumentSnapshot.get("tracks") } returns getTestBeacon.tracks

      // Define behavior for the addOnSuccessListener method
      every { mockTask.addOnSuccessListener(any<OnSuccessListener<DocumentSnapshot>>()) } answers
          {
            val listener = arg<OnSuccessListener<DocumentSnapshot>>(0)

            // Define the behavior of the mock DocumentSnapshot here
            listener.onSuccess(mockDocumentSnapshot)
            mockTask
          }
      every { mockTask.addOnFailureListener(any()) } answers { mockTask }

      // Define behavior for the get() method on the DocumentReference to return the mock task
      every { documentReference.get() } returns mockTask

      // Call the function under test
      val retrievedBeacon = beaconConnection.getItem("testBeacon").first()

      // Verify that the get function is called on the document with the correct id
      coVerify { documentReference.get() }
      assertEquals(getTestBeacon, retrievedBeacon)
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
