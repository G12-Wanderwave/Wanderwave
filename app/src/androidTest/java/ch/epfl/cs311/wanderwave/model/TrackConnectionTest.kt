package ch.epfl.cs311.wanderwave.model

import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.remote.TrackConnection
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
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
    // Call the function under test
    val retrievedTrack = trackConnection.getItem("testTrack")

    // Verify that the get function is called on the document with the correct id
    verify { documentReference.get() }
  }

  @Test
  fun testDeleteItem() {
    // Call the function under test
    trackConnection.deleteItem("testTrack")

    // Verify that the delete function is called on the document with the correct id
    verify { documentReference.delete() }
  }
}
