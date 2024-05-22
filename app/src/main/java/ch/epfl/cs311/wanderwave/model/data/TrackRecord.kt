package ch.epfl.cs311.wanderwave.model.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "track_records")
data class TrackRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Unique ID for each record
    val beaconId: String, // ID of the beacon to which the track was added
    val trackId: String, // ID of the track added to the beacon
    val timestamp: Long // Timestamp should be passed when creating an instance
)
