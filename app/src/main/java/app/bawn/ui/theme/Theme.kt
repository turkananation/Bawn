package app.bawn.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00FF9D), // Neon Moss
    secondary = Color(0xFF00E5FF),
    background = Color(0xFF1C1C1E), // Basalt Grey
    surface = Color(0xFF2C2C2E)
)

@Composable
fun BawnTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}