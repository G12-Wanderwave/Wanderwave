package ch.epfl.cs311.wanderwave.model

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import ch.epfl.cs311.wanderwave.di.RepositoryModule
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.data.ProfileTrackAssociation
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.remote.BeaconConnection
import ch.epfl.cs311.wanderwave.model.remote.ProfileConnection
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.withTimeout
import org.junit.Before
import org.junit.Rule
import org.junit.Test

public class BeaconConnectionTest {

  @get:Rule val mockkRule = MockKRule(this)
  private lateinit var beaconConnection: BeaconConnection

  private lateinit var trackConnection: TrackConnection
  private lateinit var profileConnection: ProfileConnection // Add a mock for ProfileConnection

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
    profileConnection = mockk<ProfileConnection>(relaxed = true)

    // Mock data
    beacon =
        Beacon(
            id = "testBeacon",
            location = Location(1.0, 1.0, "Test Location"),
            profileAndTrack =
                listOf(
                    ProfileTrackAssociation(
                        Profile(
                            "Sample First Name",
                            "Sample last name",
                            "Sample desc",
                            0,
                            false,
                            null,
                            "Sample Profile ID",
                            "Sample Track ID"),
                        Track("Sample Track ID", "Sample Track Title", "Sample Artist Name"))))

    // Define behavior for the mocks
    every { collectionReference.document(beacon.id) } returns documentReference
    every { firestore.collection(any()) } returns collectionReference

