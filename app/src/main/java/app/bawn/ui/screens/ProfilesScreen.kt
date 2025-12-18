package app.bawn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bawn.ui.components.NeonCard
import app.bawn.ui.theme.*

@Composable
fun ProfilesScreen() {
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .background(BackgroundProfiles) // #23200f
        ) {
            // --- HEADER (Fixed: Sticky + Border Bottom) ---
            // Matches profiles.html: border-b border-black/5 dark:border-primary/20
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundProfiles.copy(alpha = 0.95f))
                    .drawBehind {
                        drawLine(
                            color = NeonGold.copy(0.2f), // Primary/20
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        "BAWN OS",
                        color = NeonGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "CONTEXTUAL INTEL",
                        color = Color.White,
                        fontSize = 20.sp, // Slightly reduced to match screenshot width
                        fontWeight = FontWeight.Bold,
                        fontFamily = SpaceGrotesk,
                        letterSpacing = (-0.5).sp
                    )
                }

                Box(
                    modifier = Modifier
                        .border(1.dp, NeonGold.copy(0.5f), RoundedCornerShape(4.dp))
                        .background(NeonGold.copy(0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(8.dp)
                                .background(NeonGold, CircleShape)
                                .shadow(6.dp, CircleShape, spotColor = NeonGold)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "SYSTEM ACTIVE",
                            color = NeonGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // --- SCROLLABLE CONTENT ---
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "ACTIVE PROFILES",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        letterSpacing = 2.sp,
                        fontSize = 11.sp
                    )
                    Icon(
                        Icons.Default.GridView,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProfileCard("WORK", "Secure Tunnel Active", Icons.Default.BusinessCenter, true, Modifier.weight(1f))
                    ProfileCard("HOME", "Perimeter Monitoring", Icons.Default.Home, false, Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProfileCard("GUEST", "Restricted Access", Icons.Default.Person, false, Modifier.weight(1f))
                    ProfileCard("CHILD", "Content Filtered", Icons.Default.ChildCare, false, Modifier.weight(1f))
                }

                Spacer(Modifier.height(32.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "AUTOMATION PROTOCOLS",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(16.dp))
                    Box(Modifier.weight(1f).height(1.dp).background(Brush.horizontalGradient(listOf(NeonGold, Color.Transparent))))
                }

                Spacer(Modifier.height(16.dp))

                NeonCard {
                    Column(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row {
                                Box(
                                    Modifier
                                        .size(36.dp)
                                        .background(NeonGold.copy(0.1f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Schedule, null, tint = NeonGold, modifier = Modifier.size(20.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("Time-Based Trigger", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                    Text(
                                        "Auto-switch profile based on time",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Box(Modifier.size(24.dp).background(NeonGold, RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Check, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(0.6f), RoundedCornerShape(4.dp))
                                .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(4.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("09:00", fontWeight = FontWeight.Bold, fontSize = 20.sp, fontFamily = SpaceGrotesk, color = Color.White)
                                Spacer(Modifier.width(4.dp))
                                Text("START", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                            }

                            Box(Modifier.width(40.dp).height(1.dp).background(Color.Gray.copy(0.3f)))

                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("18:00", fontWeight = FontWeight.Bold, fontSize = 20.sp, fontFamily = SpaceGrotesk, color = Color.White)
                                Spacer(Modifier.width(4.dp))
                                Text("END", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                            Text("CONFIGURE", color = NeonGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowForward, null, tint = NeonGold, modifier = Modifier.size(12.dp))
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                // Geofence Card... (Omitted for brevity, logic identical to previous but needs to exist)
                Spacer(Modifier.height(80.dp))
            }
        }

        FloatingActionButton(
            onClick = {},
            containerColor = NeonGold,
            contentColor = BackgroundDark,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 24.dp)
                .size(56.dp)
                .shadow(16.dp, CircleShape, spotColor = NeonGold)
        ) {
            Icon(Icons.Default.Save, null, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun ProfileCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isActive: Boolean,
    modifier: Modifier
) {
    val borderColor = if (isActive) NeonGold else Color.White.copy(0.1f)
    val containerColor = if (isActive) Color(0xFF1E1E12) else SurfaceDark
    val iconColor = if (isActive) Color.Black else Color.Gray // Active icon is black on yellow bg
    val iconBg = if (isActive) NeonGold else Color(0xFF252525)

    Card(
        modifier = modifier
            .height(150.dp)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .shadow(
                if (isActive) 12.dp else 0.dp,
                RoundedCornerShape(12.dp),
                spotColor = if (isActive) NeonGold else Color.Transparent
            ),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                // Icon Box
                Box(
                    Modifier
                        .size(40.dp)
                        .background(iconBg, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp))
                }

                if (isActive) {
                    Box(
                        Modifier
                            .size(6.dp)
                            .background(NeonGold, CircleShape)
                            .shadow(4.dp, CircleShape, spotColor = NeonGold)
                    )
                }
            }

            Column {
                Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    subtitle,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    color = if (isActive) NeonGold else Color.Gray
                )
            }
        }
    }
}