package app.bawn.ui

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import app.bawn.BawnApplication
import app.bawn.service.SessionManager
import app.bawn.ui.theme.BawnTheme
import app.bawn.util.SecurityUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LockScreenActivity : FragmentActivity() {

    private var targetPackage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        targetPackage = intent.getStringExtra("TARGET_PACKAGE")

        // Security Flags
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        // Handle Back Button (Minimize app, don't close lock screen)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val homeIntent = Intent(Intent.ACTION_MAIN)
                homeIntent.addCategory(Intent.CATEGORY_HOME)
                homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(homeIntent)
            }
        })

        // Try Biometrics
        authenticateWithBiometrics()

        setContent {
            BawnTheme {
                // We pass a suspending verification function to the Compose UI
                LockScreenContent(
                    onVerifyPin = { inputPin -> verifyPin(inputPin) }
                )
            }
        }
    }

    /**
     * Verifies PIN against the Room Database
     */
    private suspend fun verifyPin(inputPin: String): Boolean {
        return withContext(Dispatchers.IO) {
            val db = (application as BawnApplication).database
            val storedHash = db.userSecurityDao().getPinHash()

            val isValid = SecurityUtils.verifyPin(inputPin, storedHash)

            if (isValid) {
                withContext(Dispatchers.Main) {
                    unlockAndFinish()
                }
            }
            isValid
        }
    }

    private fun unlockAndFinish() {
        // 1. Tell Application class we are safe
        (application as BawnApplication).onUserUnlocked()

        // 2. Tell SessionManager (for accessibility service)
        targetPackage?.let { SessionManager.notifyUnlock(it) }

        finish()

        // Remove transition for speed
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(android.app.Activity.OVERRIDE_TRANSITION_CLOSE, 0, 0)
        } else {
            overridePendingTransition(0, 0)
        }
    }

    private fun authenticateWithBiometrics() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(
            this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    unlockAndFinish()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Bawn Locked")
            .setSubtitle("Unlock to continue")
            .setNegativeButtonText("Use PIN")
            .build()

        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (_: Exception) {
        }
    }
}