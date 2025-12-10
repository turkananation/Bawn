package app.bawn.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.view.accessibility.AccessibilityEvent
import app.bawn.data.AppDatabase
import app.bawn.ui.LockScreenActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BawnAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var lockedPackagesCache = setOf<String>()

    // 1. Create a Receiver to listen for "Screen Off"
    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                // User locked the phone -> Lock everything again
                SessionManager.clearSession()
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        // 2. Register the Receiver
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenOffReceiver, filter)

        val dao = AppDatabase.getDatabase(applicationContext).lockedAppDao()
        serviceScope.launch {
            dao.getAllLockedApps().collect { list ->
                lockedPackagesCache = list.map { it.packageName }.toSet()
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return

            if (lockedPackagesCache.contains(packageName)) {
                if (!SessionManager.isUnlocked(packageName)) {
                    val intent = Intent(this, LockScreenActivity::class.java)
                    intent.putExtra("TARGET_PACKAGE", packageName)
                    // CRITICAL PERFORMANCE FLAGS
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION) // Skip transition
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)     // Reuse existing instance if possible
                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS) // Don't waste time adding to history
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY) // Don't keep in back stack
                    startActivity(intent)
                }
            }
        }
    }

    // 3. Unregister to prevent memory leaks
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(screenOffReceiver)
    }

    override fun onInterrupt() {}
}