package ch.epfl.cs311.wanderwave

import android.app.Application
import com.google.android.gms.maps.model.BitmapDescriptor
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp class Wanderwave : Application()

object AppResources {
    var beaconIcon: BitmapDescriptor? = null
}