package app.bawn.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_security")
data class UserSecurityEntity(
    @PrimaryKey val id: Int = 0, // Always 0, ensures only one PIN exists
    val pinHash: String // We store the HASH, not the raw PIN (Security 101)
)