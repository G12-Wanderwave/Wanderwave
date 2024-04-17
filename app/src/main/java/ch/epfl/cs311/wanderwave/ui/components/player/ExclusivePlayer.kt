package ch.epfl.cs311.wanderwave.ui.components.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import ch.epfl.cs311.wanderwave.ui.theme.spotify_green
import ch.epfl.cs311.wanderwave.viewmodel.RepeatMode
import ch.epfl.cs311.wanderwave.viewmodel.TrackListViewModel

@Composable
fun ExclusivePlayer(
    checked: MutableState<Boolean>,
    selectedVote: MutableIntState,
    uiState: TrackListViewModel.UiState,
    progress: MutableFloatState
) {
  val viewModel: TrackListViewModel = hiltViewModel()
  Column(
      modifier = Modifier.fillMaxSize().padding(bottom = 84.dp).testTag("exclusivePlayer"),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween) {
        if (checked.value) {
          PlayerDragHandle(
              duration1 = 1500, duration2 = 1500, duration3 = 1500, startColor = Color.Cyan)
        } else {
          PlayerDragHandle(
              duration1 = 4000, duration2 = 3000, duration3 = 2000, startColor = spotify_green)
        }
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
                ),
        )
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()) {
              IconButton(onClick = { /*TODO*/}, modifier = Modifier.testTag("broadcastButton")) {
                Icon(
                    painter = painterResource(id = R.drawable.broadcast_icon),
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(50.dp))
              }
              IconButton(onClick = { /*TODO*/}, modifier = Modifier.testTag("beaconButton")) {
                Icon(
                    painter = painterResource(id = R.drawable.beacon_add_icon),
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(50.dp))
              }
              IconButton(onClick = { /*TODO*/}, modifier = Modifier.testTag("playlistButton")) {
                Icon(
                    painter = painterResource(id = R.drawable.playlist_add_icon),
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(50.dp))
              }
              IconButton(onClick = { /*TODO*/}, modifier = Modifier.testTag("ignoreButton")) {
                Icon(
                    painter = painterResource(id = R.drawable.ignore_list_icon),
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(50.dp))
              }
            }
        VotingButtons(selectedVote) { vote -> selectedVote.intValue = vote }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
              Box(modifier = Modifier.size(250.dp).background(Color.LightGray))
            }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
              Text(
                  text = uiState.selectedTrack?.artist ?: "",
                  style = MaterialTheme.typography.titleSmall)
              Text(
                  text = uiState.selectedTrack?.title ?: "",
                  style = MaterialTheme.typography.titleMedium)
            }
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
                    disabledInactiveTickColor = spotify_green),
        )

        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()) {
              IconButton(
                  onClick = { viewModel.toggleShuffle() },
                  modifier = Modifier.testTag("toggleShuffle")) {
                    if (!uiState.shuffleOn) {
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
              IconButton(onClick = { /*TODO*/}) {
                Icon(
                    painter = painterResource(id = R.drawable.previous_track_icon),
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(50.dp))
              }
              IconButton(
                  onClick = { if (uiState.isPlaying) viewModel.pause() else viewModel.play() },
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
              IconButton(onClick = { /*TODO*/}) {
                Icon(
                    painter = painterResource(id = R.drawable.next_track_icon),
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(50.dp))
              }
              IconButton(
                  onClick = { viewModel.toggleRepeat() },
                  modifier = Modifier.testTag("toggleRepeat")) {
                    when (uiState.repeatMode) {
                      RepeatMode.NONE ->
                          Icon(
                              painter = painterResource(id = R.drawable.repeat_icon),
                              contentDescription = "",
                              tint = MaterialTheme.colorScheme.onSurface,
                              modifier = Modifier.size(30.dp))
                      RepeatMode.ALL ->
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
      }
}
