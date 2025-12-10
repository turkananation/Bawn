# 🏗️ Bawn Feature Implementation Guide
## Comprehensive Technical Specification - Senior Engineer Level

**Document Version:** 1.0  
**Target SDK:** 26-35 (Android 8.0 - Android 15)  
**Architecture:** Clean Architecture + MVVM  
**Estimated Total Effort:** 180-240 developer days

---

## Table of Contents
- [Security Enhancements](#security-enhancements)
- [UI/UX Features](#uiux-features)
- [Advanced Functionality](#advanced-functionality)
- [App Management](#app-management)
- [Advanced Security](#advanced-security)
- [Smart Features](#smart-features)
- [Connectivity & Integration](#connectivity--integration)
- [Experimental Features](#experimental-features)
- [Implementation Priorities](#implementation-priorities)

---

# Security Enhancements

## 1. Intruder Selfie

**Priority:** HIGH | **Complexity:** MEDIUM | **Effort:** 5-8 days

### Architecture
```
LockScreenActivity → IntruderDetectionManager → CameraManager
                                                      ↓
                                              IntruderLogDao (Room)
```

### Database Schema
```kotlin
@Entity(tableName = "intruder_logs")
data class IntruderLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val packageName: String,
    val failedAttempts: Int,
    val photoPath: String?, 
    val deviceInfo: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Dao
interface IntruderLogDao {
    @Insert
    suspend fun insert(log: IntruderLogEntity): Long
    
    @Query("SELECT * FROM intruder_logs ORDER BY timestamp DESC LIMIT 50")
    fun getAllLogs(): Flow<List<IntruderLogEntity>>
    
    @Query("DELETE FROM intruder_logs WHERE timestamp < :cutoffTime")
    suspend fun deleteOldLogs(cutoffTime: Long)
}
```

### Implementation

**IntruderDetectionManager.kt**
```kotlin
class IntruderDetectionManager @Inject constructor(
    private val cameraManager: CameraManager,
    private val intruderDao: IntruderLogDao,
    private val settingsRepository: SettingsRepository
) {
    private val attemptMap = ConcurrentHashMap<String, Int>()
    
    suspend fun recordFailedAttempt(packageName: String): Boolean {
        val settings = settingsRepository.getIntruderSettings()
        if (!settings.enabled) return false
        
        val attempts = attemptMap.compute(packageName) { _, v -> (v ?: 0) + 1 }!!
        
        return if (attempts >= settings.threshold) {
            captureAndLog(packageName, attempts)
            attemptMap.remove(packageName)
            true
        } else {
            false
        }
    }
    
    private suspend fun captureAndLog(packageName: String, attempts: Int) {
        withContext(Dispatchers.IO) {
            val photoPath = try {
                cameraManager.captureSilently()
            } catch (e: Exception) {
                Log.e(TAG, "Camera capture failed", e)
                null
            }
            
            intruderDao.insert(
                IntruderLogEntity(
                    timestamp = System.currentTimeMillis(),
                    packageName = packageName,
                    failedAttempts = attempts,
                    photoPath = photoPath,
                    deviceInfo = "${Build.MANUFACTURER} ${Build.MODEL}"
                )
            )
        }
    }
    
    fun resetAttempts(packageName: String) {
        attemptMap.remove(packageName)
    }
}
```

**CameraManager.kt**
```kotlin
class CameraManager(private val context: Context) {
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    
    suspend fun captureSilently(): String = suspendCoroutine { continuation ->
        val cameraProvider = ProcessCameraProvider.getInstance(context)
        
        cameraProvider.addListener({
            val provider = cameraProvider.get()
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            
            val imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
            
            try {
                provider.unbindAll()
                provider.bindToLifecycle(
                    context as LifecycleOwner,
                    cameraSelector,
                    imageCapture
                )
                
                val file = File(
                    context.filesDir,
                    "intruder_${System.currentTimeMillis()}.jpg"
                )
                
                val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
                
                imageCapture.takePicture(
                    outputOptions,
                    cameraExecutor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            provider.unbindAll()
                            continuation.resume(file.absolutePath)
                        }
                        
                        override fun onError(exception: ImageCaptureException) {
                            provider.unbindAll()
                            continuation.resumeWithException(exception)
                        }
                    }
                )
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }, ContextCompat.getMainExecutor(context))
    }
}
```

**UI: IntruderLogsScreen.kt**
```kotlin
@Composable
fun IntruderLogsScreen(viewModel: IntruderLogsViewModel) {
    val logs by viewModel.logs.collectAsState(initial = emptyList())
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Security Logs") },
                actions = {
                    IconButton(onClick = { viewModel.clearAllLogs() }) {
                        Icon(Icons.Default.Delete, "Clear All")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(logs) { log ->
                IntruderLogCard(
                    log = log,
                    onDelete = { viewModel.deleteLog(log.id) }
                )
            }
        }
    }
}

@Composable
fun IntruderLogCard(log: IntruderLogEntity, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            if (log.photoPath != null) {
                AsyncImage(
                    model = log.photoPath,
                    contentDescription = "Intruder Photo",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.packageName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${log.failedAttempts} failed attempts",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy HH:mm").format(log.timestamp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete")
            }
        }
    }
}
```

### Permissions
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" android:required="false" />
<uses-feature android:name="android.hardware.camera.front" android:required="false" />
```

### Dependencies (build.gradle.kts)
```kotlin
dependencies {
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    implementation("io.coil-kt:coil-compose:2.5.0")
}
```

### Testing Strategy
- Unit tests: IntruderDetectionManager attempt counting
- Integration tests: Mock camera capture
- UI tests: Verify log display and deletion
- Manual tests: Cover camera, test graceful failure

### Privacy Considerations
- Photos stored in internal storage (app-private)
- Add "Auto-delete after 30 days" option
- GDPR compliance: User consent on first enable
- Add export feature for user's own records

---

## 2. Break-in Alerts

**Priority:** MEDIUM | **Complexity:** MEDIUM | **Effort:** 3-5 days

### Architecture
```
IntruderDetectionManager → AlertManager → NotificationManager
                                       → EmailService (optional)
```

### Database Schema
```kotlin
@Entity(tableName = "alert_settings")
data class AlertSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val enableNotifications: Boolean = true,
    val enableEmail: Boolean = false,
    val emailAddress: String? = null,
    val smtpServer: String = "smtp.gmail.com",
    val smtpPort: Int = 587,
    val smtpUsername: String? = null,
    val smtpPassword: String? = null, // Store encrypted
    val alertThreshold: Int = 3,
    val alertCooldown: Long = 300000 // 5 minutes
)
```

### Implementation

**AlertManager.kt**
```kotlin
class AlertManager @Inject constructor(
    private val context: Context,
    private val notificationManager: NotificationManager,
    private val emailService: EmailService,
    private val settingsDao: AlertSettingsDao
) {
    private val lastAlertTime = AtomicLong(0)
    
    suspend fun sendBreakInAlert(
        packageName: String,
        attempts: Int,
        photoPath: String? = null
    ) {
        val settings = settingsDao.getSettings()
        val now = System.currentTimeMillis()
        
        // Cooldown check
        if (now - lastAlertTime.get() < settings.alertCooldown) {
            return
        }
        
        lastAlertTime.set(now)
        
        if (settings.enableNotifications) {
            sendNotification(packageName, attempts, photoPath)
        }
        
        if (settings.enableEmail && settings.emailAddress != null) {
            sendEmailAlert(packageName, attempts, photoPath, settings)
        }
    }
    
    private fun sendNotification(packageName: String, attempts: Int, photoPath: String?) {
        val appName = context.packageManager.getApplicationLabel(
            context.packageManager.getApplicationInfo(packageName, 0)
        )
        
        val bitmap = photoPath?.let { 
            BitmapFactory.decodeFile(it)
        }
        
        val intent = Intent(context, IntruderLogsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, SECURITY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_security_alert)
            .setContentTitle("⚠️ Break-in Attempt Detected")
            .setContentText("$attempts failed attempts on $appName")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Someone tried to access $appName $attempts times and failed. Tap to view details.")
            )
            .apply {
                bitmap?.let { setLargeIcon(it) }
            }
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .build()
        
        notificationManager.notify(ALERT_NOTIFICATION_ID, notification)
    }
    
    private suspend fun sendEmailAlert(
        packageName: String,
        attempts: Int,
        photoPath: String?,
        settings: AlertSettingsEntity
    ) = withContext(Dispatchers.IO) {
        try {
            emailService.sendAlert(
                to = settings.emailAddress!!,
                subject = "⚠️ Bawn Security Alert",
                body = buildEmailBody(packageName, attempts),
                attachment = photoPath,
                smtpConfig = SmtpConfig(
                    server = settings.smtpServer,
                    port = settings.smtpPort,
                    username = settings.smtpUsername,
                    password = settings.smtpPassword
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send email alert", e)
            // Fallback: Store for retry
            scheduleEmailRetry(packageName, attempts, photoPath)
        }
    }
    
    private fun buildEmailBody(packageName: String, attempts: Int): String {
        return """
            <!DOCTYPE html>
            <html>
            <body>
                <h2 style="color: #FF0000;">🛡️ Bawn Security Alert</h2>
                <p><strong>Break-in attempt detected!</strong></p>
                <ul>
                    <li><strong>App:</strong> $packageName</li>
                    <li><strong>Failed Attempts:</strong> $attempts</li>
                    <li><strong>Time:</strong> ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())}</li>
                    <li><strong>Device:</strong> ${Build.MODEL}</li>
                </ul>
                <p>This email was sent automatically by Bawn App Locker.</p>
            </body>
            </html>
        """.trimIndent()
    }
}
```

**EmailService.kt (Using JavaMail)**
```kotlin
class EmailService {
    data class SmtpConfig(
        val server: String,
        val port: Int,
        val username: String?,
        val password: String?
    )
    
    suspend fun sendAlert(
        to: String,
        subject: String,
        body: String,
        attachment: String?,
        smtpConfig: SmtpConfig
    ) = withContext(Dispatchers.IO) {
        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", smtpConfig.server)
            put("mail.smtp.port", smtpConfig.port)
        }
        
        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(
                    smtpConfig.username ?: "",
                    smtpConfig.password ?: ""
                )
            }
        })
        
        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(smtpConfig.username))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
            setSubject(subject)
            
            if (attachment != null) {
                val multipart = MimeMultipart()
                
                // Body
                val textPart = MimeBodyPart().apply {
                    setContent(body, "text/html; charset=utf-8")
                }
                multipart.addBodyPart(textPart)
                
                // Attachment
                val attachmentPart = MimeBodyPart().apply {
                    attachFile(File(attachment))
                }
                multipart.addBodyPart(attachmentPart)
                
                setContent(multipart)
            } else {
                setContent(body, "text/html; charset=utf-8")
            }
        }
        
        Transport.send(message)
    }
}
```

**UI: AlertSettingsScreen.kt**
```kotlin
@Composable
fun AlertSettingsScreen(viewModel: AlertSettingsViewModel) {
    val settings by viewModel.settings.collectAsState()
    
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            SwitchPreference(
                title = "Enable Notifications",
                subtitle = "Show alert when break-in detected",
                checked = settings.enableNotifications,
                onCheckedChange = { viewModel.updateNotifications(it) }
            )
        }
        
        item {
            Divider()
            Text(
                "Email Alerts",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        item {
            SwitchPreference(
                title = "Enable Email Alerts",
                subtitle = "Send email on break-in attempts",
                checked = settings.enableEmail,
                onCheckedChange = { viewModel.updateEmailEnabled(it) }
            )
        }
        
        if (settings.enableEmail) {
            item {
                OutlinedTextField(
                    value = settings.emailAddress ?: "",
                    onValueChange = { viewModel.updateEmailAddress(it) },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
            }
            
            item {
                Text(
                    "⚠️ Email alerts require internet permission",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                Button(
                    onClick = { viewModel.testEmail() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Send Test Email")
                }
            }
        }
    }
}
```

### Dependencies
```kotlin
dependencies {
    implementation("com.sun.mail:android-mail:1.6.7")
    implementation("com.sun.mail:android-activation:1.6.7")
}
```

### Permissions (Optional)
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

### Testing
- Unit tests: Alert cooldown logic
- Integration tests: Mock SMTP server
- UI tests: Settings screen interactions
- Manual: Test with real Gmail account

---

## 3. Fake Crash Screen

**Priority:** MEDIUM | **Complexity:** LOW | **Effort:** 2-3 days

### Implementation

**FakeCrashDialog.kt**
```kotlin
@Composable
fun FakeCrashDialog(
    appName: String,
    onDismiss: () -> Unit,
    onSecretUnlock: () -> Unit
) {
    var tapCount by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    var tapJob by remember { mutableStateOf<Job?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        tapJob?.cancel()
                        tapCount++
                        
                        if (tapCount >= 3) {
                            onSecretUnlock()
                            tapCount = 0
                        } else {
                            tapJob = scope.launch {
                                delay(2000)
                                tapCount = 0
                            }
                        }
                    },
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text("$appName has stopped")
        },
        text = {
            Text("The app has stopped unexpectedly. You can report this problem to the developers.")
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Report")
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    )
}
```

**Integration in LockScreenActivity.kt**
```kotlin
class LockScreenActivity : FragmentActivity() {
    
    private val viewModel: LockScreenViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        
        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: run {
            finish()
            return
        }
        
        lifecycleScope.launch {
            val appSettings = viewModel.getAppSettings(packageName)
            
            setContent {
                BawnTheme {
                    if (appSettings.useFakeCrash) {
                        FakeCrashDialog(
                            appName = viewModel.getAppName(packageName),
                            onDismiss = { finishAndGoHome() },
                            onSecretUnlock = { showRealAuthPrompt() }
                        )
                    } else {
                        StandardLockScreen(
                            packageName = packageName,
                            onUnlock = { finish() }
                        )
                    }
                }
            }
        }
    }
    
    private fun finishAndGoHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }
    
    private fun showRealAuthPrompt() {
        setContent {
            BawnTheme {
                StandardLockScreen(
                    packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)!!,
                    onUnlock = { finish() }
                )
            }
        }
    }
}
```

**Database Schema Update**
```kotlin
@Entity
data class LockedAppEntity(
    @PrimaryKey val packageName: String,
    val isLocked: Boolean = true,
    val useFakeCrash: Boolean = false,
    val fakeCrashMessage: String? = null // Custom message
)
```

**Settings UI**
```kotlin
@Composable
fun AppLockSettings(app: LockedAppEntity, viewModel: AppSettingsViewModel) {
    Column {
        SwitchPreference(
            title = "Use Fake Crash Screen",
            subtitle = "Show fake error instead of lock screen",
            checked = app.useFakeCrash,
            onCheckedChange = { viewModel.updateFakeCrash(app.packageName, it) }
        )
        
        if (app.useFakeCrash) {
            InfoCard(
                message = "Secret Unlock: Triple-tap the error icon to show real lock screen"
            )
            
            OutlinedTextField(
                value = app.fakeCrashMessage ?: "The app has stopped unexpectedly.",
                onValueChange = { viewModel.updateCrashMessage(app.packageName, it) },
                label = { Text("Custom Error Message") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
```

### Testing
- Test triple-tap detection accuracy
- Verify back button returns to home
- Test with different tap speeds
- Accessibility: Ensure TalkBack announces correctly

---

## 4. Decoy Mode

**Priority:** MEDIUM | **Complexity:** HIGH | **Effort:** 4-6 days

### Architecture
```
BawnAccessibilityService → DecoyManager → WindowManager
                                              ↓
                                        DecoyOverlayView
```

### Implementation

**DecoyManager.kt**
```kotlin
class DecoyManager(
    private val context: Context,
    private val windowManager: WindowManager
) {
    private var activeDecoyView: View? = null
    private val decoyTemplates = DecoyTemplates()
    
    fun showDecoy(packageName: String, decoyType: DecoyType) {
        dismissDecoy() // Remove any existing decoy
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }
        
        activeDecoyView = ComposeView(context).apply {
            setContent {
                BawnTheme {
                    DecoyContent(
                        decoyType = decoyType,
                        packageName = packageName,
                        onSecretGesture = { showRealLock(packageName) },
                        onDismiss = { dismissDecoy() }
                    )
                }
            }
        }
        
        windowManager.addView(activeDecoyView, params)
    }
    
    fun dismissDecoy() {
        activeDecoyView?.let { view ->
            try {
                windowManager.removeView(view)
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, "View already removed")
            }
            activeDecoyView = null
        }
    }
    
    private fun showRealLock(packageName: String) {
        dismissDecoy()
        val intent = Intent(context, LockScreenActivity::class.java).apply {
            putExtra(EXTRA_PACKAGE_NAME, packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

enum class DecoyType {
    EMPTY_INBOX,
    ERROR_LOADING,
    NO_CONTENT,
    MAINTENANCE,
    NO_INTERNET
}
```

**DecoyContent.kt**
```kotlin
@Composable
fun DecoyContent(
    decoyType: DecoyType,
    packageName: String,
    onSecretGesture: () -> Unit,
    onDismiss: () -> Unit
) {
    var longPressCount by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    var gestureJob by remember { mutableStateOf<Job?>(null) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        gestureJob?.cancel()
                        longPressCount++
                        
                        if (longPressCount >= 2) {
                            onSecretGesture()
                            longPressCount = 0
                        } else {
                            gestureJob = scope.launch {
                                delay(3000)
                                longPressCount = 0
                            }
                        }
                    }
                )
            }
    ) {
        when (decoyType) {
            DecoyType.EMPTY_INBOX -> EmptyInboxDecoy()
            DecoyType.ERROR_LOADING -> ErrorLoadingDecoy()
            DecoyType.NO_CONTENT -> NoContentDecoy()
            DecoyType.MAINTENANCE -> MaintenanceDecoy()
            DecoyType.NO_INTERNET -> NoInternetDecoy()
        }
        
        // Invisible back button handler
        BackHandler { onDismiss() }
    }
}

@Composable
fun EmptyInboxDecoy() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Inbox,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = Color.Gray.copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "No messages",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.Gray
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Your inbox is empty",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.LightGray
        )
    }
}

@Composable
fun ErrorLoadingDecoy() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = Color(0xFFFFB74D)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "Couldn't load content",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Please check your connection and try again",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = { /* Do nothing */ }) {
            Text("Retry")
        }
    }
}

@Composable
fun NoInternetDecoy() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.WifiOff,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = Color.Red.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "No Internet Connection",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Connect to Wi-Fi or mobile data to continue",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.