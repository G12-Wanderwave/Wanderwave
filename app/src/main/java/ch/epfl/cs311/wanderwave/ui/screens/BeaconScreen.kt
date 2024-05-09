package ch.epfl.cs311.wanderwave.ui.screens

import android.util.Log
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import ch.epfl.cs311.wanderwave.model.data.ProfileTrackAssociation
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.navigation.Route
import ch.epfl.cs311.wanderwave.ui.components.map.BeaconMapMarker
import ch.epfl.cs311.wanderwave.ui.components.map.WanderwaveGoogleMap
import ch.epfl.cs311.wanderwave.ui.components.profile.SelectImage
import ch.epfl.cs311.wanderwave.ui.components.utils.LoadingScreen
import ch.epfl.cs311.wanderwave.viewmodel.BeaconViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun BeaconScreen(
    beaconId: String?,
    navigationActions: NavigationActions,
    viewModel: BeaconViewModel = hiltViewModel()
) {

  LaunchedEffect(beaconId) {
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
        if (!uiState.isLoading) {
          //   BeaconScreen(beacon = uiState.beacon!!, navigationActions = navigationActions)
          BeaconScreen(beacon = uiState.beacon!!, viewModel::addTrackToBeacon, navigationActions)
        } else {
          LoadingScreen()
        }
      }
}

@Composable
private fun BeaconScreen(
    beacon: Beacon,
    addTrackToBeacon: (String, Track, (Boolean) -> Unit) -> Unit = { _, _, _ -> },
    navigationActions: NavigationActions
) {
  Column(
      modifier = Modifier.fillMaxSize().padding(8.dp).testTag("beaconScreen"),
      horizontalAlignment = Alignment.CenterHorizontally) {
        BeaconInformation(beacon.location)
        SongList(beacon, addTrackToBeacon, navigationActions)
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
    // TODO: Maybe add location tracking here too?
    WanderwaveGoogleMap(
        cameraPositionState =
            CameraPositionState(
                CameraPosition(LatLng(location.latitude, location.longitude), 15f, 0f, 0f)),
        locationSource = null,
        modifier =
            Modifier.fillMaxWidth()
                .aspectRatio(4f / 3)
                .padding(4.dp)
                .clip(RoundedCornerShape(8.dp))
                .testTag("beaconMap"),
        controlsEnabled = false,
    ) {
      BeaconMapMarker(location.toLatLng(), location.name)
    }
  }
}

// TODO : @Jonas, pls look at it again
// @Composable
// fun SongList(beacon: Beacon, addTrackToBeacon: (String, Track, (Boolean) -> Unit) -> Unit) {
//    TrackList(
//        beacon.profileAndTrack,
//        title = stringResource(R.string.beaconTracksTitle),
//        onAddTrack = {
//            addTrackToBeacon(beacon.id, it) { success ->
//                if (success) {
//                    Log.d("SongList", "Track added successfully.")
//                } else {
//                    Log.e("SongList", "Failed to add track.")
//                }
//            }
//        })
// }

@Composable
fun SongList(
    beacon: Beacon,
    addTrackToBeacon: (String, Track, (Boolean) -> Unit) -> Unit,
    navigationActions: NavigationActions
) {
  HorizontalDivider()
  Text(
      text = stringResource(R.string.beaconTracksTitle),
      style = MaterialTheme.typography.displayMedium,
      modifier = Modifier.testTag("beaconTracksTitle"))
  LazyColumn { items(beacon.profileAndTrack) { TrackItem(it, navigationActions) } }
}

@Composable
internal fun TrackItem(
    profileAndTrack: ProfileTrackAssociation,
    navigationActions: NavigationActions
) {
  val scrollState = rememberScrollState()
  val scope = rememberCoroutineScope()
  val slowScrollAnimation: AnimationSpec<Float> = TweenSpec(durationMillis = 5000, easing = { it })
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(key1 = true) {
    while (true) {
      scope.launch {
        scrollState.animateScrollTo(
            value = scrollState.maxValue, animationSpec = slowScrollAnimation)
      }
      delay(6000)
      scope.launch { scrollState.animateScrollTo(value = 0, animationSpec = slowScrollAnimation) }
      delay(6000)
    }
  }

  Card(
      colors =
          CardColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
              CardDefaults.cardColors().contentColor,
              CardDefaults.cardColors().disabledContainerColor,
              CardDefaults.cardColors().disabledContentColor),
      modifier = Modifier.height(80.dp).fillMaxWidth().padding(4.dp).testTag("trackItem"),
  ) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
          modifier = Modifier.fillMaxHeight().aspectRatio(1f),
          contentAlignment = Alignment.Center) {
            Image(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Album Cover",
                modifier = Modifier.fillMaxSize(.8f),
            )
          }
      Row(modifier = Modifier.padding(0.dp).weight(3f)) {
        Column(modifier = Modifier.padding(8.dp).weight(1f).horizontalScroll(scrollState)) {
          Text(
              text = profileAndTrack.track.title,
              color = MaterialTheme.colorScheme.onSurface,
              style = MaterialTheme.typography.titleMedium)
          Text(
              text = profileAndTrack.track.artist,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              style = MaterialTheme.typography.bodyMedium)
        }
        SelectImage(
            modifier =
                Modifier.size(width = 150.dp, height = 100.dp)
                    .clickable(
                        enabled = profileAndTrack.profile?.isPublic ?: false,
                        onClick = {
                          profileAndTrack.profile?.let {
                            if (profileAndTrack.profile.isPublic) {
                              // if the profile is public, navigate to the profile view screen
                              navigationActions.navigateToProfile(profileAndTrack.profile.firebaseUid)
                            } else {
                              // if the profile is private , output a message that say the profile is
                              // private, you cannot access to profile informations
                              scope.launch {
                                snackbarHostState.showSnackbar(
                                  "This profile is private, you cannot access profile information."
                                )
                              }
                            }
                          }
                        }),
            imageUri = profileAndTrack.profile?.profilePictureUri ?: null,
        )
      }
    }
  }
  SnackbarHost(hostState = snackbarHostState)
}
