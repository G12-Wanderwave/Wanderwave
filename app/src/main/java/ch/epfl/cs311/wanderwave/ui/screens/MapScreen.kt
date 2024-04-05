package ch.epfl.cs311.wanderwave.ui.screens

import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Preview
@Composable
fun MapScreen(mapViewModel: MapViewModel = MapViewModel()) {
    val googleMap by mapViewModel.googleMapStateFlow.collectAsState()

    LaunchedEffect(true) {
        mapViewModel.temp()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (googleMap != null) {
            Text("Map is ready!")
        } else {
            CircularProgressIndicator()
        }
    }

}

class MapViewModel : androidx.lifecycle.ViewModel() {
    private var _googleMapStateFlow = MutableStateFlow<GoogleMap?>(null)
    val googleMapStateFlow: StateFlow<GoogleMap?> = _googleMapStateFlow

    fun temp() {
        var mapFragment = MapFragment.newInstance(GoogleMapOptions().mapId("a979ff520b0186bb"))
        Log.d("Test32","Test")
        mapFragment.getMapAsync { googleMap ->
            _googleMapStateFlow.value = googleMap

        }
    }
}
