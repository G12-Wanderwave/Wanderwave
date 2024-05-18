package ch.epfl.cs311.wanderwave.model

import android.net.Uri
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.data.ProfileTrackAssociation
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.remote.TrackConnection
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TrackConnectionTest {
  @get:Rule val mockkRule = MockKRule(this)
  private lateinit var trackConnection: TrackConnection

  private lateinit var firestore: FirebaseFirestore
  private lateinit var documentReference: DocumentReference
  private lateinit var collectionReference: CollectionReference

  lateinit var track: Track

  @Before
  fun setup() {
    // Create the mocks
    firestore = mockk()
    documentReference = mockk<DocumentReference>(relaxed = true)
    collectionReference = mockk<CollectionReference>(relaxed = true)

    // Mock data
    track = Track("testTrack", "Test Title", "Test Artist")

    // Define behavior for the mocks
    every { collectionReference.document(track.id) } returns documentReference
    every { firestore.collection(any()) } returns collectionReference

    // Pass the mock Firestore instance to your TrackConnection
    trackConnection = TrackConnection(firestore)
  }

  @Test
  fun testAddItem() {
    // Call the function under test
    trackConnection.addItemWithId(track)

    // Verify that either the set function is called
    verify { documentReference.set(any()) }
  }

  @Test
  fun testUpdateItem() {
    // Call the function under test
    trackConnection.updateItem(track)

    // Verify that the set function is called on the document with the correct id
    verify { documentReference.set(any()) }
  }

  @Test
  fun testGetItem() = runBlocking {
    withTimeout(3000) {
      // Mock the Task
      val mockTask = mockk<Task<DocumentSnapshot>>()
      val mockDocumentSnapshot = mockk<DocumentSnapshot>()

      val getTestTrack = Track("testTrack", "Test Title", "Test Artist")

      every { mockDocumentSnapshot.getData() } returns getTestTrack.toMap()
      every { mockDocumentSnapshot.exists() } returns true
      every { mockDocumentSnapshot.id } returns getTestTrack.id
      every { mockDocumentSnapshot.getString("title") } returns getTestTrack.title
      every { mockDocumentSnapshot.getString("artist") } returns getTestTrack.artist

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
      val retrievedTrack = trackConnection.getItem("testTrack").first()

      // Verify that the get function is called on the document with the correct id
      coVerify { documentReference.get() }
      assertEquals(getTestTrack, retrievedTrack)
    }
  }

  @Test
  fun testDeleteItem() {
    // Call the function under test
    trackConnection.deleteItem("testTrack")

    // Verify that the delete function is called on the document with the correct id
    verify { documentReference.delete() }
  }

  @Test
  fun testFetchProfileAndTrack() = runBlocking {
    // Mock the DocumentReference
    val mockTrackDocumentReference = mockk<DocumentReference>()
    val mockProfileDocumentReference = mockk<DocumentReference>()

    // Mock the DocumentSnapshot
    val mockDocumentSnapshot = mockk<DocumentSnapshot>()

    // Mock the Track
    val mockTrack = Track("testTrackId", "Test Title", "Test Artist")
    val mockProfile =
        Profile(
            "Test First Name",
            "Test Last Name",
            "Test Description",
            10,
            true,
            Uri.parse("https://example.com/profile.jpg"),
            "Test Spotify Uid",
            "Test Firebase Uid")

    val mockProfileTrackAssociation = ProfileTrackAssociation(mockProfile, mockTrack)

    // Define behavior for the get() method on the DocumentReference to return the mock task
    coEvery { mockTrackDocumentReference.get() } returns
        mockk {
          every { isComplete } returns true
          every { isSuccessful } returns true
          every { result } returns mockDocumentSnapshot
          every { getException() } returns null
          every { isCanceled } returns false
        }

    coEvery { mockProfileDocumentReference.get() } returns
        mockk {
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
    every { mockDocumentSnapshot.getString("firstName") } returns mockProfile.firstName
    every { mockDocumentSnapshot.getString("lastName") } returns mockProfile.lastName
    every { mockDocumentSnapshot.getString("description") } returns mockProfile.description
    every { mockDocumentSnapshot.getLong("numberOfLikes") } returns
        mockProfile.numberOfLikes.toLong()
    every { mockDocumentSnapshot.getBoolean("isPublic") } returns mockProfile.isPublic
    every { mockDocumentSnapshot.getString("spotifyUid") } returns mockProfile.spotifyUid
    every { mockDocumentSnapshot.getString("firebaseUid") } returns mockProfile.firebaseUid
    every { mockDocumentSnapshot.getString("profilePictureUri") } returns
        mockProfile.profilePictureUri.toString()

    var retrievedTrackAndProfile: ProfileTrackAssociation? = null

    val mapOfDocumentReferences =
        mapOf("creator" to mockProfileDocumentReference, "track" to mockTrackDocumentReference)

    // Call the function under test
    launch {
      retrievedTrackAndProfile = trackConnection.fetchProfileAndTrack(mapOfDocumentReferences).firstOrNull()?.getOrElse { null }
    }
    // Verify that the get function is called on the document with the correct id
    coVerify { mockTrackDocumentReference.get() }

    // Assert that the retrieved track is the same as the mock track
    assertEquals(mockProfileTrackAssociation, retrievedTrackAndProfile)
  }

  @Test
  fun testFetchProfileAndTrackNullDocumentReference() = runBlocking {
    // Call the function under test
    val retrievedTrack = trackConnection.fetchProfileAndTrack(null)

    // Assert that the retrieved track is null
    assertEquals(null, retrievedTrack)
  }

  @Test
  fun testFetchTrackNullTrackDocument() = runBlocking {
    // Mock the DocumentReference
    val mockDocumentReference = mockk<DocumentReference>()

    // Define behavior for the get() method on the DocumentReference to return the mock task
    coEvery { mockDocumentReference.get() } returns
        mockk {
          every { isComplete } returns true
          every { isSuccessful } returns true
          every { result } returns null
          every { getException() } returns null
          every { isCanceled } returns false
        }

    val documentReferenceMap =
        mapOf("track" to mockDocumentReference, "creator" to mockDocumentReference)

    // Call the function under test
    val retrievedTrack = trackConnection.fetchProfileAndTrack(documentReferenceMap)

    // Verify that the get function is called on the document with the correct id
    coVerify { mockDocumentReference.get() }

    // Assert that the retrieved track is null
    assertEquals(null, retrievedTrack)
  }

  @Test
  fun testFetchProfileAndTrackException() = runBlocking {
    // Mock the DocumentReference
    val mockDocumentReference = mockk<DocumentReference>()

    // Define behavior for the get() method on the DocumentReference to return the mock task
    coEvery { mockDocumentReference.get() } returns
        mockk {
          every { isComplete } returns true
          every { isSuccessful } returns false
          every { result } returns null
          every { getException() } returns Exception("Test Exception")
          every { isCanceled } returns false
        }

    val documentReferenceMap =
        mapOf("track" to mockDocumentReference, "creator" to mockDocumentReference)

    // Call the function under test
    val retrievedTrack = trackConnection.fetchProfileAndTrack(documentReferenceMap)

    // Verify that the get function is called on the document with the correct id
    coVerify { mockDocumentReference.get() }

    // Assert that the retrieved track is null
    assertEquals(null, retrievedTrack)
  }

  @Test
  fun testFetchTrack() = runBlocking {
    // Mock the DocumentReference
    val mockTrackDocumentReference = mockk<DocumentReference>()

    // Mock the DocumentSnapshot
    val mockDocumentSnapshot = mockk<DocumentSnapshot>()

    // Mock the Track
    val mockTrack = Track("testTrackId", "Test Title", "Test Artist")

    // Define behavior for the get() method on the DocumentReference to return the mock task
    coEvery { mockTrackDocumentReference.get() } returns
        mockk {
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

    // Call the function under test
    val retrievedTrack = trackConnection.fetchTrack(mockTrackDocumentReference)

    // Verify that the get function is called on the document with the correct id
    coVerify { mockTrackDocumentReference.get() }

    // Assert that the retrieved track is the same as the mock track
    assertEquals(mockTrack, retrievedTrack)
  }

  @Test
  fun testFetchTrackNullDocumentReference() = runBlocking {
    // Call the function under test
    val retrievedTrack = trackConnection.fetchTrack(null)

    // Assert that the retrieved track is null
    assertEquals(null, retrievedTrack)
  }

  @Test
  fun testFetchTrackException() = runBlocking {
    // Mock the DocumentReference
    val mockDocumentReference = mockk<DocumentReference>()

    // Define behavior for the get() method on the DocumentReference to return the mock task
    coEvery { mockDocumentReference.get() } returns
        mockk {
          every { isComplete } returns true
          every { isSuccessful } returns false
          every { result } returns null
          every { getException() } returns Exception("Test Exception")
          every { isCanceled } returns false
        }

    // Call the function under test
    val retrievedTrack = trackConnection.fetchTrack(mockDocumentReference)

    // Verify that the get function is called on the document with the correct id
    coVerify { mockDocumentReference.get() }

    // Assert that the retrieved track is null
    assertEquals(null, retrievedTrack)
  }

  @Test
  fun testAddItemsIfNotExist() = runBlocking {
    // Mock the Task
    val mockTask = mockk<Task<QuerySnapshot>>()
    val mockQuerySnapshot = mockk<QuerySnapshot>()
    val mockDocumentSnapshot = mockk<DocumentSnapshot>()

    // Define behavior for the addOnSuccessListener method
    every { mockTask.addOnSuccessListener(any<OnSuccessListener<QuerySnapshot>>()) } answers
        {
          val listener = arg<OnSuccessListener<QuerySnapshot>>(0)

          // Define the behavior of the mock QuerySnapshot here
          listener.onSuccess(mockQuerySnapshot)
          mockTask
        }
    every { mockTask.addOnFailureListener(any()) } answers { mockTask }

    // Define behavior for the get() method on the DocumentReference to return the mock task
    every { firestore.collection(trackConnection.collectionName) } returns collectionReference
    every { collectionReference.whereEqualTo("id", track.id) } returns collectionReference
    every { collectionReference.get() } returns mockTask

    // Define behavior for the QuerySnapshot
    every { mockQuerySnapshot.isEmpty } returns true
    every { mockQuerySnapshot.documents } returns listOf(mockDocumentSnapshot)

    every { mockDocumentSnapshot.exists() } returns true
    every { mockDocumentSnapshot.id } returns track.id
    every { mockDocumentSnapshot.getString("title") } returns track.title
    every { mockDocumentSnapshot.getString("artist") } returns track.artist

    // Call the function under test
    trackConnection.addItemsIfNotExist(listOf(track))

    // Verify that the get function is called on the document with the correct id
    coVerify { collectionReference.whereEqualTo("id", "spotify:track:" + track.id) }
    coVerify { collectionReference.whereEqualTo("id", "spotify:track:" + track.id).get() }
  }
}
