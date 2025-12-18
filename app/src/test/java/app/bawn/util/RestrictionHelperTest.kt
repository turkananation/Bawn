package app.bawn.util

import android.app.AppOpsManager
import android.content.Context
import android.content.pm.InstallSourceInfo
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RestrictionHelperTest {

    private lateinit var context: Context
    private lateinit var packageManager: PackageManager
    private lateinit var appOpsManager: AppOpsManager
    private lateinit var installSourceInfo: InstallSourceInfo

    private val testPackageName = "app.bawn.test"
    private val testUid = 12345

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        packageManager = mockk()
        appOpsManager = mockk()
        installSourceInfo = mockk()

        every { context.packageName } returns testPackageName
        every { context.packageManager } returns packageManager
        every { context.getSystemService(AppOpsManager::class.java) } returns appOpsManager

        mockkStatic(Process::class)
        every { Process.myUid() } returns testUid
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // --- TEST 1: The Routing Logic (SDK check) ---

    @Test
    fun `isRestricted returns FALSE when Android version is below 13`() {
        // We can't easily mock SDK_INT without the reflection hack,
        // BUT if you are running this on a standard JVM, SDK_INT is usually 0.
        // So this test passes by default on local machines.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            val result = RestrictionHelper.isLikelyRestricted(context)
            assertFalse("Should be false on old versions", result)
        }
    }

    // --- TEST 2: The Business Logic (Directly testing the internal helper) ---
    // We bypass the SDK check by calling TiramisuHelper directly.
    // This ensures your logic is correct regardless of the test environment.

    @Test
    fun `TiramisuHelper returns FALSE when User has ALREADY allowed settings`() {
        every {
            appOpsManager.checkOpNoThrow(
                "android:access_restricted_settings",
                testUid,
                testPackageName
            )
        } returns AppOpsManager.MODE_ALLOWED

        val result = RestrictionHelper.TiramisuHelper.checkRestrictions(context)
        assertFalse(result)
    }

    @Test
    fun `TiramisuHelper returns FALSE when Source is PLAY STORE`() {
        setupRestrictedSettingsNotAllowed()
        every { packageManager.getInstallSourceInfo(testPackageName) } returns installSourceInfo
        every { installSourceInfo.packageSource } returns PackageInstaller.PACKAGE_SOURCE_STORE

        val result = RestrictionHelper.TiramisuHelper.checkRestrictions(context)
        assertFalse(result)
    }

    @Test
    fun `TiramisuHelper returns TRUE when Source is LOCAL FILE`() {
        setupRestrictedSettingsNotAllowed()
        every { packageManager.getInstallSourceInfo(testPackageName) } returns installSourceInfo
        every { installSourceInfo.packageSource } returns PackageInstaller.PACKAGE_SOURCE_LOCAL_FILE

        val result = RestrictionHelper.TiramisuHelper.checkRestrictions(context)
        assertTrue(result)
    }

    @Test
    fun `TiramisuHelper returns TRUE when Initiator is UNTRUSTED (Chrome)`() {
        setupRestrictedSettingsNotAllowed()
        every { packageManager.getInstallSourceInfo(testPackageName) } returns installSourceInfo
        every { installSourceInfo.packageSource } returns PackageInstaller.PACKAGE_SOURCE_OTHER
        every { installSourceInfo.installingPackageName } returns "com.google.android.packageinstaller"
        every { installSourceInfo.initiatingPackageName } returns "com.android.chrome"

        val result = RestrictionHelper.TiramisuHelper.checkRestrictions(context)
        assertTrue(result)
    }

    @Test
    fun `TiramisuHelper returns FALSE when Initiator is ADB Shell`() {
        // Scenario: Installed via Android Studio "Run" button or adb install
        // We TRUST this now so developers don't get stuck in a red state.
        setupRestrictedSettingsNotAllowed()

        every { packageManager.getInstallSourceInfo(testPackageName) } returns installSourceInfo
        every { installSourceInfo.packageSource } returns PackageInstaller.PACKAGE_SOURCE_OTHER

        // Installer = System, Initiator = ADB (Shell)
        every { installSourceInfo.installingPackageName } returns "com.google.android.packageinstaller"
        every { installSourceInfo.initiatingPackageName } returns "com.android.shell"

        val result = RestrictionHelper.TiramisuHelper.checkRestrictions(context)

        // FIX: We now expect FALSE because we explicitly trust "com.android.shell"
        assertFalse("ADB installs should be trusted for development convenience", result)
    }

    @Test
    fun `TiramisuHelper returns TRUE when both Installer and Initiator are NULL`() {
        // Scenario: Raw sideload or system restore where origin is lost
        setupRestrictedSettingsNotAllowed()
        every { packageManager.getInstallSourceInfo(testPackageName) } returns installSourceInfo
        every { installSourceInfo.packageSource } returns PackageInstaller.PACKAGE_SOURCE_OTHER // or UNSPECIFIED

        // Both are null
        every { installSourceInfo.installingPackageName } returns null
        every { installSourceInfo.initiatingPackageName } returns null

        val result = RestrictionHelper.TiramisuHelper.checkRestrictions(context)
        assertTrue("Unknown sources (null/null) should be restricted", result)
    }

    @Test
    fun `TiramisuHelper returns FALSE when Installer is System but Initiator is TRUSTED`() {
        // Scenario: Play Store hands off APK to System Installer (Valid update flow)
        setupRestrictedSettingsNotAllowed()
        every { packageManager.getInstallSourceInfo(testPackageName) } returns installSourceInfo
        every { installSourceInfo.packageSource } returns PackageInstaller.PACKAGE_SOURCE_OTHER

        // Installer = System (Generic)
        every { installSourceInfo.installingPackageName } returns "com.google.android.packageinstaller"
        // Initiator = Play Store (Trusted)
        every { installSourceInfo.initiatingPackageName } returns "com.android.vending"

        val result = RestrictionHelper.TiramisuHelper.checkRestrictions(context)
        assertFalse("System install initiated by Play Store should be trusted", result)
    }

    @Test
    fun `TiramisuHelper returns TRUE when Installer is completely UNKNOWN`() {
        // Scenario: Installed by "com.random.store" which is not in your trusted list
        setupRestrictedSettingsNotAllowed()
        every { packageManager.getInstallSourceInfo(testPackageName) } returns installSourceInfo
        every { installSourceInfo.packageSource } returns PackageInstaller.PACKAGE_SOURCE_OTHER

        every { installSourceInfo.installingPackageName } returns "com.random.untrusted.store"
        every { installSourceInfo.initiatingPackageName } returns "com.random.untrusted.store"

        val result = RestrictionHelper.TiramisuHelper.checkRestrictions(context)
        assertTrue("Unknown installer packages should be restricted", result)
    }

    private fun setupRestrictedSettingsNotAllowed() {
        every {
            appOpsManager.checkOpNoThrow(
                "android:access_restricted_settings",
                testUid,
                testPackageName
            )
        } returns AppOpsManager.MODE_DEFAULT
    }
}