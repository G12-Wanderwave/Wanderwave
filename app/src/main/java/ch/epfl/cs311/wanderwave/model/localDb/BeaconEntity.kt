package ch.epfl.cs311.wanderwave.model.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "beacons")
data class BeaconEntity (
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: Double,
)