    // Pass the mock Firestore instance to your BeaconConnection
    val testDispatcher = UnconfinedTestDispatcher(TestCoroutineScheduler())
    beaconConnection =
        BeaconConnection(firestore, trackConnection, profileConnection, testDispatcher)
  }

  @Test
  fun testAddItem() = runBlocking {
    // Mock data
    val beacon =
        Beacon(
            id = "testBeacon",
            location = Location(1.0, 1.0, "Test Location"),
            profileAndTrack =
                listOf(
                    ProfileTrackAssociation(
                        Profile(
                            "Sample First Name",
                            "Sample last name",
                            "Sample desc",
                            0,
                            false,
                            null,
                            "Sample Profile ID",
                            "Sample Track ID"),
                        Track("Sample Track ID", "Sample Track Title", "Sample Artist Name"))))

    // Call the function under test
    beaconConnection.addItem(beacon)

    // No verification is needed for interactions with the real object
  }

  @Test
  fun testUpdateItem() = runBlocking {
    // Mock data
    val beacon =
        Beacon(
            id = "testBeacon",
            location = Location(1.0, 1.0, "Test Location"),
            profileAndTrack =
                listOf(
                    ProfileTrackAssociation(
                        Profile(
                            "Sample First Name",
                            "Sample last name",
                            "Sample desc",
                            0,
                            false,
                            null,
                            "Sample Profile ID",
                            "Sample Track ID"),
                        Track("Sample Track ID", "Sample Track Title", "Sample Artist Name"))))

    // Call the function under test
    beaconConnection.updateItem(beacon)

    // Verify that the set function is called on the document with the correct id
    verify { documentReference.set(any()) }

    // No verification is needed for interactions with the real object
  }

  @Test
  fun testGetItem() = runBlocking {
    withTimeout(3000) {
      // Mock the Task
      val mockTask = mockk<Task<DocumentSnapshot>>()
      val mockDocumentSnapshot = mockk<DocumentSnapshot>()

      val getTestBeacon =
          Beacon(
              id = "testBeacon",
              location = Location(1.0, 1.0, "Test Location"),
              profileAndTrack = listOf())

      val mapOfTestBeacon =
          hashMapOf(
              "id" to getTestBeacon.id,
              "location" to getTestBeacon.location.toMap(),
              "tracks" to getTestBeacon.profileAndTrack.map { it.toMap() })

      every { mockDocumentSnapshot.getData() } returns mapOfTestBeacon
      every { mockDocumentSnapshot.exists() } returns true
      every { mockDocumentSnapshot.id } returns getTestBeacon.id
      every { mockDocumentSnapshot.get("location") } returns getTestBeacon.location.toMap()
      every { mockDocumentSnapshot.get("tracks") } returns
          getTestBeacon.profileAndTrack.map { it.toMap() }

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
  fun testGetItemTrackObjectIsNotList() = runBlocking {
    withTimeout(3000) {
      // Mock the Task
      val mockTask = mockk<Task<DocumentSnapshot>>()
      val mockDocumentSnapshot = mockk<DocumentSnapshot>()

      val getTestBeacon =
          Beacon(
              id = "testBeacon",
              location = Location(1.0, 1.0, "Test Location"),
              profileAndTrack = listOf())

      val mapOfTestBeacon =
          hashMapOf(
              "id" to getTestBeacon.id,
              "location" to getTestBeacon.location.toMap(),
              "tracks" to getTestBeacon.profileAndTrack.map { it.toMap() })
      every { mockDocumentSnapshot.getData() } returns mapOfTestBeacon
      every { mockDocumentSnapshot.exists() } returns true
      every { mockDocumentSnapshot.id } returns getTestBeacon.id
      every { mockDocumentSnapshot.get("location") } returns getTestBeacon.location.toMap()

      // not a list of maps, doesn't pass the if
      every { mockDocumentSnapshot.get("tracks") } returns listOf("String1", "String2")

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
      beaconConnection.getItem("testBeacon").first()

      // Verify that the get function is called on the document with the correct id
      coVerify { documentReference.get() }

      // verify that fetchTrack is not called
      // I don't know how to do this didn't work : coVerify(exactly = 0) {
      // beaconConnection.fetchTrack(any<DocumentReference>()) }
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
  fun testGetAllItems() = runBlocking {
    withTimeout(3000) {
      // Mock the Task
      val mockTask = mockk<Task<QuerySnapshot>>()
      val mockQuerySnapshot = mockk<QuerySnapshot>()
      val mockDocumentSnapshot = mockk<QueryDocumentSnapshot>()

      val getTestBeacon =
          Beacon(
              id = "testBeacon",
              location = Location(1.0, 1.0, "Test Location"),
              profileAndTrack = listOf())

      val mapOfTestBeacon =
          hashMapOf(
              "id" to getTestBeacon.id,
              "location" to getTestBeacon.location.toMap(),
              "tracks" to getTestBeacon.profileAndTrack.map { it.toMap() })

      val getTestBeaconList = listOf(getTestBeacon, getTestBeacon)

      every { mockDocumentSnapshot.getData() } returns mapOfTestBeacon
      every { mockDocumentSnapshot.exists() } returns true
      every { mockDocumentSnapshot.id } returns getTestBeacon.id
      every { mockDocumentSnapshot.get("location") } returns getTestBeacon.location.toMap()
      every { mockDocumentSnapshot.get("tracks") } returns
          getTestBeacon.profileAndTrack.map { it.toMap() }

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
    val profile =
        Profile(
            "Sample First Name",
            "Sample last name",
            "Sample desc",
            0,
            false,
            null,
            "My Firebase UID",
            track.id)
    val beacon =
        Beacon(
            "testBeaconId",
            Location(1.0, 1.0, "Test Location"),
            listOf(ProfileTrackAssociation(profile, track)))

    // Mock the Task
    val mockTask = mockk<Task<Transaction>>()
    val mockTransaction = mockk<Transaction>()
    val mockDocumentSnapshot = mockk<DocumentSnapshot>()

    val getTestBeacon =
        Beacon(
            id = "testBeacon",
            location = Location(1.0, 1.0, "Test Location"),
            profileAndTrack = listOf(ProfileTrackAssociation(profile, track)))

    val mapOfTestBeacon =
        hashMapOf(
            "id" to getTestBeacon.id,
            "location" to getTestBeacon.location.toMap(),
            "tracks" to getTestBeacon.profileAndTrack.map { it.toMap() })

    every { mockTransaction.get(any<DocumentReference>()) } returns mockDocumentSnapshot
    every { mockTransaction.update(any<DocumentReference>(), any<String>(), any()) } answers
        {
          mockTransaction
        }
    every { mockDocumentSnapshot.getData() } returns mapOfTestBeacon
    every { mockDocumentSnapshot.exists() } returns true
    every { mockDocumentSnapshot.id } returns getTestBeacon.id
    every { mockDocumentSnapshot.get("location") } returns getTestBeacon.location.toMap()
    every { mockDocumentSnapshot.get("tracks") } returns
        getTestBeacon.profileAndTrack.map { it.toMap() }

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
    val profile =
        Profile(
            "Sample First Name",
            "Sample last name",
            "Sample desc",
            0,
            false,
            null,
            "My Firebase UID",
            track.id)
    val beacon =
        Beacon(
            "testBeaconId",
            Location(1.0, 1.0, "Test Location"),
            listOf(ProfileTrackAssociation(profile, track)))

    val mapOfTestBeacon =
        hashMapOf(
            "id" to beacon.id,
            "location" to beacon.location.toMap(),
            "tracks" to beacon.profileAndTrack.map { it.toMap() })

    // Mock the Task
    val mockTask = mockk<Task<Transaction>>()
    val mockTransaction = mockk<Transaction>()
    val mockDocumentSnapshot = mockk<DocumentSnapshot>()

    val getTestBeacon =
        Beacon(
            id = "testBeacon",
            location = Location(1.0, 1.0, "Test Location"),
            profileAndTrack = listOf(ProfileTrackAssociation(profile, track)))

    every { mockDocumentSnapshot.getData() } returns mapOfTestBeacon
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
    val trackRepository = mockk<TrackConnection>(relaxed = true)
    val profileRepository = mockk<ProfileConnection>(relaxed = true)
    val beaconRepository =
        RepositoryModule.provideBeaconRepository(context, trackRepository, profileRepository)
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
