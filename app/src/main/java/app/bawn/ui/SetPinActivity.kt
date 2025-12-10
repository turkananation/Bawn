package app.bawn.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import app.bawn.data.AppDatabase
import app.bawn.data.UserSecurityEntity
import app.bawn.ui.theme.BawnTheme
import app.bawn.util.SecurityUtils
import kotlinx.coroutines.launch

class SetPinActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BawnTheme {
                SetPinScreen(onPinSet = { newPin ->
                    savePinAndFinish(newPin)
                })
            }
        }
    }

    private fun savePinAndFinish(pin: String) {
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            val hash = SecurityUtils.hashPin(pin)
            db.userSecurityDao().savePin(UserSecurityEntity(pinHash = hash))

            Toast.makeText(this@SetPinActivity, "Security PIN Set!", Toast.LENGTH_SHORT).show()
            finish() // Return to the previous screen (likely Settings or Main)
        }
    }
}

enum class PinStep {
    Create, Confirm
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SetPinScreen(onPinSet: (String) -> Unit) {
    var step by remember { mutableStateOf(PinStep.Create) }
    var firstPin by remember { mutableStateOf("") }
    var currentInput by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    // Helper to handle input logic
    fun handleInput(digit: String) {
        if (currentInput.length < 4) {
            isError = false
            currentInput += digit

            if (currentInput.length == 4) {
                if (step == PinStep.Create) {
                    // Move to confirmation
                    firstPin = currentInput
                    currentInput = ""
                    step = PinStep.Confirm
                } else {
                    // Check confirmation
                    if (currentInput == firstPin) {
                        onPinSet(firstPin)
                    } else {
                        // Mismatch! Reset to start or just shake?
                        // Let's reset to Confirm step for now
                        isError = true
                        currentInput = ""
                    }
                }
            }
        }
    }

    fun handleDelete() {
        if (currentInput.isNotEmpty()) {
            currentInput = currentInput.dropLast(1)
            isError = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // Animated Header Text
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                slideInHorizontally { width -> width } + fadeIn() with
                        slideOutHorizontally { width -> -width } + fadeOut()
            }, label = "header_anim"
        ) { targetStep ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (targetStep == PinStep.Create) "Create PIN" else "Confirm PIN",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (targetStep == PinStep.Create) "Enter a 4-digit security code"
                    else "Re-enter your code to verify",
                    color = if (isError) Color(0xFFFF5252) else Color.LightGray,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Reuse your existing UI components for consistency
        // Note: I'm reusing the visual style from LockScreenContent
        PinIndicatorRow(length = currentInput.length, isError = isError)

        Spacer(modifier = Modifier.height(64.dp))

        KeypadGrid(
            onDigitClick = { handleInput(it) },
            onDeleteClick = { handleDelete() }
        )
    }
}

// Reusing these small components locally to ensure this file is self-contained
// (or you can import them if you made them public in LockScreenContent.kt)
@Composable
private fun PinIndicatorRow(length: Int, isError: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        repeat(4) { index ->
            val isFilled = index < length
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(
                        if (isFilled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surface
                    )
                    .border( 
                        width = 1.dp,
                        color = if (isError) Color.Red else Color.Transparent,
                        shape = CircleShape
                    )
            )
        }
    }
}