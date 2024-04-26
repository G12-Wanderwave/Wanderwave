package ch.epfl.cs311.wanderwave.model

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import ch.epfl.cs311.wanderwave.di.ConnectionModule
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.remote.BeaconConnection
import ch.epfl.cs311.wanderwave.model.remote.TrackConnection
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Transaction
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.fail
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Before
import org.junit.Rule
import org.junit.Test

public class BeaconConnectionTest {

  @get:Rule val mockkRule = MockKRule(this)
  private lateinit var beaconConnection: BeaconConnection
  private lateinit var trackConnection: TrackConnection

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
    trackConnection = mockk<TrackConnection>(relaxed = true)

    // Mock data
    beacon =
        Beacon(
            id = "testBeacon",
            location = Location(1.0, 1.0, "Test Location"),
            tracks = listOf(Track("testTrack", "Test Title", "Test Artist")))

    // Define behavior for the mocks
    every { collectionReference.document(beacon.id) } returns documentReference
    every { firestore.collection(any()) } returns collectionReference

    // Pass the mock Firestore instance to your BeaconConnection
    beaconConnection = BeaconConnection(firestore, trackConnection)
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
  fun testGetItemFailure() {
    runBlocking {
      withTimeout(3000) {
        // Mock the Task
        val mockTask = mockk<Task<DocumentSnapshot>>()

        // Define behavior for the addOnSuccessListener method
        every { mockTask.addOnSuccessListener(any<OnSuccessListener<DocumentSnapshot>>()) } answers
            {
              mockTask
            }
        every { mockTask.addOnFailureListener(any()) } answers
            {
              val listener = arg<OnFailureListener>(0)
              listener.onFailure(Exception("Test Exception"))

              mockTask
            }

        // Define behavior for the get() method on the DocumentReference to return the mock task
        every { documentReference.get() } returns mockTask

        beaconConnection.getItem("testBeacon")

        // Verify that the get function is called on the document with the correct id
        coVerify { documentReference.get() }
      }
    }
  }

  @Test
  fun testFetchTrack() = runBlocking {
    // Mock the DocumentReference
    val mockDocumentReference = mockk<DocumentReference>()

    // Mock the DocumentSnapshot
    val mockDocumentSnapshot = mockk<DocumentSnapshot>()

    // Mock the Track
    val mockTrack = Track("testTrackId", "Test Title", "Test Artist")

    // Define behavior for the get() method on the DocumentReference to return the mock task
    coEvery { mockDocumentReference.get() } returns mockk {
      every { isComplete } returns true
      every { isSuccessful } returns true
      every { result } returns mockDocumentSnapshot
      every { getException() } returns null
      every { isCanceled } returns false
    }

    // Define behavior for the DocumentSnapshot
    every { mockDocumentSnapshot.exists() } returns true
    every { mockDocumentSnapshot.id } returns mockTrack.id
    every { mockDocumentSnapshot.getString("title") } returns mockTrack.title
    every { mockDocumentSnapshot.getString("artist") } returns mockTrack.artist

    var retrievedTrack: Track? = null

    // Call the function under test
    async {
      retrievedTrack = beaconConnection.fetchTrack(mockDocumentReference)
    }.await()

    // Verify that the get function is called on the document with the correct id
    coVerify { mockDocumentReference.get() }

    // Assert that the retrieved track is the same as the mock track
    assertEquals(mockTrack, retrievedTrack)
  }

  @Test
  fun testGetAllItems() = runBlocking {
    withTimeout(3000) {
      // Mock the Task
      val mockTask = mockk<Task<QuerySnapshot>>()
      val mockQuerySnapshot = mockk<QuerySnapshot>()
      val mockDocumentSnapshot = mockk<QueryDocumentSnapshot>()

      val getTestBeacon =
          Beacon(
              id = "testBeacon", location = Location(1.0, 1.0, "Test Location"), tracks = listOf())

      val getTestBeaconList = listOf(getTestBeacon, getTestBeacon)

      every { mockDocumentSnapshot.getData() } returns getTestBeacon.toMap()
      every { mockDocumentSnapshot.exists() } returns true
      every { mockDocumentSnapshot.id } returns getTestBeacon.id
      every { mockDocumentSnapshot.get("location") } returns getTestBeacon.location.toMap()
      every { mockDocumentSnapshot.get("tracks") } returns getTestBeacon.tracks

      every { mockQuerySnapshot.documents } returns
          listOf(mockDocumentSnapshot, mockDocumentSnapshot)
      every { mockQuerySnapshot.iterator() } returns
          mutableListOf(mockDocumentSnapshot, mockDocumentSnapshot).iterator()

      // Define behavior for the addOnSuccessListener method
      every { mockTask.addOnSuccessListener(any<OnSuccessListener<QuerySnapshot>>()) } answers
          {
            val listener = arg<OnSuccessListener<QuerySnapshot>>(0)

            // Define the behavior of the mock QuerySnapshot here
            listener.onSuccess(mockQuerySnapshot)
            mockTask
          }
      every { mockTask.addOnFailureListener(any()) } answers { mockTask }

      // Define behavior for the get() method on the CollectionReference to return the mock task
      every { collectionReference.get() } returns mockTask

      // Call the function under test
      val retrievedBeacons = beaconConnection.getAll().first()

      // Verify that the get function is called on the collection
      coVerify { collectionReference.get() }
      assertEquals(getTestBeaconList, retrievedBeacons)
    }
  }

