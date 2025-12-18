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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bawn.ui.components.*
import app.bawn.ui.theme.*

@Composable
fun DashboardScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDashboard) // #0A0A0A
    ) {
        // --- STICKY HEADER (Matches dashboard.html: border-b border-white/5) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundDashboard.copy(alpha = 0.95f))
                .drawBehind {
                    drawLine(
                        color = Color.White.copy(0.05f),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "BAWN",
                    style = MaterialTheme.typography.headlineMedium,
                    color = NeonGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    letterSpacing = 2.sp
                )
                Text(
                    "CYBER_SEC // V.4.0.2",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontSize = 10.sp
                )
            }
            Box(
                Modifier
                    .size(40.dp)
                    .border(1.dp, NeonGold.copy(0.3f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Shield, null, tint = NeonGold, modifier = Modifier.size(20.dp))
            }
        }

        // --- SCROLLABLE CONTENT ---
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Status Card
            NeonCard(borderColor = NeonGreen) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(6.dp).background(NeonGreen, CircleShape))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "STATUS: ONLINE",
                                color = NeonGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "FORTRESS ACTIVE",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        Icons.Default.Security,
                        contentDescription = null,
                        tint = NeonGreen.copy(alpha = 0.8f),
                        modifier = Modifier.size(48.dp)
                    )
                }

                Divider(color = NeonGreen.copy(0.2f), modifier = Modifier.padding(vertical = 12.dp))

                Text(
                    "System Secure • Scanning real-time traffic...",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )

                Row(
                    Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                        .height(32.dp),
                    verticalAlignment = Alignment.Bottom,
                    // CHANGE THIS: Reduced from 4.dp to 1.dp
                    horizontalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    val heights = listOf(0.4f, 0.6f, 0.3f, 0.7f, 0.5f, 0.9f, 0.4f, 0.8f, 0.5f, 0.3f, 0.6f, 0.4f)
                    heights.forEach { h ->
                        Box(
                            Modifier
                                .weight(1f) // This automatically widens the bars to fill the space saved
                                .fillMaxHeight(h)
                                .background(NeonGreen.copy(alpha = 0.6f), RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader("HIGH VALUE TARGETS")
            AppItem("Chase Mobile", "com.chase.sig.mobile", Icons.Default.AccountBalance, true)
            AppItem("Binance", "com.binance.dev", Icons.Default.CurrencyBitcoin, true)
            AppItem("Signal", "org.thoughtcrime.securesms", Icons.AutoMirrored.Filled.Chat, true)

            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader("GENERAL APPLICATIONS")
            AppItem("Chrome", "com.android.chrome", Icons.Default.Public, false)
            AppItem("Gallery", "com.sec.android.gallery3d", Icons.Default.PhotoLibrary, false)
            AppItem("Calculator", "com.sec.android.calc", Icons.Default.Calculate, false)

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun AppItem(name: String, pkg: String, icon: ImageVector, isLocked: Boolean) {
    var lockedState by remember { mutableStateOf(isLocked) }
    val activeColor = if (lockedState) NeonGold else Color.Gray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { lockedState = !lockedState },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Container
        Box(
            Modifier
                .size(48.dp)
                .background(Color(0xFF222222), RoundedCornerShape(8.dp))
                .border(1.dp, Color.White.copy(0.05f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = activeColor)
        }

        Spacer(Modifier.width(16.dp))

        Column(Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
            Text(pkg, style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontSize = 11.sp)
        }

        NeonSwitch(checked = lockedState, onCheckedChange = { lockedState = it })
    }
}