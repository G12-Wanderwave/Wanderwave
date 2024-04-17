package ch.epfl.cs311.wanderwave.ui.components.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.epfl.cs311.wanderwave.R
import ch.epfl.cs311.wanderwave.ui.theme.orange
import ch.epfl.cs311.wanderwave.ui.theme.spotify_green
import ch.epfl.cs311.wanderwave.viewmodel.TrackListViewModel
import javax.inject.Singleton
import kotlinx.coroutines.delay

@Singleton
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurroundWithMiniPlayer(
    displayPlayer: Boolean,
    viewModel: TrackListViewModel = hiltViewModel(),
    screen: @Composable () -> Unit
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val sheetState = rememberStandardBottomSheetState(initialValue = SheetValue.PartiallyExpanded)
  val progress = remember { mutableFloatStateOf(0f) }
  val selectedVote = remember { mutableIntStateOf(0) }
  val checked = remember { mutableStateOf(false) }
  HandleSheetStateChanges(sheetState = sheetState, uiState = uiState, viewModel = viewModel)

  HandleProgressChanges(uiState = uiState, progress = progress)

  BottomSheetScaffold(
      sheetContent = {
        if (!uiState.expanded && sheetState.hasPartiallyExpandedState && displayPlayer) {
          MiniPlayer(
              isPlaying = uiState.isPlaying,
              onTitleClick = { viewModel.expand() },
              onPlayClick = { viewModel.play() },
              onPauseClick = { viewModel.pause() },
              progress = progress.floatValue)
        } else {
          ExclusivePlayer(
              checked = checked,
              selectedVote = selectedVote,
              uiState = uiState,
              progress = progress)
        }
      },
      sheetShape = RectangleShape,
      sheetContainerColor = MaterialTheme.colorScheme.background.copy(alpha = .85f),
      sheetDragHandle = {
        if (!uiState.expanded && sheetState.hasPartiallyExpandedState && displayPlayer) {
          Column(
              modifier =
                  Modifier.background(
                          if (!uiState.isPlaying) MaterialTheme.colorScheme.primary
                          else spotify_green)
                      .fillMaxWidth()
                      .height(2.dp)
                      .clickable {}) {}
        }
      },
      scaffoldState =
          BottomSheetScaffoldState(
              bottomSheetState = sheetState, snackbarHostState = SnackbarHostState()),
      sheetPeekHeight = if (displayPlayer) 144.dp else 0.dp) {
        screen()
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HandleSheetStateChanges(
    sheetState: SheetState,
    uiState: TrackListViewModel.UiState,
    viewModel: TrackListViewModel
) {
  LaunchedEffect(uiState.expanded) {
    if (uiState.expanded) {
      sheetState.expand()
    } else {
      sheetState.partialExpand()
    }
  }

  LaunchedEffect(sheetState.currentValue) {
    when (sheetState.currentValue) {
      SheetValue.Expanded -> viewModel.expand()
      SheetValue.PartiallyExpanded -> viewModel.collapse()
      else -> {
        viewModel.collapse()
      }
    }
  }
}

// A placeholder
@Composable
fun HandleProgressChanges(uiState: TrackListViewModel.UiState, progress: MutableFloatState) {
  LaunchedEffect(uiState.isPlaying) {
    if (uiState.isPlaying) {
      while (true) {
        delay(1000L)
        progress.floatValue += 0.01f
        if (progress.floatValue >= 1f) {
          progress.floatValue = 0f
        }
      }
    }
  }
}

@Composable
fun VotingButtons(selectedVote: MutableState<Int>, onVoteSelected: (Int) -> Unit) {
  Row(
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()) {
        IconButton(onClick = { onVoteSelected(-2) }, Modifier.size(20.dp)) {
          Icon(
              painter = painterResource(id = R.drawable.downvote_two_icon),
              contentDescription = "",
              tint =
                  if (selectedVote.value == -2) Color.DarkGray
                  else MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.size(20.dp))
        }
        IconButton(onClick = { onVoteSelected(-1) }, modifier = Modifier.size(20.dp)) {
          Icon(
              painter = painterResource(id = R.drawable.downvote_one_icon),
              contentDescription = "",
              tint =
                  if (selectedVote.value == -1) Color.Gray else MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.size(20.dp))
        }
        IconButton(onClick = { onVoteSelected(1) }, Modifier.size(20.dp)) {
          Icon(
              painter = painterResource(id = R.drawable.upvote_one_icon),
              contentDescription = "",
              tint = if (selectedVote.value == 1) orange else MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.size(20.dp))
        }
        IconButton(onClick = { onVoteSelected(2) }, Modifier.size(20.dp)) {
          Icon(
              painter = painterResource(id = R.drawable.upvote_two_icon),
              contentDescription = "",
              tint =
                  if (selectedVote.value == 2) Color.Red else MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.size(20.dp))
        }
      }
}
