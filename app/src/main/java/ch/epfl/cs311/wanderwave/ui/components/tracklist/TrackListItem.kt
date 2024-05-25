package ch.epfl.cs311.wanderwave.ui.components.tracklist

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ch.epfl.cs311.wanderwave.model.data.ProfileTrackAssociation
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.components.profile.PlaceholderProfilePicture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A single track item in the track list.
 *
 * @param track The track to display.
 * @param selected Whether the track is selected.
 * @param onClick The action to perform when the track is clicked.
 */
@Composable
fun TrackListItem(track: Track, selected: Boolean, onClick: () -> Unit) {
  val scope = rememberCoroutineScope()
  val scrollState = rememberScrollState()

  TrackListItemLaunchedEffect(scope = scope, scrollState = scrollState)

  TrackListItemCard(onClick = onClick, selected = selected) {
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
      Column(modifier = Modifier.padding(8.dp).horizontalScroll(scrollState)) {
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

@Composable
fun TrackListItemWithProfile(
    trackAndProfile: ProfileTrackAssociation,
    selected: Boolean,
    onClick: () -> Unit,
    navigationActions: NavigationActions
) {
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }
  val scrollState = rememberScrollState()
  TrackListItemLaunchedEffect(scope = scope, scrollState = scrollState)

  TrackListItemCard(onClick = onClick, selected = selected) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(8.dp)) {
          Box(
              modifier = Modifier.fillMaxHeight().aspectRatio(1f),
              contentAlignment = Alignment.Center) {
                Image(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Album Cover",
                    modifier = Modifier.fillMaxSize(.8f),
                )
              }
          Column(
              modifier =
                  Modifier.padding(horizontal = 8.dp).weight(1f).horizontalScroll(scrollState)) {
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
          if (trackAndProfile.profile != null) {
            Box(
                modifier =
                    Modifier.size(48.dp)
                        .clickable(
                            enabled = trackAndProfile.profile.isPublic,
                            onClick = {
                              if (trackAndProfile.profile.isPublic) {
                                navigationActions.navigateToProfile(
                                    trackAndProfile.profile.firebaseUid)
                              } else {
                                scope.launch {
                                  snackbarHostState.showSnackbar(
                                      "This profile is private, you cannot access profile information.")
                                }
                              }
                            }),
                contentAlignment = Alignment.Center) {
                  PlaceholderProfilePicture(name = trackAndProfile.profile.firstName)
                }
          }
        }
  }
}

@Composable
internal fun TrackListItemLaunchedEffect(scope: CoroutineScope, scrollState: ScrollState) {
  val slowScrollAnimation: AnimationSpec<Float> = TweenSpec(durationMillis = 5000, easing = { it })
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
}

@Composable
internal fun TrackListItemCard(
    onClick: () -> Unit,
    selected: Boolean,
    content: @Composable () -> Unit
) {
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
        content()
      }
}
