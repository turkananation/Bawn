package app.bawn.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import app.bawn.ui.theme.BackgroundDark
import app.bawn.ui.theme.NeonGold
import app.bawn.ui.theme.SpaceGrotesk
import app.bawn.ui.theme.SurfaceDark

// --- 1. Custom Navigation Bar (Matches Screenshot) ---
@Composable
fun NeonNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp) // Taller nav bar
            .background(BackgroundDark.copy(alpha = 0.95f))
            .border(1.dp, Color.White.copy(0.05f)), // Top border
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavItem("DASHBOARD", "dash", currentRoute, navController)
        NavItem("PROFILES", "profiles", currentRoute, navController)
        NavItem("SECURITY LAB", "lab", currentRoute, navController)
        NavItem("SETTINGS", "settings", currentRoute, navController)
    }
}

@Composable
fun NavItem(label: String, route: String, currentRoute: String?, navController: NavController) {
    val selected = currentRoute == route
    val color = if (selected) NeonGold else Color.Gray

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { navController.navigate(route) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // The "Active Line" indicator at the very top
        if (selected) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(2.dp)
                    .background(NeonGold)
                    .shadow(4.dp, spotColor = NeonGold)
            )
        } else {
            Spacer(Modifier.height(2.dp))
        }

        Spacer(Modifier.weight(1f))

        // Icon mapping (using standard icons for simplicity, swap if you have specific drawables)
        val icon = when (route) {
            "dash" -> androidx.compose.material.icons.Icons.Default.Dashboard
            "profiles" -> androidx.compose.material.icons.Icons.Default.Badge
            "lab" -> androidx.compose.material.icons.Icons.Default.Security
            else -> androidx.compose.material.icons.Icons.Default.Settings
        }

        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            color = color,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            letterSpacing = 0.5.sp
        )

        Spacer(Modifier.weight(1f))
    }
}

// --- 2. Reusable Neon Card with "Corner Bracket" Option ---
@Composable
fun NeonCard(
    modifier: Modifier = Modifier,
    borderColor: Color = NeonGold.copy(alpha = 0.3f),
    showBracket: Boolean = false, // Toggle for the yellow corner accent
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, borderColor, RoundedCornerShape(4.dp)), // Sharper corners like screenshot
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), content = content)
        }

        // The "Yellow Bracket" decoration seen in Security Lab
        if (showBracket) {
            Canvas(modifier = Modifier.size(20.dp).align(Alignment.TopEnd).padding(4.dp)) {
                drawLine(
                    color = NeonGold,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = NeonGold,
                    start = Offset(size.width, 0f),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
    }
}

// --- 3. Tactical Switch (Pill Shape) ---
@Composable
fun NeonSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    color: Color = NeonGold
) {
    // Exact dimensions from screenshot (~44x24dp)
    val trackColor = if (checked) color.copy(alpha = 0.2f) else Color(0xFF2A2A2A)
    val borderColor = if (checked) color else Color.Gray.copy(0.5f)
    val thumbOffset by animateDpAsState(if (checked) 20.dp else 2.dp, label = "thumb")

    Box(
        modifier = Modifier
            .width(44.dp)
            .height(24.dp)
            .clip(RoundedCornerShape(100))
            .background(trackColor)
            .border(1.dp, borderColor, RoundedCornerShape(100))
            .clickable { onCheckedChange(!checked) }
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .padding(vertical = 2.dp)
                .size(20.dp)
                .background(if (checked) color else Color.Gray, CircleShape)
        )
    }
}

@Composable
fun SectionHeader(title: String, actionText: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Optional: Add small icon if needed, otherwise just text
            if (title.contains("LOGS")) {
                Icon(
                    androidx.compose.material.icons.Icons.Default.VisibilityOff,
                    contentDescription = null,
                    tint = NeonGold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(0.9f),
                letterSpacing = 1.5.sp
            )
        }

        if (actionText != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelSmall,
                color = NeonGold,
                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * Gauge Component with SYMMETRICAL logic.
 * 100% = 360 degrees (Closed).
 * 98% = 352.8 degrees (Tiny gap at bottom).
 * 50% = 180 degrees (Semi-circle at top).
 */
@Composable
fun ConfidenceGauge(percentage: Int) {
    val animatedProgress by animateFloatAsState(
        targetValue = percentage.toFloat(),
        animationSpec = tween(durationMillis = 1000),
        label = "GaugeAnimation"
    )

    // LOGIC: Map 0-100 to 0-360 degrees
    val sweepAngle = (animatedProgress / 100f) * 360f

    // SYMMETRY: Start angle is adjusted so the arc is always centered at the Top (270 degrees)
    // Formula: 270 - (sweep / 2)
    // Examples:
    // If sweep 360 -> Start = 270 - 180 = 90 (Bottom) -> Full Circle.
    // If sweep 180 -> Start = 270 - 90 = 180 (Left) -> Top Half Circle.
    // If sweep 352 (98%) -> Start = 270 - 176 = 94 -> Gap is at Bottom.
    val startAngle = 270f - (sweepAngle / 2f)

    Box(
        Modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Background Track: A faint full circle to guide the eye (optional, keeps layout stable)
            drawCircle(
                color = Color(0xFF222222),
                style = Stroke(width = 20.dp.toPx())
            )

            // Foreground Progress: Neon Gold Arc
            drawArc(
                color = NeonGold,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Center Info Block
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "SYSTEM STATUS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = NeonGold.copy(0.8f),
                letterSpacing = 2.sp
            )
            Text(
                "$percentage%",
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SpaceGrotesk,
                color = Color.White,
                letterSpacing = (-2).sp
            )
            Spacer(Modifier.height(4.dp))

            if (percentage >= 90) {
                Box(
                    modifier = Modifier
                        .background(NeonGold, RoundedCornerShape(100))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        "VERIFIED",
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}