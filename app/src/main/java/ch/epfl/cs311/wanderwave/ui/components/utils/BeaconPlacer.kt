package ch.epfl.cs311.wanderwave.ui.components.utils

import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import com.google.android.gms.maps.model.LatLng
import kotlin.math.*
import kotlin.random.Random

private const val EARTH_RADIUS_M = 6371000.0
private const val BEACON_RADIUS = 1000.0
private const val BEACON_COUNT = 20
private const val NUMBER_ITERATION = 5
fun placeBeaconsRandomly( beacons:List<Beacon>,location: LatLng):List<Beacon>{
    var finalBeacons = mutableListOf<Beacon>()
    var nearbyBeacons = findNearbyBeacons(location,beacons, BEACON_RADIUS)
    if ( nearbyBeacons.size< BEACON_COUNT) {
        var currentMaxDistance = 0.0
        repeat (NUMBER_ITERATION){
            val newBeacons = mutableListOf<Beacon>()
            repeat(BEACON_COUNT - nearbyBeacons.size) {
                findRandomBeacon(location, newBeacons, it)
            }
            var newMaxDistance = computeDistanceBetweenBeacons(newBeacons, beacons)
            if(newMaxDistance>currentMaxDistance || finalBeacons.isEmpty()){
                currentMaxDistance = newMaxDistance
                finalBeacons = newBeacons
            }
        }
        //TODO: add the beacons to the repo or firebase or whatever
        //These are the beacons that are to be added : finalBeacons
    }

    return finalBeacons

}
fun computeDistanceBetweenBeacons(newBeacons: MutableList<Beacon>, beacons: List<Beacon>):Double{
    var distance = 0.0
    newBeacons.forEach { beacon ->
        beacons.forEach{ existingBeacon ->
            distance+= haversine(beacon.location.toLatLng(), existingBeacon.location.toLatLng())
        }
    }
    return distance
}
fun findRandomBeacon(location: LatLng, newBeacons: MutableList<Beacon>, it: Int) {
    val randomLocation = randomLatLongFromPosition(
        location,
        BEACON_RADIUS
    )
    val newBeacon = Beacon(
        id = "beacon${newBeacons.size + it}",//TODO: modify the ID!!!!!!!!
        location = Location(randomLocation.latitude, randomLocation.longitude)
    )
    newBeacons.add(newBeacon)
}
fun haversine(position1:LatLng, position2:LatLng): Double {
    val latDistance = Math.toRadians(position2.latitude - position1.latitude)
    val lonDistance = Math.toRadians(position2.longitude - position1.longitude)
    val a = sin(latDistance / 2).pow(2) +
            (cos(Math.toRadians(position1.latitude)) * cos(Math.toRadians(position2.latitude)) *
                    sin(lonDistance / 2).pow(2))
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return EARTH_RADIUS_M * c
}

fun findNearbyBeacons(userPosition: LatLng, beacons:List<Beacon>,  radius: Double): List<Beacon> {
    var nearbyBeacons = mutableListOf<Beacon>()
    beacons.forEach { beacon-> if( (haversine(userPosition,beacon.location.toLatLng()))<radius){ nearbyBeacons+=beacon } }
    return nearbyBeacons
}

fun randomLatLongFromPosition(userPosition: LatLng, distance: Double): LatLng{
    val latRad = Math.toRadians(userPosition.latitude)
    val lonRad = Math.toRadians(userPosition.longitude)

    val bearing = Random.nextDouble(0.0, 2 * Math.PI)
    val angularDistance = Random.nextDouble(distance) / EARTH_RADIUS_M

    // New latitude in radians
    val newLat = asin(sin(latRad) * cos(angularDistance) +
            cos(latRad) * sin(angularDistance) * cos(bearing))

    // New longitude in radians
    val newLon = lonRad + atan2(sin(bearing) * sin(angularDistance) * cos(latRad),
        cos(angularDistance) - sin(latRad) * sin(newLat))

    val newLatDeg = Math.toDegrees(newLat)
    val newLonDeg = Math.toDegrees(newLon)

    return LatLng(newLatDeg, newLonDeg)
}