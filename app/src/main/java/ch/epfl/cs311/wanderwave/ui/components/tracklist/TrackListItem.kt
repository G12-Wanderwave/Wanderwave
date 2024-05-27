package ch.epfl.cs311.wanderwave.ui.components.tracklist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.ProfileTrackAssociation
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.components.profile.SelectImage
import ch.epfl.cs311.wanderwave.ui.theme.spotify_green
import ch.epfl.cs311.wanderwave.viewmodel.BeaconViewModel
import ch.epfl.cs311.wanderwave.viewmodel.ProfileViewModel
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

/**
 * Same as [TrackListItem] but with a profile picture on the right side of the track information.
 * Takes a [ProfileTrackAssociation] instead of a [Track].to display profile information, as well as
 * a [NavigationActions] to navigate to the profile view screen.
 */
@Composable
fun TrackListItemWithProfile(
    trackAndProfile: ProfileTrackAssociation,
    profileViewModel: ProfileViewModel,
    beacon: Beacon,
    beaconViewModel: BeaconViewModel,
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
    ) {
      val isLiked = remember {
        mutableStateOf(trackAndProfile.isLiked(profileViewModel.profile.value))
      }
      LikeButton(
          isLiked = isLiked,
          onLike = {
            // Update the profile and track association
            val newTrackProfile = trackAndProfile.likeTrack(profileViewModel.profile.value)
            // Update it in the beacon
            val newBeacon = beacon.updateProfileAndTrackElement(newTrackProfile)
            // Update it on Firebase
            beaconViewModel.updateBeacon(newBeacon)

            // Add liked track to the profile
            profileViewModel.likeTrack(trackAndProfile.track)
            // Update it on Firebase
            profileViewModel.updateProfile(profileViewModel.profile.value)

            // Update UI
            isLiked.value = true
          },
          onUnlike = {
            // Update the profile and track association
            val newTrackProfile = trackAndProfile.unlikeTrack(profileViewModel.profile.value)
            // Update it in the beacon
            val newBeacon = beacon.updateProfileAndTrackElement(newTrackProfile)
            // Update it on Firebase
            beaconViewModel.updateBeacon(newBeacon)

            // Add liked track to the profile
            profileViewModel.unlikeTrack(trackAndProfile.track)
            // Update it on Firebase
            profileViewModel.updateProfile(profileViewModel.profile.value)

            // Update UI
            isLiked.value = false
          })
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
        SelectImage(
            modifier =
                Modifier.size(width = 150.dp, height = 100.dp)
                    .clickable(
                        enabled = trackAndProfile.profile.isPublic,
                        onClick = {
                          if (trackAndProfile.profile.isPublic) {
                            // if the profile is public, navigate to the profile view screen
                            navigationActions.navigateToProfile(trackAndProfile.profile.firebaseUid)
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
            profile = trackAndProfile.profile)
      }
    }
  }
}

@Composable
internal fun LikeButton(
    isLiked: MutableState<Boolean>,
    onLike: () -> Unit,
    onUnlike: () -> Unit,
    modifier: Modifier = Modifier
) {
  Box(
      modifier = Modifier.fillMaxHeight().aspectRatio(1f).padding(start = 8.dp),
      contentAlignment = Alignment.Center) {
        Image(
            imageVector =
                if (isLiked.value) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = "Like Button",
            modifier =
                modifier.fillMaxSize(.65f).clickable {
                  if (isLiked.value) {
                    onUnlike()
                  } else {
                    onLike()
                  }
                },
            colorFilter = ColorFilter.tint(if (isLiked.value) spotify_green else Color.DarkGray))
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

@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun RemovableTrackListItem(
    track: Track,
    selected: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
  val scope = rememberCoroutineScope()
  val scrollState = rememberScrollState()
  TrackListItemLaunchedEffect(scope = scope, scrollState = scrollState)

  val swipeState = rememberSwipeableState(0)
  val swipeDistance = 100f

  Box(
      contentAlignment = Alignment.CenterEnd,
  ) {
    Box(
        modifier =
            Modifier.swipeable(
                    state = swipeState,
                    anchors = mapOf(-swipeDistance to 1, 0f to 0),
                    orientation = Orientation.Horizontal,
                )
                .offset(x = swipeState.offset.value.dp)) {
          TrackListItem(track = track, selected = selected, onClick = onClick)
        }
    AnimatedVisibility(visible = swipeState.offset.value <= -swipeDistance) {
      IconButton(
          onClick = {
            scope.launch { swipeState.snapTo(0) }
            onRemove()
          },
          modifier = Modifier) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.error)
          }
    }
  }
}
