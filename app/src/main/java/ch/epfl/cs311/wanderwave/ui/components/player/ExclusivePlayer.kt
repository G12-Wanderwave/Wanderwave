package ch.epfl.cs311.wanderwave.ui.components.player

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ch.epfl.cs311.wanderwave.R
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import ch.epfl.cs311.wanderwave.ui.theme.orange
import ch.epfl.cs311.wanderwave.ui.theme.pink
import ch.epfl.cs311.wanderwave.ui.theme.spotify_green
import ch.epfl.cs311.wanderwave.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ExclusivePlayer(
    selectedVote: MutableIntState,
    uiState: PlayerViewModel.UiState,
    progress: MutableFloatState
) {
  val viewModel: PlayerViewModel = hiltViewModel()
  Column(
      modifier = Modifier.fillMaxSize().padding(bottom = 84.dp).testTag("exclusivePlayer"),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween) {
        Column(verticalArrangement = Arrangement.Top) {
          PlayerDragHandle(duration1 = 1500, duration2 = 1500, duration3 = 1500, startColor = pink)
          Spacer(modifier = Modifier.height(10.dp))
          // TODO : show image of the album
        }
      Column(verticalArrangement = Arrangement.Bottom) {
        TrackInfoComponent(uiState)
        Spacer(modifier = Modifier.height(10.dp))
        SliderComponent(progress)
        Spacer(modifier = Modifier.height(10.dp))
        PlayerControlRowComponent(viewModel, uiState)
      }
      }
}


@Composable
fun TrackInfoComponent(uiState: PlayerViewModel.UiState) {
  val scrollState = rememberScrollState()
  val coroutineScope = rememberCoroutineScope()

  val slowScrollAnimation: AnimationSpec<Float> = TweenSpec(durationMillis = 10000, easing = FastOutSlowInEasing)

  LaunchedEffect(key1 = true) {
    coroutineScope.launch {
      while (true) {
        delay(1000) // delay before scroll starts
        scrollState.animateScrollTo(scrollState.maxValue, slowScrollAnimation) // scroll to end
        delay(1000) // delay at the end of scroll
        scrollState.animateScrollTo(0, slowScrollAnimation) // scroll back to start
      }
    }
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .horizontalScroll(scrollState)
      .padding(10.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text(
      text = uiState.track?.artist ?: "",
      style = MaterialTheme.typography.titleSmall,
      modifier = Modifier.padding(10.dp)
    )
    Text(
      text = uiState.track?.title ?: "",
      style = MaterialTheme.typography.titleMedium,
      modifier = Modifier.padding(10.dp)
    )
  }
}

@Composable
fun SliderComponent(progress: MutableFloatState) {
  Slider(
      value = progress.floatValue,
      onValueChange = { progress.floatValue = it },
      modifier = Modifier.padding(horizontal = 30.dp),
      colors =
          SliderColors(
              thumbColor = MaterialTheme.colorScheme.onSurface,
              activeTrackColor = spotify_green,
              activeTickColor = spotify_green,
              inactiveTrackColor = MaterialTheme.colorScheme.onSurface,
              inactiveTickColor = MaterialTheme.colorScheme.onSurface,
              disabledThumbColor = spotify_green,
              disabledActiveTrackColor = spotify_green,
              disabledActiveTickColor = spotify_green,
              disabledInactiveTrackColor = spotify_green,
              disabledInactiveTickColor = spotify_green))
}

@Composable
fun PlayerControlRowComponent(viewModel: PlayerViewModel, uiState: PlayerViewModel.UiState) {
  Row(
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()) {
        ShuffleButton(viewModel, uiState)
        PlayerIconButton(
            onClick = { viewModel.skipBackward() },
            testTag = "previousButton",
            painterId = R.drawable.previous_track_icon,
            tint = MaterialTheme.colorScheme.onSurface)
        PlayPauseButton(viewModel, uiState)
        PlayerIconButton(
            onClick = { viewModel.skipForward() },
            testTag = "nextButton",
            painterId = R.drawable.next_track_icon,
            tint = MaterialTheme.colorScheme.onSurface)
        RepeatButton(viewModel, uiState)
      }
}

@Composable
fun ShuffleButton(viewModel: PlayerViewModel, uiState: PlayerViewModel.UiState) {
  IconButton(
      onClick = { viewModel.toggleShuffle() }, modifier = Modifier.testTag("toggleShuffle")) {
        if (!uiState.isShuffling) {
          Icon(
              painter = painterResource(id = R.drawable.shuffle_off_icon),
              contentDescription = "",
              tint = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.size(30.dp))
        } else {
          Icon(
              painter = painterResource(id = R.drawable.shuffle_on_icon),
              contentDescription = "",
              tint = spotify_green,
              modifier = Modifier.size(30.dp))
        }
      }
}

@Composable
fun PlayPauseButton(viewModel: PlayerViewModel, uiState: PlayerViewModel.UiState) {
  IconButton(
      onClick = { if (uiState.isPlaying) viewModel.pause() else viewModel.resume() },
      modifier = Modifier.size(70.dp)) {
        if (uiState.isPlaying) {
          Icon(
              painter = painterResource(id = R.drawable.pause_icon),
              contentDescription = "",
              tint = spotify_green,
              modifier = Modifier.size(55.dp))
        } else {
          Icon(
              painter = painterResource(id = R.drawable.play_icon),
              contentDescription = "",
              tint = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.size(70.dp))
        }
      }
}

@Composable
fun RepeatButton(viewModel: PlayerViewModel, uiState: PlayerViewModel.UiState) {
  IconButton(onClick = { viewModel.toggleRepeat() }, modifier = Modifier.testTag("toggleRepeat")) {
    when (uiState.repeatMode) {
      SpotifyController.RepeatMode.OFF ->
          Icon(
              painter = painterResource(id = R.drawable.repeat_icon),
              contentDescription = "",
              tint = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.size(30.dp))
      SpotifyController.RepeatMode.ALL ->
          Icon(
              painter = painterResource(id = R.drawable.repeat_icon),
              contentDescription = "",
              tint = spotify_green,
              modifier = Modifier.size(30.dp))
      SpotifyController.RepeatMode.ONE ->
          Icon(
              painter = painterResource(id = R.drawable.repeat_one_icon),
              contentDescription = "",
              tint = spotify_green,
              modifier = Modifier.size(30.dp))
    }
  }
}

@Composable
private fun PlayerIconButton(onClick: () -> Unit, testTag: String, painterId: Int, tint: Color) {
  IconButton(onClick = onClick, modifier = Modifier.testTag(testTag)) {
    Icon(
        painter = painterResource(id = painterId),
        contentDescription = "",
        tint = tint,
        modifier = Modifier.size(50.dp))
  }
}
