package ch.epfl.cs311.wanderwave.ui.screens

import android.content.ContentValues
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.epfl.cs311.wanderwave.R
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.local.BeaconEntity
import ch.epfl.cs311.wanderwave.viewmodel.MapViewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState

@Composable
@Preview
fun MapScreen() {
  val viewModel : MapViewModel = hiltViewModel()
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  addMockBeacons()
  GoogleMap() {
    DisplayBeacons(uiState.beacons)
  }
}

/**
 * This is a Composable function that displays the beacons on the map.
 * It takes a list of beacons as input and adds a marker for each beacon on the map.
 *
 * @param beacons The list of beacons to be displayed on the map.
 */
@Composable
fun DisplayBeacons(beacons: List<Beacon>) {

  // Add a marker for each beacon
  //source for the icon: https://www.svgrepo.com/svg/448258/waypoint
  val customIcon = BitmapDescriptorFactory.fromResource(R.drawable.waypoint)
  beacons.forEach() {
    Marker(
      state = MarkerState(position = it.location.toLatLng()),
      title = it.id,
      icon = customIcon,
    )
  }
}
fun addMockBeacons() {
  val db = Firebase.firestore
  val collection = db.collection("beacons")

  val latitudes = listOf(46.51857556996283, 46.51857417773428, 46.52298529087412, 46.51846723837138)
  val longitudes = listOf(6.5631609607190775, 6.5619195033506434, 6.564644391110982, 6.568149323030634)
  val names = listOf("INM", "BC", "STCC", "RLC")

  for (i in 0..4) {
    val beacon = BeaconEntity(names[i], latitudes[i], longitudes[i])
    val beaconDoc = hashMapOf(
      "name" to beacon.id,
      "latitude" to beacon.latitude,
      "longitude" to beacon.longitude
    )
    collection
      .add(beaconDoc)
      .addOnSuccessListener { documentReference ->
        Log.d(ContentValues.TAG, "DocumentSnapshot written with ID: ${documentReference.id}")
      }
      .addOnFailureListener { e -> Log.w(ContentValues.TAG, "Error adding document", e) }
  }
}