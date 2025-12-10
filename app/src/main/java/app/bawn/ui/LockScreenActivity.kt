package app.bawn.ui

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.OnBackPressedCallback
// 1. IMPORTANT: Use FragmentActivity
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import app.bawn.service.SessionManager
import app.bawn.ui.theme.BawnTheme

// 2. Class must inherit from FragmentActivity
class LockScreenActivity : FragmentActivity() {

    private lateinit var targetPackage: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        targetPackage = intent.getStringExtra("TARGET_PACKAGE") ?: ""

        // Security: Prevent screenshots and recents preview
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        // Handle Back Button (Exit to home instead of unlocking app)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val homeIntent = Intent(Intent.ACTION_MAIN)
                homeIntent.addCategory(Intent.CATEGORY_HOME)
                homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(homeIntent)
                finish()
            }
        })

        // Trigger biometrics immediately
        showBiometricPrompt()

        setContent {
            BawnTheme {
                LockScreenUI(onUnlock = { unlockAndFinish() })
            }
        }
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Bawn Locked")
            .setSubtitle("Authenticate to access")
            .setNegativeButtonText("Use PIN")
            .build()

        // 3. 'this' is now a valid FragmentActivity
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    unlockAndFinish()
                }
            })

        biometricPrompt.authenticate(promptInfo)
    }

    private fun unlockAndFinish() {
        SessionManager.notifyUnlock(targetPackage)
        finish()
    }
}

@Composable
fun LockScreenUI(onUnlock: () -> Unit) {
    // Simple PIN Logic Placeholder
    var pin by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF1C1C1E)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF00FF9D), modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(32.dp))
        Text("Bawn Locked", color = Color.White, fontSize = 24.sp)

        Spacer(modifier = Modifier.height(32.dp))

        // Visual PIN Dots
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(4) {
                Box(modifier = Modifier.size(16.dp).background(
                    if(pin.length > it) Color(0xFF00FF9D) else Color.Gray, CircleShape
                ))
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = { onUnlock() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
        ) {
            Text("Emergency Unlock (Dev)", color = Color.White)
        }
    }
}