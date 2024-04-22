package ch.epfl.cs311.wanderwave.model

import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.remote.TrackConnection
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
    every { firestore.collection(any()) } returns mockk(relaxed = true)
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
}
