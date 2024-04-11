package ch.epfl.cs311.wanderwave.model

import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.remote.ProfileConnection
import com.google.firebase.firestore.DocumentSnapshot
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/*
public constructor Profile(
    val firstName: String,
    val lastName: String,
    val description: String,
    val numberOfLikes: Int,
    val isPublic: Boolean,
    val profilePictureUri: Uri? = null,
    val spotifyUid: String,
    val firebaseUid: String
)
 */

public class ProfileConnectionTest {

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK private lateinit var documentSnapshot: DocumentSnapshot
  private lateinit var profileConnection: ProfileConnection

  @Before
  fun setup() {

    MockKAnnotations.init(this)
    profileConnection = ProfileConnection()
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
}
