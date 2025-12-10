package app.bawn.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import app.bawn.R
import app.bawn.ui.theme.BawnTheme
import com.google.accompanist.drawablepainter.rememberDrawablePainter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BawnTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: AppListViewModel = viewModel(factory = AppListViewModel.Factory)) {
    val context = LocalContext.current

    // FIX: This works with your current dependencies
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    val apps by viewModel.apps.collectAsState()
    val permissionState by viewModel.permissionState.collectAsState()

    // --- LIFECYCLE OBSERVER ---
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var showRestrictedDialog by remember { mutableStateOf(false) }

    if (showRestrictedDialog) {
        RestrictedHelpDialog(
            onDismiss = { showRestrictedDialog = false },
            onGoToSettings = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
                showRestrictedDialog = false
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFF2B3440),
        bottomBar = {
            Button(
                onClick = {
                    val intent = Intent(context, LockScreenActivity::class.java)
                    intent.putExtra("TARGET_PACKAGE", "app.bawn")
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Test Lock Screen UI", color = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Make sure logo_bawn_gold exists in res/drawable, or change this to Icons.Default.Lock
                Image(
                    painter = painterResource(id = R.drawable.logo_bawn_gold),
                    contentDescription = "Bawn Logo",
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Bawn", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            }

            // PERMISSION CARD
            PermissionStatusCard(
                state = permissionState,
                onClick = {
                    when (permissionState) {
                        PermissionState.Restricted -> showRestrictedDialog = true
                        PermissionState.Inactive -> {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        }
                        PermissionState.Active -> { /* Do Nothing */ }
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Your Apps",
                color = Color.LightGray,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            LazyColumn {
                items(apps, key = { it.packageName }) { app ->
                    AppListItem(app = app, onToggle = { viewModel.toggleLock(app) })
                    HorizontalDivider(color = Color(0xFF3E4856), thickness = 0.5.dp)
                }
            }
        }
    }
}

// --- UI COMPONENTS ---

@Composable
fun PermissionStatusCard(state: PermissionState, onClick: () -> Unit) {
    // Helper data class for colors/icons
    data class StatusUI(val color: Color, val icon: ImageVector, val title: String, val desc: String)

    val status = when (state) {
        PermissionState.Restricted -> StatusUI(
            Color(0xFFFF5252), Icons.Default.Lock, "Restricted Setting", "Tap to fix Android 13+ block"
        )
        PermissionState.Inactive -> StatusUI(
            Color(0xFFFFA000), Icons.Default.Warning, "Service Inactive", "Tap to enable Accessibility"
        )
        PermissionState.Active -> StatusUI(
            Color(0xFF00FF9D), Icons.Default.CheckCircle, "Protected", "Bawn is active"
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(enabled = state != PermissionState.Active) { onClick() },
        colors = CardDefaults.cardColors(containerColor = status.color.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, status.color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = status.icon,
                contentDescription = null,
                tint = status.color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = status.title, color = Color.White, style = MaterialTheme.typography.titleMedium)
                Text(text = status.desc, color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun AppListItem(app: AppUiModel, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (app.icon != null) {
            Image(painter = rememberDrawablePainter(app.icon), contentDescription = null, modifier = Modifier.size(42.dp))
        } else {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(42.dp))
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = app.name,
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = app.isLocked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF00C853),
                uncheckedThumbColor = Color.LightGray,
                uncheckedTrackColor = Color.DarkGray
            )
        )
    }
}