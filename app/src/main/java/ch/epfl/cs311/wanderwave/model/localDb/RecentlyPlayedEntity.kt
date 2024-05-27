package ch.epfl.cs311.wanderwave.model.localDb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Represents a track that was recently played */
@Entity(tableName = "recently_played")
data class RecentlyPlayedEntity(
    /** The track id of the track that was played */
    @PrimaryKey @ColumnInfo(name = "track_id") var trackId: String,

    /** Unix timestamp of when the track was last played */
    @ColumnInfo(name = "last_played") var lastPlayed: Long
)
