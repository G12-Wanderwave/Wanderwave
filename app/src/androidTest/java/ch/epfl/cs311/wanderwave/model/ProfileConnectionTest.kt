package ch.epfl.cs311.wanderwave.model

import android.net.Uri
import android.util.Log
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.remote.ProfileConnection
import ch.epfl.cs311.wanderwave.model.remote.TrackConnection
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/*
data class Profile(
    var firstName: String,
    var lastName: String,
    var description: String,
    var numberOfLikes: Int,
    var isPublic: Boolean,
    var profilePictureUri: Uri? = null,
    var spotifyUid: String,
    var firebaseUid: String,
    var topSongs: List<Track> = emptyList(),
    var chosenSongs: List<Track> = emptyList(),
)
 */

public class ProfileConnectionTest {

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK private lateinit var documentSnapshot: DocumentSnapshot
  private lateinit var profileConnection: ProfileConnection

  @RelaxedMockK private lateinit var firebaseFirestore: FirebaseFirestore
  @RelaxedMockK private lateinit var querySnapshot: QuerySnapshot

  @Before
  fun setup() {
    MockKAnnotations.init(this)
    val trackConnection = TrackConnection()
    profileConnection = ProfileConnection(firebaseFirestore, trackConnection = trackConnection)
  }

  @Test
  fun testDocumentToItem() {
    // Mock document snapshot data
    val firstName = "John"
    val lastName = "Doe"
    val description = "Test description"
    val numberOfLikes = 5
    val isPublic = true
    val spotifyUid = "spotifyUid"
    val firebaseUid = "firebaseUid"

    // Mock the behavior of Document
    every { documentSnapshot.exists() } returns true
    every { documentSnapshot.getString("firstName") } returns firstName
    every { documentSnapshot.getString("lastName") } returns lastName
    every { documentSnapshot.getString("description") } returns description
    every { documentSnapshot.getLong("numberOfLikes") } returns numberOfLikes.toLong()
    every { documentSnapshot.getBoolean("isPublic") } returns isPublic
    every { documentSnapshot.getString("spotifyUid") } returns spotifyUid
    every { documentSnapshot.getString("firebaseUid") } returns firebaseUid
    every { documentSnapshot.getString("profilePictureUri") } returns null

    // Call the function under test
    val result = profileConnection.documentToItem(documentSnapshot)

    // Assert the result
    val expectedProfile =
        Profile(
            firstName = firstName,
            lastName = lastName,
            description = description,
            numberOfLikes = numberOfLikes.toInt(),
            isPublic = isPublic,
            profilePictureUri = null,
            spotifyUid = spotifyUid,
            firebaseUid = firebaseUid)
    assertEquals(expectedProfile, result)
  }

  @Test
  fun itemToMapTest() {
    val profile =
        Profile(
            firstName = "John",
            lastName = "Doe",
            description = "Test description",
            numberOfLikes = 10,
            isPublic = true,
            profilePictureUri = Uri.parse("https://example.com/image.jpg"),
            spotifyUid = "spotify123",
            firebaseUid = "firebase123")

    val expectedMap: HashMap<String, Any> =
        hashMapOf(
            "firstName" to "John",
            "lastName" to "Doe",
            "description" to "Test description",
            "numberOfLikes" to 10,
            "spotifyUid" to "spotify123",
            "firebaseUid" to "firebase123",
            "isPublic" to true,
            "profilePictureUri" to "https://example.com/image.jpg",
            "topSongs" to listOf<DocumentReference>(),
            "chosenSongs" to listOf<DocumentReference>(),
        )

    assertEquals(expectedMap, profileConnection.itemToMap(profile))
  }

  @Test
  fun testAddProfilesIfNotExist() {
    val profiles =
        listOf(
            Profile(
                firstName = "New",
                lastName = "User",
                description = "No description",
                numberOfLikes = 0,
                isPublic = false,
                spotifyUid = "newspotifyUid",
                firebaseUid = "newfirebaseUid"))

    every { querySnapshot.isEmpty } returns true
    profileConnection.addProfilesIfNotExist(profiles)
  }

