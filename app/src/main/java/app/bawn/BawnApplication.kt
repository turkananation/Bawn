package app.bawn

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import app.bawn.data.AppDatabase
import app.bawn.ui.LockScreenActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BawnApplication : Application(), Application.ActivityLifecycleCallbacks {

    val database by lazy { AppDatabase.getDatabase(this) }

    // Scope for background DB checks
    private val applicationScope = CoroutineScope(Dispatchers.IO)

    // Lock Logic State
    private var backgroundTime: Long = 0
    private var isUnlockPending = false

    // 30 seconds grace period (adjust as needed)
    private val GRACE_PERIOD_MS = 30000L

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityResumed(activity: Activity) {
        // 1. Prevent Infinite Loop: Never lock the LockScreen itself
        if (activity is LockScreenActivity) return

        // 2. Check if we need to lock (Async because DB access is required)
        applicationScope.launch {
            if (shouldShowLockScreen()) {
                // Switch to Main thread to launch UI
                launch(Dispatchers.Main) {
                    isUnlockPending = true
                    val intent = Intent(activity, LockScreenActivity::class.java)
                    intent.putExtra("TARGET_PACKAGE", packageName)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    activity.startActivity(intent)
                }
            }
        }
    }

    override fun onActivityPaused(activity: Activity) {
        // Record time ONLY if we aren't currently unlocking
        if (!isUnlockPending) {
            backgroundTime = System.currentTimeMillis()
        }
    }

    private suspend fun shouldShowLockScreen(): Boolean {
        // A. Check if user has actually set a PIN in your Room DB
        val hasPin = database.userSecurityDao().hasPin()
        if (!hasPin) return false

        // B. Check if Grace Period expired
        val timeElapsed = System.currentTimeMillis() - backgroundTime
        val isGracePeriodExpired = timeElapsed > GRACE_PERIOD_MS

        return isGracePeriodExpired && !isUnlockPending
    }

    /**
     * Called by LockScreenActivity when PIN is correct
     */
    fun onUserUnlocked() {
        isUnlockPending = false
        backgroundTime = System.currentTimeMillis() // Reset timer
    }

    // --- Required Boilerplate ---
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}