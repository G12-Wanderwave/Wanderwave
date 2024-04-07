package ch.epfl.cs311.wanderwave.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.components.login.LoginAppLogo
import ch.epfl.cs311.wanderwave.ui.components.login.SignInButton
import ch.epfl.cs311.wanderwave.ui.components.login.WelcomeTitle
import ch.epfl.cs311.wanderwave.viewmodel.ProfileViewModel
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoginScreen(navigationActions: NavigationActions) {
  val profileViewModel: ProfileViewModel = hiltViewModel()

  Column(modifier = Modifier.testTag("loginScreen")) {
    LoginAppLogo(modifier = Modifier.weight(1f))
    WelcomeTitle(modifier = Modifier.weight(4f))
    SignInButton(modifier = Modifier.weight(1f)) {
      // TODO : fetch the profile from the spotify API
      var profile =
        Profile(
          "John",
          "Doe",
          description = "I am a wanderer",
          numberOfLikes = 0,
          isPublic = true,
          spotifyUid = "1234",
          firebaseUid = "1234",
          profilePictureUri = null
        )
      profileViewModel.fetchProfile(profile)
      navigationActions.signIn()
    }

    // TODO : all for testing, to be deleted before PR
    Button(onClick = {
      val db = FirebaseFirestore.getInstance()

      val beacon: Beacon = Beacon(id = "1234", location = Location(12.0, 12.0, "srilanka"), tracks = listOf())

      db.collection("beacons")
        .add(beacon)
        .addOnFailureListener { e -> Log.e("Firestore", "Error adding document: ", e) }
        .addOnSuccessListener { Log.d("Firestore", "DocumentSnapshot successfully added!") }
    }) {

    }
  }
}
