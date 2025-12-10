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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LockScreenContent(onUnlockSuccess: () -> Unit) {
    // State for the PIN input
    var pinInput by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    // HARDCODED PIN FOR DEMO (Replace with real logic later)
    val correctPin = "1234"

    // Error shake animation trigger (simple scale effect for now)
    val scale by animateFloatAsState(if (isError) 1.1f else 1f, label = "shake")

    // Function to handle key presses
    fun onDigitClick(digit: String) {
        if (pinInput.length < 4) {
            isError = false
            pinInput += digit

            // Auto-check when 4 digits are entered
            if (pinInput.length == 4) {
                if (pinInput == correctPin) {
                    onUnlockSuccess()
                } else {
                    isError = true
                    pinInput = "" // Clear on error
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
            tint = MaterialTheme.colorScheme.primary,
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
            }
        // Optional: Add background for buttons if desired
        // .background(MaterialTheme.colorScheme.surface)
        ,
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