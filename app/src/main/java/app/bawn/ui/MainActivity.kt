package app.bawn.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
    val lifecycleOwner = LocalLifecycleOwner.current

    val apps by viewModel.apps.collectAsState()
    val permissionState by viewModel.permissionState.collectAsState()
    val isPinSet by viewModel.isPinSet.collectAsState()

    // --- INTELLIGENT ROUTING ---
    LaunchedEffect(isPinSet) {
        if (isPinSet == false) {
            viewModel.resetPinState()

            val intent = Intent(context, SetPinActivity::class.java)
            context.startActivity(intent)

            // CRITICAL FIX: Close this activity so it's removed from the back stack.
            // If user presses Back on SetPinActivity, the app will close.
            (context as? Activity)?.finish()
        }
    }

    // --- LIFECYCLE OBSERVER ---
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkStatus()
                viewModel.checkPinStatus()
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
                // Use default icon if logo resource is missing
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Bawn Logo",
                    tint = Color(0xFFFFD700), // Gold
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

            // LOADING STATE OR LIST
            if (apps.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                LazyColumn {
                    items(apps, key = { it.packageName }) { app ->
                        AppListItem(app = app, onToggle = { viewModel.toggleLock(app) })
                        HorizontalDivider(color = Color(0xFF3E4856), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

// --- UI COMPONENTS ---

@Composable
fun PermissionStatusCard(state: PermissionState, onClick: () -> Unit) {
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