package ch.epfl.cs311.wanderwave.ui.screens

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.ViewModel
import ch.epfl.cs311.wanderwave.R
import com.google.android.gms.maps.CameraUpdateFactory

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.IndoorBuilding
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.compose.GoogleMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/*
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_map) // Make sure this is your layout file

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }
}
*/
@Preview
@Composable
fun MapScreen(viewModel: MapViewModel = MapViewModel()) {
    val context = LocalContext.current
    val mapId = stringResource(id = R.string.map_id) // Your map ID
    val isMapReady = remember { mutableStateOf(false) }
    val googleMapState = viewModel.googleMapState.collectAsState().value

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AndroidView(
            factory = { ctx ->
                // Initialize the GoogleMapOptions with your map ID
                val options = GoogleMapOptions().mapId(mapId)
                Log.d("MapScreen", options.mapId.toString())
                MapView(ctx, options).apply {
                    onCreate(Bundle())
                    onResume() // Remember to manage the full lifecycle as mentioned previously
                    getMapAsync { googleMap ->
                        viewModel.setGoogleMap(googleMap) // Update the ViewModel with the GoogleMap
                        isMapReady.value = true
                        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style))
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (!isMapReady.value) {
            CircularProgressIndicator()
        } else {
            // Example usage: Display a marker once the map is ready and googleMapState is not null

            googleMapState?.let { googleMap ->
                // Assume we want to place a marker at a specific location
                val sydney = LatLng(-34.0, 151.0)
                googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
            }
        }
    }
}

class MapViewModel : ViewModel() {
    private var _googleMapState = MutableStateFlow<GoogleMap?>(null)
    val googleMapState: StateFlow<GoogleMap?> = _googleMapState

    fun setGoogleMap(googleMap: GoogleMap) {
        _googleMapState.value = googleMap
    }
}