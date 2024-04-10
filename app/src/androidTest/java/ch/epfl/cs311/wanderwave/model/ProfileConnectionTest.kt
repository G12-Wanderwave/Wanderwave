package ch.epfl.cs311.wanderwave.model

import ch.epfl.cs311.wanderwave.model.remote.ProfileConnection
import io.mockk.junit4.MockKRule
import org.junit.Before
import org.junit.Rule

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
}