  @Test
  fun testAddItemWithId() {
    // Call the function under test
    beaconConnection.addItemWithId(beacon)

    // Verify that the set function is called on the document with the correct id
    verify { collectionReference.document(beacon.id) }
  }

  @Test
  fun testAddTrackToBeacon() {
    // Mock data
    val track = Track("testTrackId", "Test Title", "Test Artist")
    val beacon = Beacon("testBeaconId", Location(1.0, 1.0, "Test Location"), listOf(track))

    // Mock the Task
    val mockTask = mockk<Task<Transaction>>()
    val mockTransaction = mockk<Transaction>()
    val mockDocumentSnapshot = mockk<DocumentSnapshot>()

    val getTestBeacon =
        Beacon(id = "testBeacon", location = Location(1.0, 1.0, "Test Location"), tracks = listOf())

    every { mockTransaction.get(any<DocumentReference>()) } returns mockDocumentSnapshot
    every { mockTransaction.update(any<DocumentReference>(), any<String>(), any()) } answers
        {
          mockTransaction
        }
    every { mockDocumentSnapshot.getData() } returns getTestBeacon.toMap()
    every { mockDocumentSnapshot.exists() } returns true
    every { mockDocumentSnapshot.id } returns getTestBeacon.id
    every { mockDocumentSnapshot.get("location") } returns getTestBeacon.location.toMap()
    every { mockDocumentSnapshot.get("tracks") } returns getTestBeacon.tracks

    // Define behavior for the addOnSuccessListener method
    every { mockTask.addOnSuccessListener(any<OnSuccessListener<Transaction>>()) } answers
        {
          val listener = arg<OnSuccessListener<Transaction>>(0)

          // Define the behavior of the mock QuerySnapshot here
          listener.onSuccess(mockTransaction)
          mockTask
        }
    every { mockTask.addOnFailureListener(any()) } answers { mockTask }

    coEvery { firestore.runTransaction<Transaction>(any()) } answers
        {
          val lambda = firstArg<Transaction.Function<Unit>>()
          lambda.apply(mockTransaction)

          mockk(relaxed = true)
        }

    // Call the function under test
    beaconConnection.addTrackToBeacon(beacon.id, track, {})

    verify { firestore.runTransaction<Transaction>(any()) }
    verify { mockTransaction.get(any<DocumentReference>()) }
    verify { mockTransaction.update(any<DocumentReference>(), "tracks", any()) }
  }

  @Test
  fun testAddNullTrackToBeacon() {
    // Mock data
    val track = Track("testTrackId", "Test Title", "Test Artist")
    val beacon = Beacon("testBeaconId", Location(1.0, 1.0, "Test Location"), listOf(track))

    // Mock the Task
    val mockTask = mockk<Task<Transaction>>()
    val mockTransaction = mockk<Transaction>()
    val mockDocumentSnapshot = mockk<DocumentSnapshot>()

    val getTestBeacon =
        Beacon(id = "testBeacon", location = Location(1.0, 1.0, "Test Location"), tracks = listOf())

    every { mockDocumentSnapshot.getData() } returns getTestBeacon.toMap()
    every { mockDocumentSnapshot.exists() } returns false

    every { mockTransaction.get(any<DocumentReference>()) } returns mockDocumentSnapshot

    // Define behavior for the addOnSuccessListener method
    every { mockTask.addOnSuccessListener(any<OnSuccessListener<Transaction>>()) } answers
        {
          val listener = arg<OnSuccessListener<Transaction>>(0)

          // Define the behavior of the mock QuerySnapshot here
          listener.onSuccess(mockTransaction)
          mockTask
        }
    every { mockTask.addOnFailureListener(any()) } answers { mockTask }

    coEvery { firestore.runTransaction<Transaction>(any()) } answers
        {
          val lambda = firstArg<Transaction.Function<Unit>>()
          lambda.apply(mockTransaction)

          mockk(relaxed = true)
        }

    // Call the function under test
    try {
      beaconConnection.addTrackToBeacon(beacon.id, track, {})
      fail("Should have thrown an exception")
    } catch (e: Exception) {
      // Verify that the exception is thrown
      verify { firestore.runTransaction<Transaction>(any()) }
    }
  }

  @Test
  fun provideBeaconRepository_returnsBeaconConnection() {
    // delete as soon as possible
    val context = ApplicationProvider.getApplicationContext<Context>()
    val beaconRepository = ConnectionModule.provideBeaconRepository(context)
    assertEquals(BeaconConnection::class.java, beaconRepository::class.java)
  }

  @Test
  fun testDeleteItem() {
    // Call the function under test
    beaconConnection.deleteItem(beacon)
    // Verify that the delete function is called on the document with the correct id
    verify { documentReference.delete() }
  }
}
