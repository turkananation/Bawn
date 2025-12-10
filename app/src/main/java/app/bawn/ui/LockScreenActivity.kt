package app.bawn.ui

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import app.bawn.service.SessionManager
import app.bawn.ui.theme.BawnTheme

class LockScreenActivity : FragmentActivity() {

    private var targetPackage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Get the package name of the app we are locking
        targetPackage = intent.getStringExtra("TARGET_PACKAGE")

        // 2. Security Flags: Prevent screenshots & hide from 'Recent Apps' preview
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        // 3. Block Back Button: Pressing back goes HOME, not to the locked app
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goHome()
            }
        })

        // 4. Launch Biometrics immediately
        authenticateWithBiometrics()

        setContent {
            BawnTheme {
                // Pass the unlock callback to the UI
                LockScreenContent(
                    onUnlockSuccess = { unlockAndFinish() }
                )
            }
        }
    }

    private fun authenticateWithBiometrics() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    unlockAndFinish()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // If user cancels or too many attempts, we stay on the PIN screen
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock App")
            .setSubtitle("Confirm your identity")
            .setNegativeButtonText("Use PIN")
            .build()

        // Only authenticate if we can (hardware available)
        // If not, the user just sees the PIN screen from 'setContent'
        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (_: Exception) {
            // Biometrics might crash on some emulators without setup, ignore safely
        }
    }

    private fun unlockAndFinish() {
        // 1. Notify SessionManager
        targetPackage?.let { packageName ->
            SessionManager.notifyUnlock(packageName)
        }

        // 2. Finish the activity
        finish()

        // 3. Handle Exit Animation Correctly based on API Level
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // API 34+ (Android 14): Use the new method
            overrideActivityTransition(
                android.app.Activity.OVERRIDE_TRANSITION_CLOSE,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
        } else {
            // API 33 and below: Use the legacy method
            // This is NOT deprecated for these API levels, so the compiler accepts it safely here
            // without needing @Suppress if your compileSdk matches your logic,
            // but if your compileSdk is 34+, legacy calls might still flag.
            // The cleanest way is often wrapping legacy calls in a compat helper,
            // but this if-check is the standard architectural pattern.
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun goHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN)
        homeIntent.addCategory(Intent.CATEGORY_HOME)
        homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(homeIntent)
        finish()
    }
}