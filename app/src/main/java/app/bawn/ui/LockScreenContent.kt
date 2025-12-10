package app.bawn.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LockScreenContent(
    // We pass a suspending function that returns true (success) or false (fail)
    onVerifyPin: suspend (String) -> Boolean
) {
    // State for the PIN input
    var pinInput by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    // Coroutine scope to run the verification logic
    val scope = rememberCoroutineScope()

    // Error shake animation trigger (scales up slightly on error)
    val scale by animateFloatAsState(if (isError) 1.2f else 1f, label = "shake")

    // Function to handle key presses
    fun onDigitClick(digit: String) {
        if (pinInput.length < 4) {
            // Reset error state when typing
            if (isError) {
                isError = false
                pinInput = ""
            }

            val newInput = pinInput + digit
            pinInput = newInput

            // Auto-check when 4 digits are entered
            if (newInput.length == 4) {
                scope.launch {
                    // 1. Verify against DB (this suspends)
                    val isCorrect = onVerifyPin(newInput)

                    if (!isCorrect) {
                        // 2. Handle Failure
                        isError = true
                        pinInput = "" // Clear input
                        // Optional: Reset error state after animation
                        delay(500)
                        isError = false
                    }
                    // 3. Handle Success: The Activity will call finish(), so we do nothing here
                }
            }
        }
    }

    fun onDeleteClick() {
        if (pinInput.isNotEmpty()) {
            pinInput = pinInput.dropLast(1)
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
        // --- LOCK ICON ---
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = if (isError) Color.Red else MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(64.dp)
                .scale(scale)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- PIN INDICATOR DOTS ---
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            repeat(4) { index ->
                val isFilled = index < pinInput.length
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

        Spacer(modifier = Modifier.height(64.dp))

        // --- NUMERIC KEYPAD ---
        KeypadGrid(
            onDigitClick = { onDigitClick(it) },
            onDeleteClick = { onDeleteClick() }
        )
    }
}

@Composable
fun KeypadGrid(onDigitClick: (String) -> Unit, onDeleteClick: () -> Unit) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "DEL")
    )

    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        keys.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { key ->
                    KeypadButton(key, onDigitClick, onDeleteClick)
                }
            }
        }
    }
}

@Composable
fun KeypadButton(key: String, onDigitClick: (String) -> Unit, onDeleteClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .clickable(enabled = key.isNotEmpty()) {
                if (key == "DEL") onDeleteClick() else onDigitClick(key)
            },
        contentAlignment = Alignment.Center
    ) {
        if (key == "DEL") {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Delete",
                tint = Color.LightGray
            )
        } else if (key.isNotEmpty()) {
            Text(
                text = key,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = Color.White
                )
            )
        }
    }
}