package ch.epfl.cs311.wanderwave.model.localDb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Represents tokens used for authentication */
@Entity(tableName = "auth_tokens", indices = [Index(value = ["type"], unique = true)])
data class AuthTokenEntity(
    /** The token used for authenticating to something */
    @PrimaryKey @ColumnInfo(name = "token") var token: String,
    /** The unix timestamp when the token expires */
    @ColumnInfo(name = "expiration_date") var expirationDate: Long,
    /** The type of the token */
    @ColumnInfo(name = "type") var type: Int
)
