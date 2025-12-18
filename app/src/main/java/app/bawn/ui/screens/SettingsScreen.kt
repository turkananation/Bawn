package app.bawn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bawn.ui.components.NeonSwitch
import app.bawn.ui.theme.*

@Composable
fun SettingsScreen() {
    Column(
        Modifier
            .fillMaxSize()
            .background(BackgroundSettings) // #181710
    ) {
        // --- HEADER (Fixed: Border Bottom) ---
        // Matches settings.html: border-b border-black/5 dark:border-white/5
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundSettings.copy(alpha = 0.95f))
                .drawBehind {
                    drawLine(
                        color = Color.White.copy(0.05f),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                .padding(top = 16.dp, bottom = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "THE VAULT",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color.White,
                    fontSize = 14.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                    shadow = Shadow(
                        color = Color.White.copy(alpha = 0.3f),
                        offset = Offset(0f, 0f),
                        blurRadius = 10f
                    )
                )
            )
        }

        // --- CONTENT ---
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ... (Rest of settings content items from previous steps) ...
            // Just ensure SettingsItemContainer etc. are present
            SettingsSectionTitle("AUTHENTICATION")
            SettingsItemContainer {
                SettingsItem(Icons.Default.Fingerprint, "Biometric Access", "Enable Fingerprint or FaceID login") { NeonSwitch(true, {}) }
                Divider()
                SettingsItem(Icons.Default.Grid3x3, "Pattern Lock", "Change your 3x3 security grid") { Icon(Icons.Default.ChevronRight, null, tint = Color.Gray) }
                Divider()
                SettingsItem(Icons.Default.Timer, "Auto-Lock Timeout", "Lock app after inactivity") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Immediate", color = Color.Gray, fontSize = 12.sp)
                        Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
                    }
                }
            }

            SettingsSectionTitle("LOOK & FEEL")
            SettingsItemContainer {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SettingsIcon(Icons.Default.Palette)
                        Spacer(Modifier.width(16.dp))
                        Text("Interface Theme", fontWeight = FontWeight.Medium, color = Color.White)
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ThemeCard("NEON", NeonGold, true)
                        ThemeCard("STEALTH", Color.Gray, false)
                        ThemeCard("MATRIX", NeonGreen, false)
                    }
                }
                Divider()
                SettingsItem(Icons.Default.Vibration, "Haptic Feedback", "Vibrate on touch interactions") { NeonSwitch(true, {}) }
            }

            SettingsSectionTitle("PRIVACY")
            SettingsItemContainer {
                SettingsItem(Icons.Default.AdminPanelSettings, "Uninstall Protection", "Prevent unauthorized removal") { NeonSwitch(false, {}) }
                Divider()
                SettingsItem(Icons.Default.NotificationsOff, "Notification Privacy", "Hide content on lock screen") { Icon(Icons.Default.ChevronRight, null, tint = Color.Gray) }
            }

            SettingsSectionTitle("ADVANCED")
            SettingsItemContainer {
                SettingsItem(Icons.Default.CloudUpload, "Encrypted Backup", "Last backup: 2 hours ago") {
                    Box(Modifier.background(NeonGold.copy(0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Text("SYNC", color = NeonGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}

// --- Helper Components ---

@Composable
fun VaultHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "THE VAULT",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = Color.White,
                // CHANGED: Much smaller font size to match design
                fontSize = 14.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold,
                shadow = Shadow(
                    color = Color.White.copy(alpha = 0.3f),
                    offset = Offset(0f, 0f),
                    blurRadius = 10f
                )
            )
        )
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        title,
        color = Color.White.copy(0.4f),
        // CHANGED: Smaller section headers (10.sp)
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsItemContainer(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(4.dp)),
        content = content
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    control: @Composable () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIcon(icon)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, color = Color.White, fontSize = 16.sp)
            Text(
                subtitle,
                fontFamily = NotoSans,
                color = Color.Gray,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
        Spacer(Modifier.width(8.dp))
        control()
    }
}

@Composable
fun SettingsIcon(icon: ImageVector) {
    Box(
        Modifier
            .size(48.dp)
            .background(NeonGold.copy(0.1f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = NeonGold)
    }
}

@Composable
fun Divider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.White.copy(0.05f))
    )
}

@Composable
fun ThemeCard(name: String, color: Color, selected: Boolean) {
    val borderColor = if (selected) color else Color.Gray.copy(0.3f)

    val bgBrush = if (selected && name == "NEON") {
        Brush.linearGradient(colors = listOf(Color(0xFF332200), Color.Black))
    } else {
        SolidColor(Color(0xFF2a2a2a))
    }

    Box(
        Modifier
            .width(100.dp)
            .height(70.dp)
            .border(if (selected) 2.dp else 1.dp, borderColor, RoundedCornerShape(8.dp))
            .background(bgBrush, RoundedCornerShape(8.dp))
    ) {
        if (selected) {
            Box(
                Modifier
                    .padding(8.dp)
                    .size(8.dp)
                    .background(color, CircleShape)
                    .shadow(4.dp, CircleShape, spotColor = color)
                    .align(Alignment.TopStart)
            )
        } else {
            Box(
                Modifier
                    .padding(8.dp)
                    .size(8.dp)
                    .border(1.dp, Color.White.copy(0.3f), CircleShape)
                    .align(Alignment.TopStart)
            )
        }
        Text(
            name,
            color = if (selected) color else Color.White.copy(0.5f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        )
    }
}