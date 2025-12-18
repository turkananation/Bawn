package app.bawn.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bawn.ui.components.ConfidenceGauge
import app.bawn.ui.components.NeonCard
import app.bawn.ui.components.NeonSwitch
import app.bawn.ui.components.SectionHeader
import app.bawn.ui.theme.BackgroundLab
import app.bawn.ui.theme.NeonGold
import app.bawn.ui.theme.NeonRed
import app.bawn.ui.theme.NotoSans
import app.bawn.ui.theme.SpaceGrotesk
import app.bawn.ui.theme.SurfaceDark

@Composable
fun SecurityLabScreen() {
    Column(
        Modifier
            .fillMaxSize()
            .background(BackgroundLab)
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundLab.copy(alpha = 0.95f))
                .drawBehind {
                    drawLine(
                        color = NeonGold.copy(0.2f),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("SECURITY LAB", color = NeonGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontFamily = SpaceGrotesk)
            Spacer(Modifier.width(8.dp))
            Text("//", color = Color.White.copy(0.3f), fontSize = 11.sp, fontWeight = FontWeight.Normal, fontFamily = SpaceGrotesk)
            Spacer(Modifier.width(8.dp))
            Text("ACTIVE DEFENSE", color = NeonGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontFamily = SpaceGrotesk)
        }

        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .align(Alignment.CenterHorizontally)
                .padding(16.dp)
        ) {
            // --- TOP SPACER (Push Gauge Down) ---
            Spacer(modifier = Modifier.height(40.dp))

            // --- DYNAMIC GAUGE ---
            // 98% will visually be a "Full Circle with a tiny bottom gap"
            // 100% will be a "Closed Circle"
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                ConfidenceGauge(percentage = 98)
            }

            // --- BOTTOM SPACER (Push Text Way Below) ---
            Spacer(modifier = Modifier.height(40.dp))

            // --- Identity Confidence ---
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "IDENTITY CONFIDENCE",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "BEHAVIORAL ANALYSIS: STABLE",
                    fontSize = 10.sp,
                    color = NeonGold.copy(0.7f),
                    fontFamily = NotoSans,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(Modifier.height(40.dp))

            // --- Intruder Logs ---
            SectionHeader("INTRUDER LOGS [ENCRYPTED]", "VIEW ALL")
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IntruderItem("CAM_01 // FRONT_GATE", "14:02:55")
                IntruderItem("CAM_02 // SERVER", "09:15:22")
                Spacer(Modifier.width(4.dp))
            }

            Spacer(Modifier.height(24.dp))

            // --- Countermeasures ---
            NeonCard(showBracket = true) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, null, tint = NeonGold)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "COUNTERMEASURES",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(Modifier.height(20.dp))

                CountermeasureRow("Fake Crash Protocol", "Simulates system failure on unauthorized touch", false)
                Divider(color = Color.White.copy(0.05f), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
                CountermeasureRow("Decoy Mode V2", "Launches sandbox environment for intruders", true)

                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGold.copy(alpha = 0.1f), contentColor = NeonGold),
                    border = BorderStroke(1.dp, NeonGold),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                ) {
                    Icon(Icons.Default.Lock, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("INITIATE LOCKDOWN", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun IntruderItem(title: String, time: String) {
    Column(
        Modifier
            .width(160.dp)
            .background(SurfaceDark, RoundedCornerShape(8.dp))
            .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(8.dp))
    ) {
        Box(
            Modifier
                .height(100.dp)
                .fillMaxWidth()
                .background(Color.DarkGray)
        ) {
            Icon(
                Icons.Default.Person,
                null,
                tint = Color.White.copy(0.2f),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
            )
            Box(
                Modifier
                    .padding(4.dp)
                    .background(Color.Black.copy(0.6f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp)
            ) {
                Text("REC", color = NeonRed, fontSize = 10.sp)
            }
        }
        Column(Modifier.padding(8.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(time, color = Color.Gray, fontSize = 10.sp)
            Text("UNAUTHORIZED", color = NeonRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CountermeasureRow(title: String, desc: String, initial: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(desc, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        NeonSwitch(checked = initial, onCheckedChange = {})
    }
}