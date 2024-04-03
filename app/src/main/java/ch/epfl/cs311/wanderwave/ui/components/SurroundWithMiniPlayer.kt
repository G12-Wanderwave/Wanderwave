package ch.epfl.cs311.wanderwave.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.epfl.cs311.wanderwave.navigation.Route
import ch.epfl.cs311.wanderwave.ui.components.tracklist.MiniPlayer
import ch.epfl.cs311.wanderwave.viewmodel.TrackListViewModel
import javax.inject.Singleton
import kotlinx.coroutines.delay

@Singleton
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurroundWithMiniPlayer(currentRouteState: Route?, screen: @Composable () -> Unit) {
  val viewModel: TrackListViewModel = hiltViewModel()
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val sheetState = rememberStandardBottomSheetState(initialValue = SheetValue.PartiallyExpanded)
  val progress = remember { mutableFloatStateOf(0f) }
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
      else -> {}
    }
  }

  LaunchedEffect(uiState.isPlaying) {
    if (uiState.isPlaying) {
      while (true) {
        delay(1000L) // delay for 1 second
        progress.floatValue += 0.01f // increment progress by 1%
        if (progress.floatValue >= 1f) {
          progress.floatValue = 0f // reset progress when it reaches 100%
        }
      }
    }
  }

  BottomSheetScaffold(
      modifier = Modifier.background(MaterialTheme.colorScheme.background),
      sheetContent = {
        if (!uiState.expanded &&
            sheetState.hasPartiallyExpandedState &&
            currentRouteState != Route.LOGIN) {
          MiniPlayer(
              isPlaying = uiState.isPlaying,
              onTitleClick = { viewModel.expand() },
              onPlayClick = { viewModel.play() },
              onPauseClick = { viewModel.pause() },
              progress = progress.floatValue)
        } else {
          Column(
              modifier = Modifier.height(63.dp).fillMaxWidth().background(Color.Black),
              verticalArrangement = Arrangement.Center,
              horizontalAlignment = Alignment.CenterHorizontally) {}
        }
        Row(modifier = Modifier.fillMaxHeight()) {
          Button(
              onClick = {
                if (!uiState.isPlaying) {
                  viewModel.pause()
                } else {
                  viewModel.play()
                }
              }) {
                Text(text = "Coming soon.")
              }
        }
      },
      sheetShape = RectangleShape,
      sheetContainerColor = MaterialTheme.colorScheme.secondaryContainer,
      sheetDragHandle = {
        Column(
            modifier =
                Modifier.background(
                        if (!uiState.isPlaying) MaterialTheme.colorScheme.primary
                        else Color(0xFF1DB954))
                    .fillMaxWidth()
                    .height(2.dp)
                    .clickable {}) {}
      },
      scaffoldState =
          BottomSheetScaffoldState(
              bottomSheetState = sheetState, snackbarHostState = SnackbarHostState()),
      sheetPeekHeight = if (currentRouteState != Route.LOGIN) 144.dp else 0.dp) {
        screen()
      }
}
