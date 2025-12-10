package app.bawn.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserSecurityDao {
    // Get the single stored PIN hash
    @Query("SELECT pinHash FROM user_security WHERE id = 0")
    suspend fun getPinHash(): String?

    // Save or Update the PIN
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePin(security: UserSecurityEntity)

    // Check if a PIN exists (for setup screens)
    @Query("SELECT EXISTS(SELECT 1 FROM user_security WHERE id = 0)")
    suspend fun hasPin(): Boolean
}