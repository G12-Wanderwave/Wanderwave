package ch.epfl.cs311.wanderwave.model.localDb

import androidx.room.ColumnInfo
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomDatabase

@Database(entities = [PlaceHolderEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {}

// place holder entity
@Entity(tableName = "beacons")
data class PlaceHolderEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: Double,
)
