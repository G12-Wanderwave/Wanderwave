package ch.epfl.cs311.wanderwave.model.localDb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AuthTokenDao {

  @Query("SELECT * FROM auth_tokens WHERE type = :type LIMIT 1")
  fun getAuthToken(type: Int): AuthTokenEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE) fun setAuthToken(authToken: AuthTokenEntity)

  @Query("DELETE FROM auth_tokens WHERE type = :type") fun deleteAuthToken(type: Int)
}
