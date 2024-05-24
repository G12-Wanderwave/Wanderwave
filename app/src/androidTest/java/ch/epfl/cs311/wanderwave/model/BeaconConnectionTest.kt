package ch.epfl.cs311.wanderwave.model

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import ch.epfl.cs311.wanderwave.di.RepositoryModule
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.data.ProfileTrackAssociation
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.localDb.AppDatabase
import ch.epfl.cs311.wanderwave.model.remote.BeaconConnection
import ch.epfl.cs311.wanderwave.model.remote.ProfileConnection
import ch.epfl.cs311.wanderwave.model.remote.TrackConnection
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Before
import org.junit.Rule
import org.junit.Test

public class BeaconConnectionTest {

  @get:Rule val mockkRule = MockKRule(this)
  private lateinit var beaconConnection: BeaconConnection

  private lateinit var trackConnection: TrackConnection
  private lateinit var profileConnection: ProfileConnection // Add a mock for ProfileConnection
  private lateinit var appDatabase: AppDatabase
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
    appDatabase = mockk<AppDatabase>(relaxed = true)

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
        BeaconConnection(firestore, testDispatcher, trackConnection, profileConnection, appDatabase)
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
      // Mock the Test
      val mockDocumentSnapshot = mockk<DocumentSnapshot>()