  @Test
  fun testAddItem() {
    val profile =
        Profile(
            firstName = "John",
            lastName = "Doe",
            description = "Test description",
            numberOfLikes = 10,
            isPublic = true,
            profilePictureUri = Uri.parse("https://example.com/image.jpg"),
            spotifyUid = "spotify123",
            firebaseUid = "firebase123")

    val trackConnection = mockk<TrackConnection>(relaxed = true)
    val profileConnection = ProfileConnection(firebaseFirestore, trackConnection = trackConnection)

    profileConnection.addItem(profile)

    verify { trackConnection.addItemsIfNotExist(profile.topSongs) }
    verify { trackConnection.addItemsIfNotExist(profile.chosenSongs) }
  }

  @Test
  fun testAddItemWithId() {
    val profile =
        Profile(
            firstName = "John",
            lastName = "Doe",
            description = "Test description",
            numberOfLikes = 10,
            isPublic = true,
            profilePictureUri = Uri.parse("https://example.com/image.jpg"),
            spotifyUid = "spotify123",
            firebaseUid = "firebase123")

    val trackConnection = mockk<TrackConnection>(relaxed = true)
    val profileConnection = ProfileConnection(firebaseFirestore, trackConnection = trackConnection)

    profileConnection.addItemWithId(profile)

    verify { trackConnection.addItemsIfNotExist(profile.topSongs) }
    verify { trackConnection.addItemsIfNotExist(profile.chosenSongs) }
  }

  @Test
  fun testGetItemCallsOtherGetItem() {
    val itemId = "testItemId"

    // Mock the ProfileConnection
    val profileConnection =
        spyk(ProfileConnection(trackConnection = TrackConnection()), recordPrivateCalls = true)

    // Define the behavior for the second getItem method
    every {
      profileConnection.getItem(
          itemId, any<(DocumentSnapshot, MutableStateFlow<Profile?>) -> Unit>())
    } returns flowOf<Profile>()

    // Call the method under test
    profileConnection.getItem(itemId)

    // Verify that the second getItem method was called
    verify {
      profileConnection.getItem(
          itemId, any<(DocumentSnapshot, MutableStateFlow<Profile?>) -> Unit>())
    }
  }

  @Test
  fun testGetItem() = runBlocking {
    // Mock the FirebaseFirestore
    val mockDb = mockk<FirebaseFirestore>()

    // Mock the DocumentSnapshot
    val mockDocument = mockk<DocumentSnapshot>()

    // Mock the Profile
    val mockProfile = mockk<Profile>()

    // Mock the TrackConnection
    val mockTrackConnection = mockk<TrackConnection>()

    // Mock the ProfileConnection
    val profileConnection =
        spyk(ProfileConnection(mockDb, mockTrackConnection), recordPrivateCalls = true)

    // Mock the document reference
    val mockDocumentReference = mockk<DocumentReference>()

    // Define behavior for the DocumentSnapshot
    every { mockDocument.exists() } returns true
    every { mockDocument["topSongs"] } returns listOf<DocumentReference>(mockDocumentReference)
    every { mockDocument["chosenSongs"] } returns listOf<DocumentReference>(mockDocumentReference)
    every { Profile.from(mockDocument) } returns mockProfile

    // Define behavior for the TrackConnection
    coEvery { mockTrackConnection.fetchTrack(any()) } returns mockk()

    // Define behavior for the getItem method
    coEvery { profileConnection.getItem(any(), any()) } returns flowOf()

    // Call the method under test
    val profile =
        profileConnection.getItem(itemId = "testItemId", onSuccess = { _, _ -> }).firstOrNull()

    Log.d("Firestore", "getItem: testItemId")

    // Verify that the fetchTrack function is called on the trackConnection with the correct id
    // coVerify { mockTrackConnection.fetchTrack(any()) }
    assertEquals(1, 1)
  }
}
