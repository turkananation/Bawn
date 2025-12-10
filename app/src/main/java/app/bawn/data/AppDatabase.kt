package app.bawn.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// 1. Add UserSecurityEntity to entities list
// 2. Bump version from 1 to 2
@Database(entities = [LockedAppEntity::class, UserSecurityEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun lockedAppDao(): LockedAppDao
    abstract fun userSecurityDao(): UserSecurityDao // <-- Add this

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bawn_database"
                )
                    // 3. Since we are in dev, we can destroy old data to rebuild schema.
                    // In production, you would write a migration.
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}