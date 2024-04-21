package ch.epfl.cs311.wanderwave.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ch.epfl.cs311.wanderwave.R
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.components.map.WanderwaveGoogleMap
import ch.epfl.cs311.wanderwave.ui.components.map.WanderwaveMapMarker
import ch.epfl.cs311.wanderwave.ui.components.profile.AddTrackDialog
import ch.epfl.cs311.wanderwave.ui.components.utils.LoadingScreen
import ch.epfl.cs311.wanderwave.ui.theme.WanderwaveTheme
import ch.epfl.cs311.wanderwave.viewmodel.BeaconViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState

@Composable
fun BeaconScreen(
    navigationActions: NavigationActions,
    viewModel: BeaconViewModel = hiltViewModel()
) {
  // id value remebered for the text field
  // Here is the id of a good beacon for testing : UAn8OUadgrUOKYagf8a2
  val uiState = viewModel.uiState.collectAsState().value
  Column(
      modifier = Modifier.fillMaxSize().padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally) {
        if (!uiState.isLoading) {
          BeaconScreen(beacon = uiState.beacon!!)
        } else {
          LoadingScreen()
        }
      }
}

@Composable
@Preview(showBackground = true)
private fun BeaconScreenPreview() {
  val previewBeacon =
      Beacon(
          id = "a",
          location = Location(latitude = 46.519962, longitude = 6.633597, name = "EPFL"),
          tracks =
              listOf(
                  Track("a", "Never gonna give you up", "Rick Astley"),
                  Track("b", "Take on me", "A-ha"),
                  Track("c", "Africa", "Toto"),
              ),
      )
  WanderwaveTheme { BeaconScreen(previewBeacon) }
}

@Composable
private fun BeaconScreen(beacon: Beacon, viewModel: BeaconViewModel = hiltViewModel()) {
  Column(
      modifier = Modifier.fillMaxSize().padding(8.dp).testTag("beaconScreen"),
      horizontalAlignment = Alignment.CenterHorizontally) {
        BeaconInformation(beacon.location)
        SongList(beacon, viewModel)
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
      WanderwaveMapMarker(location.toLatLng(), location.name)
    }
  }
}

@Composable
fun SongList(beacon: Beacon, viewModel: BeaconViewModel) {
  // State to control the visibility of the add track dialog
  var showDialog by remember { mutableStateOf(false) }

  Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {
          Text(
              text = stringResource(R.string.beaconTracksTitle),
              style = MaterialTheme.typography.headlineMedium,
              modifier = Modifier)
          IconButton(onClick = { showDialog = true }) { // Toggle dialog visibility
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.beaconTitle))
          }
        }
    LazyColumn { items(beacon.tracks) { TrackItem(it) } }

    if (showDialog) {
      AddTrackDialog(
          onAddTrack = { id, title, artist ->
            viewModel.addTrackToBeacon(beacon.id, Track(id, title, artist)) { success ->
              if (success) {
                Log.d("SongList", "Track added successfully.")
              } else {
                Log.e("SongList", "Failed to add track.")
              }
            }
            showDialog = false // Close dialog after adding track
          },
          onDismiss = {
            showDialog = false // Close dialog on dismiss
          },
          initialTrackId = "",
          initialTrackTitle = "",
          initialTrackArtist = "",
          dialogTestTag = "addTrackDialog" // For testing purposes
          )
    }
  }
}

@Composable
internal fun TrackItem(track: Track) {
  Card(
      colors =
          CardColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
              CardDefaults.cardColors().contentColor,
              CardDefaults.cardColors().disabledContainerColor,
              CardDefaults.cardColors().disabledContentColor),
      modifier = Modifier.height(80.dp).fillMaxWidth().padding(4.dp).testTag("trackItem")) {
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
          Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = track.title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium)
            Text(
                text = track.artist,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
          }
        }
      }
}
