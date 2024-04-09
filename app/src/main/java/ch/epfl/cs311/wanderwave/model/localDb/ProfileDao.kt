package ch.epfl.cs311.wanderwave.model.localDb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProfileDao {
  @Query("SELECT * FROM profiles LIMIT 1") suspend fun getProfile(): ProfileEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertProfile(profile: ProfileEntity)

  @Delete suspend fun deleteProfile(profile: ProfileEntity)
}
