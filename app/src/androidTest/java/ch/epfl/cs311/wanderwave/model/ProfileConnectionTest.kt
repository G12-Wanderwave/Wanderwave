package ch.epfl.cs311.wanderwave.model

import android.util.Log
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.remote.ProfileConnection
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
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
  private lateinit var profileConnection: ProfileConnection
  @RelaxedMockK private lateinit var firebaseFirestore: FirebaseFirestore

  @Before
  fun setup() {
    MockKAnnotations.init(this)

    profileConnection = ProfileConnection(firebaseFirestore)
  }

  // @Test
  // fun testAddAndGetItem() = runBlocking {
  //   withTimeout(30000) {
  //     // Given
  //     val profile = Profile(
  //       firstName = "My FirstName",
  //       lastName = "My LastName",
  //       description = "My Description",
  //       numberOfLikes = 0,
  //       isPublic = true,
  //       profilePictureUri = null,
  //       firebaseUid = "My Firebase UID",
  //       spotifyUid = "My Spotify UID"
  //     )
  //
  //     // Mock the behavior of FirebaseFirestore
  //     val documentReference = mockk<DocumentReference>()
  //     val documentSnapshot = mockk<DocumentSnapshot>()
  //     val task = mockk<Task<DocumentSnapshot>>()
  //     every { task.addOnFailureListener(any()) } returns task
  //     every { documentSnapshot.toObject(Profile::class.java) } returns profile
  //     every { firebaseFirestore.collection(any()).document(any()) } returns documentReference
  //     every { documentReference.set(profile) } returns mockk()
  //     every { documentReference.get() } returns Tasks.forResult(documentSnapshot)
  //     every { documentReference.set(any()) } returns mockk()
  //
  //     // When
  //     profileConnection.addItemWithId(profile)
  //     val retrievedProfile = profileConnection.getItem("testProfile").first()
  //
  //     // Then
  //     assertEquals(profile, retrievedProfile)
  //   }
  // }
}
