package ch.epfl.cs311.wanderwave.ui.screens

import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.remote.ProfileConnection
import ch.epfl.cs311.wanderwave.model.remote.TrackConnection
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.navigation.Route
import ch.epfl.cs311.wanderwave.ui.components.login.LoginAppLogo
import ch.epfl.cs311.wanderwave.ui.components.login.SignInButton
import ch.epfl.cs311.wanderwave.ui.components.login.WelcomeTitle
import ch.epfl.cs311.wanderwave.viewmodel.LoginScreenViewModel
import com.spotify.sdk.android.auth.AuthorizationClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navigationActions: NavigationActions,
    showMessage: (String) -> Unit,
    viewModel: LoginScreenViewModel = hiltViewModel()
) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()

  LaunchedEffect(state) {
    state.let {
      if (it.hasResult) {
        if (it.message != null) {
          showMessage(it.message)
        }
        if (it.success) {
          navigationActions.navigateTo(Route.SPOTIFY_CONNECT)
        }
      }
    }
  }

  val launcher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.StartActivityForResult()) {
            val response = AuthorizationClient.getResponse(it.resultCode, it.data)
            viewModel.handleAuthorizationResponse(response)
          }
  val context = LocalContext.current

  Column(modifier = Modifier.testTag("loginScreen")) {
    LoginAppLogo(modifier = Modifier.weight(1f))
    WelcomeTitle(modifier = Modifier.weight(4f))
    SignInButton(modifier = Modifier.weight(1f)) {
      val intent =
          AuthorizationClient.createLoginActivityIntent(
              context.getActivity(), viewModel.getAuthorizationRequest())
      launcher.launch(intent)

      // val track = Track(id = "1", title = "1", artist = "1")
      //
      // val profile =
      //     Profile(
      //         firstName = "New",
      //         lastName = "User",
      //         description = "No description",
      //         numberOfLikes = 0,
      //         isPublic = false,
      //         spotifyUid = "newspotifyUid",
      //         firebaseUid = "newfirebaseUid",
      //         topSongs = listOf(track, track),
      //         chosenSongs = listOf(track, track, track))
      //
      // val trackConnection = TrackConnection()
      // val profileConnection = ProfileConnection(trackConnection = trackConnection)

      // val beaconConnection =
      //     BeaconConnection(trackConnection = trackConnection, profileConnection =
      // profileConnection)

      // profileConnection.addItemWithId(profile)
      //
      // GlobalScope.launch {
      //   profileConnection.getItem(profile.firebaseUid).collect {
      //     Log.d("Debug", "collected Profile ${it}")
      //   }
      // }
    }
  }
}

private fun Context.getActivity(): ComponentActivity? {
  var currentContext = this
  while (currentContext is ContextWrapper) {
    if (currentContext is ComponentActivity) {
      return currentContext
    }
    currentContext = currentContext.baseContext
  }
  return null
}
