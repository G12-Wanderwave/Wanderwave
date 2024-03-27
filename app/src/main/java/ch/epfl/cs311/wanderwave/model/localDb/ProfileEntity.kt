package ch.epfl.cs311.wanderwave.model.localDb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
  @PrimaryKey val id: Int, // Assuming a primary key is required, adjust as needed
  @ColumnInfo(name = "first_name") val firstName: String,
  @ColumnInfo(name = "last_name") val lastName: String,
  @ColumnInfo(name = "description") val description: String,
  @ColumnInfo(name = "number_of_likes") val numberOfLikes: Int,
  @ColumnInfo(name = "is_public") val isPublic: Boolean,
  @ColumnInfo(name = "profile_picture_uri") val profilePictureUri: String?, // Assuming Uri is stored as String
  @ColumnInfo(name = "spotify_uid") val spotifyUid: String,
  @ColumnInfo(name = "firebase_uid") val firebaseUid: String
)