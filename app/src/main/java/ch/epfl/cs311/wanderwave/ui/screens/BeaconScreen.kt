package ch.epfl.cs311.wanderwave.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ch.epfl.cs311.wanderwave.R
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.data.viewModelType
import ch.epfl.cs311.wanderwave.model.utils.addTrackToBeacon
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.navigation.Route
import ch.epfl.cs311.wanderwave.ui.components.map.BeaconMapMarker
import ch.epfl.cs311.wanderwave.ui.components.map.WanderwaveGoogleMap
import ch.epfl.cs311.wanderwave.ui.components.tracklist.TrackListWithProfiles
import ch.epfl.cs311.wanderwave.ui.components.utils.LoadingScreen
import ch.epfl.cs311.wanderwave.viewmodel.BeaconViewModel
import ch.epfl.cs311.wanderwave.viewmodel.ProfileViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun BeaconScreen(
    beaconId: String?,
    profileViewModel: ProfileViewModel,
    navigationActions: NavigationActions,
    viewModel: BeaconViewModel = hiltViewModel()
) {

  LaunchedEffect(beaconId) {
    Log.d("BeaconScreen", "Fetching beacon with id: $beaconId")
    if (beaconId != null) {
      viewModel.getBeaconById(beaconId)
    } else {
      withContext(Dispatchers.Main) {
        navigationActions.navigateTo(Route.MAIN)
        Log.e("No beacons found", "No beacons found for the given id")
      }
    }
  }

  val uiState = viewModel.uiState.collectAsState().value
  Column(
      modifier = Modifier.fillMaxSize().padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally) {
        if (!uiState.isLoading && uiState.error == null && uiState.beacon != null) {
          //   BeaconScreen(beacon = uiState.beacon!!, navigationActions = navigationActions)
          BeaconScreen(
              beacon = uiState.beacon!!,
              beaconViewModel = viewModel,
              profileViewModel,
              bannedTracks = uiState.bannedTracks,
              viewModel::addTrackToBeacon,
              viewModel::selectTrack,
              navigationActions,
              viewModel)
        } else {
          LoadingScreen()
        }
      }
}

@Composable
private fun BeaconScreen(
    beacon: Beacon,
    beaconViewModel: BeaconViewModel,
    profileViewModel: ProfileViewModel,
    bannedTracks: List<Track>,
    addTrackToBeacon: (String, Track, (Boolean) -> Unit) -> Unit = { _, _, _ -> },
    onSelectTrack: (Track) -> Unit = {},
    navigationActions: NavigationActions,
    viewModel: BeaconViewModel
) {

  Column(
      modifier = Modifier.fillMaxSize().padding(8.dp).testTag("beaconScreen"),
      horizontalAlignment = Alignment.CenterHorizontally) {
        BeaconInformation(beacon.location)
        AddTrack(beacon, navigationActions, viewModel)
        SongList(
            beacon,
            beaconViewModel,
            profileViewModel,
            bannedTracks,
            addTrackToBeacon,
            onSelectTrack,
            navigationActions)
      }
}

@Composable
fun BeaconInformation(location: Location) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
        text = stringResource(R.string.beaconTitle),
        style = MaterialTheme.typography.displayLarge,
        modifier = Modifier.testTag("beaconTitle"))
    if (location.name.isNotBlank()) {
      Text(
          stringResource(R.string.beaconLocation, location.name),
          style = MaterialTheme.typography.titleMedium,
          modifier = Modifier.testTag("beaconLocation"))
    }
    WanderwaveGoogleMap(
        cameraPositionState =
            CameraPositionState(
                CameraPosition(LatLng(location.latitude, location.longitude), 15f, 0f, 0f)),
        locationSource = null,
        modifier =
            Modifier.fillMaxWidth()
                .aspectRatio(5f / 3)
                .padding(4.dp)
                .clip(RoundedCornerShape(8.dp))
                .testTag("beaconMap"),
        controlsEnabled = false,
    ) {
      BeaconMapMarker(location.toLatLng(), location.name)
    }
  }
}

@Composable
fun AddTrack(beacon: Beacon, navigationActions: NavigationActions, viewModel: BeaconViewModel) {
  viewModel.beaconId = beacon.id
  Log.d("AddTrack", "Adding track to beacon ${beacon.id}")

  Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.Center) {
        Button(onClick = { navigationActions.navigateToSelectSongScreen(viewModelType.BEACON) }) {
          Text(text = "Add a song")
        }
      }
}

@Composable
fun SongList(
    beacon: Beacon,
    beaconViewModel: BeaconViewModel,
    profileViewModel: ProfileViewModel,
    bannedTracks: List<Track>,
    addTrackToBeacon: (String, Track, (Boolean) -> Unit) -> Unit,
    onSelectTrack: (Track) -> Unit,
    navigationActions: NavigationActions
) {
  TrackListWithProfiles(
      tracks =
          beacon.profileAndTrack
              .filter { profileTrack -> bannedTracks.any { profileTrack.track.id == it.id }.not() }
              .sortedBy { it.track.id },
      profileViewModel,
      title = stringResource(R.string.beaconTracksTitle),
      onAddTrack = { track: Track ->
        Log.d(
            "SongList",
            "Adding track to beacon ${beacon.profileAndTrack
          .filter { profileTrack -> bannedTracks.any { profileTrack.track.id == it.id }.not() }
          .sortedBy { it.track.id }}")
        addTrackToBeacon(beacon.id, track) { success ->
          if (success) {
            Log.d("SongList", "Track added successfully.")
          } else {
            Log.e("SongList", "Failed to add track.")
          }
        }
      },
      navigationActions = navigationActions,
      canAddSong = false,
      beacon = beacon,
      beaconViewModel = beaconViewModel,
      onSelectTrack = onSelectTrack,
  )
}
