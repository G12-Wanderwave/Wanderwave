package ch.epfl.cs311.wanderwave.ui.components.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ch.epfl.cs311.wanderwave.R
import ch.epfl.cs311.wanderwave.ui.theme.orange
import ch.epfl.cs311.wanderwave.ui.theme.spotify_green
import ch.epfl.cs311.wanderwave.viewmodel.PlayerViewModel

@Composable
fun ExclusivePlayer(
    checked: MutableState<Boolean>,
    selectedVote: MutableIntState,
    uiState: PlayerViewModel.UiState,
    progress: MutableFloatState
) {
  val viewModel: PlayerViewModel = hiltViewModel()
  Column(
      modifier = Modifier.fillMaxSize().padding(bottom = 84.dp).testTag("exclusivePlayer"),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween) {
        PlayerDragHandleComponent(checked)
        SwitchComponent(checked)
        PlayerIconButtonRowComponent()
        VotingButtonsComponent(selectedVote)
        TrackInfoComponent(uiState)
        SliderComponent(progress)
        PlayerControlRowComponent(viewModel, uiState)
      }
}

@Composable
fun PlayerDragHandleComponent(checked: MutableState<Boolean>) {
  if (checked.value) {
    PlayerDragHandle(duration1 = 1500, duration2 = 1500, duration3 = 1500, startColor = Color.Cyan)
  } else {
    PlayerDragHandle(
        duration1 = 4000, duration2 = 3000, duration3 = 2000, startColor = spotify_green)
  }
}

@Composable
fun SwitchComponent(checked: MutableState<Boolean>) {
  Switch(
      modifier = Modifier.testTag("switch"),
      checked = checked.value,
      onCheckedChange = { checked.value = it },
      colors =
          SwitchColors(
              checkedThumbColor = Color.White,
              checkedTrackColor = MaterialTheme.colorScheme.surface,
              checkedBorderColor = Color.White,
              checkedIconColor = Color.White,
              uncheckedThumbColor = spotify_green,
              uncheckedTrackColor = MaterialTheme.colorScheme.surface,
              uncheckedBorderColor = spotify_green,
              uncheckedIconColor = spotify_green,
              disabledCheckedThumbColor = MaterialTheme.colorScheme.onBackground,
              disabledCheckedTrackColor = MaterialTheme.colorScheme.onBackground,
              disabledCheckedBorderColor = MaterialTheme.colorScheme.onBackground,
              disabledCheckedIconColor = MaterialTheme.colorScheme.onBackground,
              disabledUncheckedThumbColor = MaterialTheme.colorScheme.onBackground,
              disabledUncheckedTrackColor = MaterialTheme.colorScheme.onBackground,
              disabledUncheckedBorderColor = MaterialTheme.colorScheme.onBackground,
              disabledUncheckedIconColor = MaterialTheme.colorScheme.onBackground,
          ))
}

@Composable
fun PlayerIconButtonRowComponent() {
  Row(
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()) {
        PlayerIconButton(
            onClick = {},
            testTag = "broadcastButton",
            painterId = R.drawable.broadcast_icon,
            tint = MaterialTheme.colorScheme.onSurface)
        PlayerIconButton(
            onClick = {},
            testTag = "beaconButton",
            painterId = R.drawable.beacon_add_icon,
            tint = MaterialTheme.colorScheme.onSurface)
        PlayerIconButton(
            onClick = {},
            testTag = "playlistButton",
            painterId = R.drawable.playlist_add_icon,
            tint = MaterialTheme.colorScheme.onSurface)
        PlayerIconButton(
            onClick = {},
            testTag = "ignoreButton",
            painterId = R.drawable.ignore_list_icon,
            tint = MaterialTheme.colorScheme.onSurface)
      }
}

@Composable
fun VotingButtonsComponent(selectedVote: MutableIntState) {
  VotingButtons(selectedVote) { vote -> selectedVote.intValue = vote }
}

@Composable
fun TrackInfoComponent(uiState: PlayerViewModel.UiState) {
  Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center) {
        Text(text = uiState.track?.artist ?: "", style = MaterialTheme.typography.titleSmall)
        Text(text = uiState.track?.title ?: "", style = MaterialTheme.typography.titleMedium)
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
      false ->
          Icon(
              painter = painterResource(id = R.drawable.repeat_icon),
              contentDescription = "",
              tint = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.size(30.dp))
      true ->
          Icon(
              painter = painterResource(id = R.drawable.repeat_icon),
              contentDescription = "",
              tint = spotify_green,
              modifier = Modifier.size(30.dp))
      else ->
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

@Composable
fun VotingButtons(selectedVote: MutableState<Int>, onVoteSelected: (Int) -> Unit) {
  val voteOptions = listOf(-2, -1, 1, 2)
  val icons =
      listOf(
          R.drawable.downvote_two_icon,
          R.drawable.downvote_one_icon,
          R.drawable.upvote_one_icon,
          R.drawable.upvote_two_icon)
  val tints = listOf(Color.DarkGray, Color.Gray, orange, Color.Red)

  Row(
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()) {
        voteOptions.forEachIndexed { index, vote ->
          VotingButton(
              vote = vote,
              selectedVote = selectedVote,
              onVoteSelected = onVoteSelected,
              icon = icons[index],
              tint = tints[index])
        }
      }
}

@Composable
fun VotingButton(
    vote: Int,
    selectedVote: MutableState<Int>,
    onVoteSelected: (Int) -> Unit,
    icon: Int,
    tint: Color
) {
  IconButton(onClick = { onVoteSelected(vote) }, modifier = Modifier.size(20.dp)) {
    Icon(
        painter = painterResource(id = icon),
        contentDescription = "",
        tint = if (selectedVote.value == vote) tint else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.size(20.dp))
  }
}
