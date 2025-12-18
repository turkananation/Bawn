package app.bawn.util

import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageInstaller
import android.os.Build
import android.os.Process
import android.view.accessibility.AccessibilityManager
import androidx.annotation.RequiresApi
import app.bawn.service.BawnAccessibilityService

object RestrictionHelper {

    /**
     * Public entry point.
     * Checks restrictions safely across all Android versions.
     */
    fun isLikelyRestricted(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return false
        }
        return TiramisuHelper.checkRestrictions(context)
    }

    fun isServiceEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices =
            am.getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK)

        return enabledServices.any {
            it.resolveInfo.serviceInfo.packageName == context.packageName &&
                    it.resolveInfo.serviceInfo.name == BawnAccessibilityService::class.java.name
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    internal object TiramisuHelper {

        fun checkRestrictions(context: Context): Boolean {
            if (hasUserAllowedRestrictedSettings(context)) {
                return false
            }
            return isInstallSourceRestricted(context)
        }

        private fun hasUserAllowedRestrictedSettings(context: Context): Boolean {
            return try {
                val appOps = context.getSystemService(AppOpsManager::class.java)
                val mode = appOps.checkOpNoThrow(
                    "android:access_restricted_settings",
                    Process.myUid(),
                    context.packageName
                )
                mode == AppOpsManager.MODE_ALLOWED
            } catch (_: Exception) {
                false
            }
        }

        private fun isInstallSourceRestricted(context: Context): Boolean {
            return try {
                val packageManager = context.packageManager
                val info = packageManager.getInstallSourceInfo(context.packageName)

                // 1. Check strict sources
                val source = info.packageSource
                val isFromStore = source == PackageInstaller.PACKAGE_SOURCE_STORE
                val isLocalFile = source == PackageInstaller.PACKAGE_SOURCE_LOCAL_FILE
                val isDownloadedFile = source == PackageInstaller.PACKAGE_SOURCE_DOWNLOADED_FILE

                if (isFromStore) return false
                if (isLocalFile || isDownloadedFile) return true

                // 2. Fallback: Check Package Names
                val installer = info.installingPackageName
                val initiator = info.initiatingPackageName

                // If both are null, it's a very raw install (often ADB or Restore) -> RESTRICTED
                if (installer == null && initiator == null) return true

                // 3. ADB Detection Logic
                // If the initiator is "com.android.shell", it was installed via ADB.
                // ADB installs are considered restricted on release build but not on debug build.
                if (initiator == "com.android.shell") {
                    return false // Trust ADB installs so we can debug easily!
                }

                val trustedStores = setOf(
                    "com.android.vending",
                    "com.google.android.packageinstaller",
                    "com.amazon.venezia",
                    "com.sec.android.app.samsungapps",
                    "com.huawei.appmarket",
                    "com.xiaomi.market",
                    "com.oppo.market",
                    "com.vivo.appstore"
                )

                val isInstallerTrusted = installer != null && trustedStores.contains(installer)
                val isInitiatorTrusted = initiator != null && trustedStores.contains(initiator)

                if (installer == "com.google.android.packageinstaller") {
                    return !isInitiatorTrusted
                }

                !isInstallerTrusted
            } catch (_: Exception) {
                // Fail safe: If we can't check, assume restricted
                true
            }
        }
    }
}