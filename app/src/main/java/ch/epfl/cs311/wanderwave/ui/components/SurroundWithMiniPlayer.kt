package ch.epfl.cs311.wanderwave.ui.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.epfl.cs311.wanderwave.R
import ch.epfl.cs311.wanderwave.navigation.Route
import ch.epfl.cs311.wanderwave.ui.components.animated.FlexibleRectangle
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
          BoxWithConstraints {
            val boxWidth = this.maxWidth
            Column(
                modifier =
                    Modifier.height(63.dp)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = .7f)),
                verticalArrangement = Arrangement.SpaceAround,
                horizontalAlignment = Alignment.Start) {
                  Row(
                      verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Icon(
                        painter = painterResource(id = R.drawable.play_icon),
                        contentDescription = "Play icon",
                        modifier = Modifier.width(12.dp),
                        tint = MaterialTheme.colorScheme.primary)
                    FlexibleRectangle(
                        startWidth = 12f,
                        endWidth = boxWidth.value - 20f,
                        startHeight = 12f,
                        endHeight = 3f,
                        startColor = Color(0xFF1DB954),
                        endColor = MaterialTheme.colorScheme.primary,
                        durationMillis = 4500,
                        isFiredOnce = false,
                        easing = LinearOutSlowInEasing)
                    Icon(
                        painter = painterResource(id = R.drawable.wanderwave_icon),
                        contentDescription = "Primary Wanderwave icon",
                        modifier = Modifier.width(9.dp),
                        tint = MaterialTheme.colorScheme.primary)
                  }
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.stop_icon),
                        contentDescription = "Stop icon",
                        modifier = Modifier.width(12.dp),
                        tint = Color(0xFFFFA500))
                    FlexibleRectangle(
                        startWidth = 12f,
                        endWidth = boxWidth.value - 20f,
                        startHeight = 12f,
                        endHeight = 3f,
                        startColor = Color(0xFF1DB954),
                        endColor = Color(0xFFFFA500),
                        durationMillis = 4000,
                        isFiredOnce = false,
                        easing = LinearOutSlowInEasing)
                    Icon(
                        painter = painterResource(id = R.drawable.wanderwave_icon),
                        contentDescription = "Orange Wanderwave icon",
                        modifier = Modifier.width(9.dp),
                        tint = Color(0xFFFFA500))
                  }
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.record_icon),
                        contentDescription = "Record icon",
                        modifier = Modifier.width(12.dp),
                        tint = Color.Red)
                    FlexibleRectangle(
                        startWidth = 12f,
                        endWidth = boxWidth.value - 20f,
                        startHeight = 12f,
                        endHeight = 3f,
                        startColor = Color(0xFF1DB954),
                        endColor = Color.Red,
                        durationMillis = 3500,
                        isFiredOnce = false,
                        easing = LinearOutSlowInEasing)
                    Icon(
                        painter = painterResource(id = R.drawable.wanderwave_icon),
                        contentDescription = "Red Wanderwave icon",
                        modifier = Modifier.width(9.dp),
                        tint = Color.Red)
                  }
                }
          }
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
              Box(modifier = Modifier.size(250.dp).background(Color.LightGray))
            }
      },
      sheetShape = RectangleShape,
      sheetContainerColor = MaterialTheme.colorScheme.background.copy(alpha = .85f),
      sheetDragHandle = {
        if (!uiState.expanded &&
            sheetState.hasPartiallyExpandedState &&
            currentRouteState != Route.LOGIN) {
          Column(
              modifier =
                  Modifier.background(
                          if (!uiState.isPlaying) MaterialTheme.colorScheme.primary
                          else Color(0xFF1DB954))
                      .fillMaxWidth()
                      .height(2.dp)
                      .clickable {}) {}
        }
      },
      scaffoldState =
          BottomSheetScaffoldState(
              bottomSheetState = sheetState, snackbarHostState = SnackbarHostState()),
      sheetPeekHeight =
          if (currentRouteState != Route.LOGIN && currentRouteState != null) 144.dp else 0.dp) {
        screen()
      }
}
