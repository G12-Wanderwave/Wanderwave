package ch.epfl.cs311.wanderwave.model

import android.net.Uri
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import io.mockk.called
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class AuthenticationControllerTest {

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK private lateinit var mockFirebaseAuth: FirebaseAuth

  private lateinit var authenticationController: AuthenticationController

  private val dummyUser =
      AuthenticationController.UserData(
          "testid", "testemail", "testDisplayName", "https://example.com/testphoto.jpg")

  @Before
  fun setup() {
    authenticationController = AuthenticationController(mockFirebaseAuth)
  }

  fun setupDummyUserSignedIn() {
    every { mockFirebaseAuth.currentUser } returns
        mockk {
          every { uid } returns dummyUser.id
          every { email } returns dummyUser.email
          every { displayName } returns dummyUser.displayName
          every { photoUrl } returns Uri.parse(dummyUser.photoUrl)
        }
  }

  @Test
  fun canGetData() = runBlocking {
    setupDummyUserSignedIn()

    val userData = authenticationController.getUserData()
    assert(userData == dummyUser)

    assert(authenticationController.isSignedIn())
  }

  @Test
  fun canSignOut() = runBlocking {
    every { mockFirebaseAuth.signOut() } returns Unit
    authenticationController.deauthenticate()
    verify { mockFirebaseAuth.signOut() }
  }

  @Test
  fun alreadySignedIn() = runBlocking {
    setupDummyUserSignedIn()
    val result = authenticationController.authenticate("testtoken").first()
    assert(result)

    verify { mockFirebaseAuth.signInAnonymously() wasNot called }
  }

  @Test
  fun signInAnonymously() = runBlocking {
    val mockFirebaseUser =
        mockk<com.google.firebase.auth.FirebaseUser> {
          every { uid } returns "testid"
          every { email } returns null
          every { displayName } returns null
          every { photoUrl } returns null
        }

    val task =
        mockk<Task<AuthResult>> {
          every { result } returns mockk { every { user } returns mockFirebaseUser }
          every { isComplete } returns true
          every { isSuccessful } returns true
          every { isCanceled } returns false
          every { exception } returns null
        }

    every { mockFirebaseAuth.currentUser } returns null
    every { mockFirebaseAuth.signInAnonymously() } returns task

    val result = authenticationController.authenticate("testtoken").first()
    verify { mockFirebaseAuth.signInAnonymously() }
    assert(result)
  }
}