      val getTestBeacon =
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
                          Track("Sample Track ID", "Sample Track Title", "Sample Artist Name")),
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

      every { mockDocumentSnapshot.getData() } returns getTestBeacon.toMap(firestore)
      every { mockDocumentSnapshot.exists() } returns true
      every { mockDocumentSnapshot.id } returns getTestBeacon.id
      every { mockDocumentSnapshot.get("location") } returns getTestBeacon.location.toMap()
      // get track needs to return a List<Map<String, DocumentReference>>
      every { mockDocumentSnapshot.get("tracks") } returns
          getTestBeacon.profileAndTrack.map({
            hashMapOf(
                "creator" to firestore.collection("users").document(it.profile?.firebaseUid ?: ""),
                "track" to firestore.collection("tracks").document(it.track.id))
          })
      every { mockDocumentSnapshot.getLong(any()) } returns 0

      // Define behavior for the addOnSuccessListener method
      every { documentReference.addSnapshotListener(any()) } answers
          {
            val listener = arg<EventListener<DocumentSnapshot>>(0)

            // Define the behavior of the mock DocumentSnapshot here
            listener.onEvent(mockDocumentSnapshot, null)

            mockk(relaxed = true)
          }

      every { trackConnection.fetchProfileAndTrack(any()) } returns
          flowOf(
              Result.success(
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
      val retrievedBeacon = beaconConnection.getItem("testBeacon").first()

      // Verify that the get function is called on the document with the correct id
      coVerify { documentReference.addSnapshotListener(any()) }
      assertEquals(Result.success(getTestBeacon), retrievedBeacon)
    }
  }

  @Test
  fun testGetItemTrackObjectIsNotList() = runBlocking {
    withTimeout(3000) {
      // Mock the Test
      val mockDocumentSnapshot = mockk<DocumentSnapshot>()

      val getTestBeacon =
          Beacon(
              id = "testBeacon",
              location = Location(1.0, 1.0, "Test Location"),
              profileAndTrack = listOf())


      every { mockDocumentSnapshot.getData() } returns getTestBeacon.toMap(firestore)

      every { mockDocumentSnapshot.exists() } returns true
      every { mockDocumentSnapshot.id } returns getTestBeacon.id
      every { mockDocumentSnapshot.get("location") } returns getTestBeacon.location.toMap()

      // not a list of maps, doesn't pass the if
      every { mockDocumentSnapshot.get("tracks") } returns listOf("String1", "String2")
      every { mockDocumentSnapshot.getLong(any()) } returns 0

      // Define behavior for the addOnSuccessListener method
      every { documentReference.addSnapshotListener(any()) } answers
          {
            val listener = arg<EventListener<DocumentSnapshot>>(0)

            // Define the behavior of the mock DocumentSnapshot here
            listener.onEvent(mockDocumentSnapshot, null)

            mockk(relaxed = true)
          }

      // Call the function under test
      beaconConnection.getItem("testBeacon").first()

      // Verify that the get function is called on the document with the correct id
      coVerify { documentReference.addSnapshotListener(any()) }
    }
  }

  @Test
  fun testGetItemFailure() {
    runBlocking {
      withTimeout(3000) {
        // Mock the Task
        val mockTask = mockk<Task<DocumentSnapshot>>()

        // Define behavior for the addOnSuccessListener method
        every { documentReference.addSnapshotListener(any()) } answers
            {
              val listener = arg<EventListener<DocumentSnapshot>>(0)

              // Define the behavior of the mock DocumentSnapshot here
              listener.onEvent(
                  null,
                  FirebaseFirestoreException(
                      "Test Exception", FirebaseFirestoreException.Code.ABORTED))

              mockk(relaxed = true)
            }

        val result = beaconConnection.getItem("testBeacon").first()
        Log.d("Firestore", "Result: $result")

        // Verify that the get function is called on the document with the correct id
        coVerify { documentReference.addSnapshotListener(any()) }
        assert(result.isFailure)
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


      val getTestBeaconList = listOf(getTestBeacon, getTestBeacon)

      every { mockDocumentSnapshot.getData() } returns getTestBeacon.toMap(firestore)
      every { mockDocumentSnapshot.exists() } returns true
      every { mockDocumentSnapshot.id } returns getTestBeacon.id
      every { mockDocumentSnapshot.get("location") } returns getTestBeacon.location.toMap()
      every { mockDocumentSnapshot.get("tracks") } returns
          getTestBeacon.profileAndTrack.map { it.toMap(firestore) }


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
  fun testDocumentTransformNullDocument() {
    runBlocking {
      val documentSnapshot = mockk<DocumentSnapshot>()
      every { documentSnapshot.exists() } returns false
      val result = beaconConnection.documentTransform(documentSnapshot, null).first()
      assert(result.isFailure)
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
  fun testAddItemAndGetId() {
    runTest {
      // Call the function under test
      every { documentReference.id } returns beacon.id
      val mockTask: Task<DocumentReference> = mockk {
        every { isComplete } returns true
        every { isSuccessful } returns true
        every { result } returns documentReference
        every { exception } returns null
        every { isCanceled } returns false
      }
      coEvery { collectionReference.add(any()) } coAnswers { mockTask }
      val id = beaconConnection.addItemAndGetId(beacon)

      assertEquals(beacon.id, id)

      // now test with null id by have unsuccessful task
      val mockTask2: Task<DocumentReference> = mockk {
        every { isComplete } returns true
        every { isSuccessful } returns false
        every { result } returns null
        every { exception } returns null
        every { isCanceled } returns false
      }

      coEvery { collectionReference.add(any()) } coAnswers { mockTask2 }
      val id2 = beaconConnection.addItemAndGetId(beacon)

      assertEquals(null, id2)
    }
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
            "tracks" to
                getTestBeacon.profileAndTrack.map {
                  hashMapOf(
                      "creator" to firestore.collection("users").document(profile.firebaseUid),
                      "track" to firestore.collection("tracks").document(track.id))
                })

    every { mockTransaction.get(any<DocumentReference>()) } answers
        {
          val reference = arg<DocumentReference>(0)

          when {
            reference.path.contains("beacons") -> mockDocumentSnapshot
            reference.path.contains("users") ->
                mockk<DocumentSnapshot>() { every { getData() } returns profile.toMap(firestore) }
            reference.path.contains("tracks") ->
                mockk<DocumentSnapshot>() { every { getData() } returns track.toMap() }
            reference.path.equals("") -> mockDocumentSnapshot
            else -> throw IllegalStateException("Invalid reference path: ${reference.path}")
          }
        }

    every { mockTransaction.update(any<DocumentReference>(), any<String>(), any()) } answers
        {
          mockTransaction
        }
    every { mockDocumentSnapshot.getData() } returns mapOfTestBeacon
    every { mockDocumentSnapshot.exists() } returns true
    every { mockDocumentSnapshot.id } returns getTestBeacon.id
    every { mockDocumentSnapshot.get("location") } returns getTestBeacon.location.toMap()
    every { mockDocumentSnapshot.get("tracks") } returns
        getTestBeacon.profileAndTrack.map {
          hashMapOf(
              "creator" to firestore.collection("users").document(profile.firebaseUid),
              "track" to firestore.collection("tracks").document(track.id))
        }

    profile.toMap(firestore).forEach { (key, value) ->
      every { mockDocumentSnapshot.get(key) } returns value
      (value as? String)?.let { every { mockDocumentSnapshot.getString(key) } returns it }
      (value as? Int)?.let { every { mockDocumentSnapshot.getLong(key) } returns it.toLong() }
      (value as? Boolean)?.let { every { mockDocumentSnapshot.getBoolean(key) } returns it }
    }
    track.toMap().forEach { (key, value) ->
      every { mockDocumentSnapshot.get(key) } returns value
      (value as? String)?.let { every { mockDocumentSnapshot.getString(key) } returns it }
      (value as? Int)?.let { every { mockDocumentSnapshot.getLong(key) } returns it.toLong() }
      (value as? Boolean)?.let { every { mockDocumentSnapshot.getBoolean(key) } returns it }
    }

    every { mockDocumentSnapshot.getLong(any()) } returns 0

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
    beaconConnection.addTrackToBeacon(beacon.id, track, "testing-uid", {})

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


    // Mock the Task
    val mockTask = mockk<Task<Transaction>>()
    val mockTransaction = mockk<Transaction>()
    val mockDocumentSnapshot = mockk<DocumentSnapshot>()

    val getTestBeacon =
        Beacon(
            id = "testBeacon",
            location = Location(1.0, 1.0, "Test Location"),
            profileAndTrack = listOf(ProfileTrackAssociation(profile, track)))

    every { mockDocumentSnapshot.getData() } returns getTestBeacon.toMap(firestore)
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
      beaconConnection.addTrackToBeacon(beacon.id, track, "testing-uid", {})
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
    val firestore = mockk<FirebaseFirestore>()
    val trackRepository = mockk<TrackConnection>(relaxed = true)
    val profileRepository = mockk<ProfileConnection>(relaxed = true)
    val beaconRepository =
        RepositoryModule.provideBeaconRepository(
            context, firestore, trackRepository, profileRepository, mockk())
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
