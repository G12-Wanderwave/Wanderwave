package ch.epfl.cs311.wanderwave.ui.components.tracklist

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ch.epfl.cs311.wanderwave.model.data.ProfileTrackAssociation
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.components.profile.SelectImage
import kotlinx.coroutines.launch

@Composable
fun TrackListItemWithProfile(
    trackAndProfile: ProfileTrackAssociation,
    selected: Boolean,
    onClick: () -> Unit,
    navigationActions: NavigationActions
) {
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }
  Card(
      onClick = onClick,
      colors =
          CardColors(
              containerColor =
                  if (selected) MaterialTheme.colorScheme.surfaceContainerHighest
                  else MaterialTheme.colorScheme.surfaceContainerHigh,
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
                text = trackAndProfile.track.title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium)
            Text(
                text = trackAndProfile.track.artist,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
          }
          SelectImage(
              modifier =
                  Modifier.size(width = 150.dp, height = 100.dp)
                      .clickable(
                          enabled = trackAndProfile.profile.isPublic,
                          onClick = {
                            if (trackAndProfile.profile.isPublic) {
                              // if the profile is public, navigate to the profile view screen
                              navigationActions.navigateToProfile(
                                  trackAndProfile.profile.firebaseUid)
                            } else {
                              // if the profile is private , output a message that say the profile
                              // is
                              // private, you cannot access to profile informations
                              scope.launch {
                                snackbarHostState.showSnackbar(
                                    "This profile is private, you cannot access profile information.")
                              }
                            }
                          }),
              imageUri = trackAndProfile.profile.profilePictureUri,
          )
        }
      }
}

@Composable
fun TrackListItem(track: Track, selected: Boolean, onClick: () -> Unit) {
  Card(
      onClick = onClick,
      colors =
          CardColors(
              containerColor =
                  if (selected) MaterialTheme.colorScheme.surfaceContainerHighest
                  else MaterialTheme.colorScheme.surfaceContainerHigh,
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
