package ch.epfl.cs311.wanderwave.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.remote.BeaconConnection
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.components.login.LoginAppLogo
import ch.epfl.cs311.wanderwave.ui.components.login.SignInButton
import ch.epfl.cs311.wanderwave.ui.components.login.WelcomeTitle
import ch.epfl.cs311.wanderwave.viewmodel.ProfileViewModel
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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

    val beaconConnection: BeaconConnection = BeaconConnection()
    val beaconState = beaconConnection.getItem("EmSELs5dY9UsPyyrNvIX").collectAsState(initial = null)
    // TODO : all for testing, to be deleted before PR
    Button(onClick = {
      val db = FirebaseFirestore.getInstance()

      val beacon: Beacon = Beacon(id = "12345", location = Location(12.0, 12.0, "srilanka"), tracks = listOf())

      Log.d("Firestore", "Test Beacon: ${beaconState.value}")
      Log.d("Firestore", "Test Beacon tracks: ${beaconState.value?.tracks}")



      // db.collection("beacons")
      //   .add(beacon)
      //   .addOnFailureListener { e -> Log.e("Firestore", "Error adding document: ", e) }
      //   .addOnSuccessListener { Log.d("Firestore", "DocumentSnapshot successfully added!") }

      // val dataFlow = MutableStateFlow(null)
      // db.collection("beacons")
      //   .document("EmSELs5dY9UsPyyrNvIX")
      //   .get()
      //   .addOnSuccessListener { document ->
      //     if (document != null && document.data != null) {
      //       val trackRefs = document.get("tracks") as? List<DocumentReference>
      //       trackRefs?.forEach { trackRef ->
      //           trackRef.get().addOnSuccessListener { trackDocument ->
      //               val trackData = trackDocument.data
      //               Log.d("Firestore", "Track data: $trackData")
      //           }
      //       }
      //     }
      //   }
      //   .addOnFailureListener { e -> Log.e("Firestore", "Error getting document: ", e) }

    }, modifier = Modifier.fillMaxWidth()) {
      Text(text = "Test Button")
    }
  }
}
