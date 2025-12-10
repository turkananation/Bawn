package app.bawn.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LockedAppDao {
    @Query("SELECT * FROM locked_apps")
    fun getAllLockedApps(): Flow<List<LockedAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun lockApp(app: LockedAppEntity)

    @Query("DELETE FROM locked_apps WHERE packageName = :packageName")
    suspend fun unlockApp(packageName: String)
}