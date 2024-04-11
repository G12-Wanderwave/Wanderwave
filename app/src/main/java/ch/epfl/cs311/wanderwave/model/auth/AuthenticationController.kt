package ch.epfl.cs311.wanderwave.model.auth

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class AuthenticationController @Inject constructor(private val auth: FirebaseAuth) {

  fun isSignedIn(): Boolean {
    return auth.currentUser != null
  }

  fun signIn(): Flow<Boolean> {
    if (auth.currentUser != null) {
      return flowOf(true)
    }
    return flow {
      val result = auth.signInAnonymously().await()
      if (result.user != null) {
        emit(true)
      } else {
        emit(false)
      }
    }
  }

  fun signOut() {
    auth.signOut()
  }

  private data class State(val isSignedIn: Boolean = false)
}
