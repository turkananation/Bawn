package app.bawn.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

            // Relaunch MainActivity to ensure fresh state
            val intent = Intent(this@SetPinActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
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
            // Reset error state on new input
            if (isError) {
                isError = false
                currentInput = ""
            }

            val newInput = currentInput + digit
            currentInput = newInput

            if (newInput.length == 4) {
                if (step == PinStep.Create) {
                    // Move to confirmation
                    firstPin = newInput
                    currentInput = ""
                    step = PinStep.Confirm
                } else {
                    // Check confirmation
                    if (newInput == firstPin) {
                        onPinSet(firstPin)
                    } else {
                        // Mismatch! Reset EVERYTHING to start over
                        isError = true
                        currentInput = ""
                        firstPin = ""
                        step = PinStep.Create
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
                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                    exit = slideOutHorizontally { width -> -width } + fadeOut())
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

                val instruction = when {
                    isError -> "PINs did not match. Try again."
                    targetStep == PinStep.Create -> "Enter a 4-digit security code"
                    else -> "Re-enter your code to verify"
                }

                Text(
                    text = instruction,
                    color = if (isError) Color(0xFFFF5252) else Color.LightGray,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(height = 48.dp))

        PinIndicatorRow(length = currentInput.length, isError = isError)

        Spacer(modifier = Modifier.height(height = 64.dp))

        // Reuse KeypadGrid from LockScreenContent.kt (Removed duplicate definition below)
        KeypadGrid(
            onDigitClick = { handleInput(digit = it) },
            onDeleteClick = { handleDelete() }
        )
    }
}

// Keep this helper private since it's unique to this screen
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