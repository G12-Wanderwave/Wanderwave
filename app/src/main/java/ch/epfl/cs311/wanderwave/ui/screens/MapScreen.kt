package ch.epfl.cs311.wanderwave.ui.screens

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import ch.epfl.cs311.wanderwave.R
import ch.epfl.cs311.wanderwave.viewmodel.MapViewModel
import ch.epfl.cs311.wanderwave.viewmodel.ProfileViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.compose.GoogleMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


@Preview
@Composable
fun MapScreen() {
  val context = LocalContext.current
  val isMapReady = remember { mutableStateOf(false) }
  val viewModel: MapViewModel = hiltViewModel()
  val googleMapState = viewModel.googleMapState.collectAsState().value
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    AndroidView(
        factory = { ctx ->
          MapView(ctx).apply {
            onCreate(Bundle())
            onResume() 
            getMapAsync { googleMap ->
              viewModel.setGoogleMap(googleMap)
              isMapReady.value = true
              googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style))
            }
          }
        },
        modifier = Modifier.fillMaxSize())

    if (!isMapReady.value) {
      CircularProgressIndicator()
    } else {
      googleMapState?.let { googleMap ->
        val epfl = LatLng(46.518831258 , 6.559331096)
        googleMap.addMarker(MarkerOptions().position(epfl).title("Marker at EPFL"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(epfl))
      }
    }
  }
}




//class MyMapActivity : AppCompatActivity(), OnMapReadyCallback {
//  private var mMap: GoogleMap? = null
//
//  override fun onCreate(savedInstanceState: Bundle?) {
//    super.onCreate(savedInstanceState)
//    setContentView(R.layout.fragment_map) // Ensure this is your activity's layout
//
//    // Instantiate a MapFragment using GoogleMapOptions
//    val mapFragment = MapFragment.newInstance(
//      GoogleMapOptions()
//        .mapId(resources.getString(R.string.map_id)) // Ensure you have a map_id in your strings.xml
//    )
//
//    // Add the MapFragment to the Activity
//    fragmentManager.beginTransaction().add(R.id.map_container, mapFragment).commit()
//
//    // Set this activity as the callback for when the map is ready
//    mapFragment.getMapAsync(this)
//  }
//
//  override fun onMapReady(googleMap: GoogleMap) {
//    mMap = googleMap
//
//    // Customize the map with a custom map style
//    //googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json)) // Assuming you have a style_json in res/raw
//
//    // Add a marker in Sydney and move the camera
//    val sydney = LatLng(-34.858, 151.211)
//    mMap?.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//    mMap?.moveCamera(CameraUpdateFactory.newLatLng(sydney))
//  }
//}