package ch.epfl.cs311.wanderwave.model;

import android.util.Log
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.remote.BeaconConnection
import ch.epfl.cs311.wanderwave.model.remote.ProfileConnection
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepositoryImpl
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
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

  @Before
  fun setup() {
    profileConnection = ProfileConnection()
  }

  @Test
  fun testAddAndGetItem() = runBlocking {
    withTimeout(30000) {
      var profile =
        Profile(
          firstName = "My FirstName",
          lastName = "My LastName",
          description = "My Description",
          numberOfLikes = 0,
          isPublic = true,
          profilePictureUri = null,
          firebaseUid = "My Firebase UID",
          spotifyUid = "My Spotify UID")

      profileConnection.addItemWithId(profile)

      Log.d("Firestore", "Added item")
      val retrievedProfile = profileConnection.getItem("testProfile").first()
      Log.d("Firestore", "$retrievedProfile $profile")
      assert(profile == retrievedProfile)
    }
  }
}
