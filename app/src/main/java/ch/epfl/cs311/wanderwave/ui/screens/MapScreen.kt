package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.google.maps.android.compose.GoogleMap

@Composable
@Preview
fun MapScreen() {
  GoogleMap(
    modifier = Modifier.testTag("mapScreen")
  )
}