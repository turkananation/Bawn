package app.bawn

import android.app.Application
import app.bawn.data.AppDatabase

class BawnApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
}