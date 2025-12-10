package app.bawn.util

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.pm.InstallSourceInfo
import android.os.Build
import android.view.accessibility.AccessibilityManager
import app.bawn.service.BawnAccessibilityService

object RestrictionHelper {

    // 1. Check if the Service is currently ON
    fun isServiceEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)

        return enabledServices.any { service ->
            service.resolveInfo.serviceInfo.packageName == context.packageName &&
                    service.resolveInfo.serviceInfo.name == BawnAccessibilityService::class.java.name
        }
    }

    // 2. Check if the App is Restricted (Sideloaded)
    fun isLikelyRestricted(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false

        val installer = getInstallerPackageName(context) ?: return false

        // If installer is NULL, it's usually ADB (Developer). We treat this as SAFE (Not Restricted).

        val trustedInstallers = listOf(
            "com.android.vending", // Google Play
            "com.amazon.venezia",  // Amazon
            "com.google.android.packageinstaller"
        )

        return !trustedInstallers.contains(installer)
    }

    private fun getInstallerPackageName(context: Context): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getInstallerPackageName(context.packageName)
            }
        } catch (e: Exception) {
            null
        }
    }
}