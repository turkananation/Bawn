# 🛠️ Master Engineering Roadmap & Feasibility Report

**Scope:** Full Feature Set Analysis  
**Calibration:** Complexity (Low → Extreme) vs. Priority (P0 → P4)

---

## 🟢 Tier 1: The "Quick Wins" (Low Complexity)
*Features that rely on existing architectures (Room/Compose) or simple Android APIs. These are high-ROI tasks for filling out the app.*

### 1. Batch Operations (Lock/Unlock All)
* **Priority:** P1
* **Effort:** 1 Day
* **Tech:** SQL query update in `LockedAppDao`. Simple UI toggle in `MainActivity`.

### 2. Dark/Light Mode Toggle
* **Priority:** P1
* **Effort:** 2 Days
* **Tech:** Jetpack Compose `LocalTheme` provider switching.

### 3. Quick Actions (Notification Shade)
* **Priority:** P2
* **Effort:** 2 Days
* **Tech:** Add a `TileService` to Quick Settings tiles for one-tap "Lock All" or "Pause Protection".

### 4. Fake Crash Screen
* **Priority:** P2
* **Effort:** 2-3 Days
* **Tech:** A dedicated Dialog in `LockScreenActivity` that intercepts touches. Triple-tap gesture to dismiss.

### 5. Custom Themes
* **Priority:** P2
* **Effort:** 3 Days
* **Tech:** Define `ColorPalette` objects in Compose and store user preference in DataStore.

### 6. Usage Statistics
* **Priority:** P3
* **Effort:** 3 Days
* **Tech:** Add a `usage_logs` table to Room. Log events in `BawnAccessibilityService`. Visualize with a simple Compose chart.

### 7. Lock Screen Customization
* **Priority:** P3
* **Effort:** 3 Days
* **Tech:** Allow users to upload a background image (store URI) and change text colors in `LockScreenContent`.

### 8. Shake to Lock
* **Priority:** P3
* **Effort:** 2 Days
* **Tech:** `SensorEventListener` (Accelerometer) in a foreground service. Detect g-force threshold.

### 9. Panic Button
* **Priority:** P3
* **Effort:** 1 Day
* **Tech:** A floating widget or Quick Tile that instantly calls `lockAll()` and `clearSession()`.

### 10. Recommended Apps
* **Priority:** P4
* **Effort:** 1 Day
* **Tech:** Hardcoded list of package names (e.g., Banking, Social) to highlight in the `AppListViewModel`.

### 11. Widget Support
* **Priority:** P4
* **Effort:** 3-4 Days
* **Tech:** Glance (Jetpack Compose for Widgets) to build a home screen toggle.

---

## 🟡 Tier 2: Core Security & Logic (Medium Complexity)
*Features requiring new Android components (Receivers, Camera, Device Admin) but are well-documented.*

### 12. Uninstall Protection
* **Priority:** P0 (Critical)
* **Effort:** 3-5 Days
* **Tech:** `DeviceAdminReceiver`. Prevents the app from being uninstalled unless Admin is revoked (which we protect).

### 13. Pattern Lock
* **Priority:** P1
* **Effort:** 5-7 Days
* **Tech:** Custom Compose Canvas to draw nodes and paths. Logic to hash the node sequence string.

### 14. Intruder Selfie
* **Priority:** P1
* **Effort:** 5-7 Days
* **Tech:** `CameraX` library. Hidden `ImageCapture` use case on the lock screen. Permission handling is key.

### 15. Break-in Alerts
* **Priority:** P2
* **Effort:** 3-4 Days
* **Tech:** Logic hook in `LockScreenActivity` failure state. Send local notification or email via SMTP library.

### 16. App Groups (Profiles)
* **Priority:** P2
* **Effort:** 4 Days
* **Tech:** Database relation (One-to-Many). "Work Profile" locks apps A, B, C. "Home Profile" locks X, Y, Z.

### 17. Time-based Locking
* **Priority:** P2
* **Effort:** 5 Days
* **Tech:** `WorkManager` to schedule state changes. Needs exact alarm permissions for reliability.

### 18. Auto-lock on USB Debug
* **Priority:** P2
* **Effort:** 2 Days
* **Tech:** Listen for `Settings.Global.ADB_ENABLED` changes via `ContentObserver`.

### 19. Guest Mode
* **Priority:** P3
* **Effort:** 4 Days
* **Tech:** A temporary "Session" state that overrides the main database locks with a restrictive set.

### 20. Fingerprint Limit
* **Priority:** P3
* **Effort:** 3 Days
* **Tech:** Difficult API. We can't see whose fingerprint it is, only that it changed. We can invalidate keys on new enrollments via `KeyGenParameterSpec.Builder.setInvalidatedByBiometricEnrollment(true)`.

### 21. Screenshot Detection
* **Priority:** P3
* **Effort:** 3 Days
* **Tech:** `WindowManager.FLAG_SECURE` prevents it mostly. For detection, we need a `FileObserver` on the Screenshots folder (unreliable on Android 14+ due to scoped storage).

### 22. Child Lock Profile
* **Priority:** P3
* **Effort:** 4 Days
* **Tech:** Similar to Guest Mode but with a timer (Time-based locking logic).

---

## 🟠 Tier 3: Advanced Architecture (High Complexity)
*Features that fight against Android system limitations or require complex background services.*

### 23. Decoy Mode
* **Priority:** P2
* **Effort:** 6-8 Days
* **Tech:** Advanced `WindowManager` overlay. We draw a fake "Crash" or "No Internet" layout over the app, intercept touches, and only unlock on a specific gesture.

### 24. Local Backup & Restore
* **Priority:** P2
* **Effort:** 5-7 Days
* **Tech:** Export Room DB to JSON/ProtoBuf. Use Storage Access Framework (SAF) to save to user storage. Must encrypt the export file so PIN hashes aren't exposed.

### 25. Stealth Mode (Hide App Icon)
* **Priority:** P3
* **Effort:** 3 Days (High Risk)
* **Tech:** Disable the main Activity component via `PackageManager`. Launch via a specific dialer code (`BroadcastReceiver` on `NEW_OUTGOING_CALL` - Note: Google Play restricts this permission heavily).

### 26. Notification Privacy
* **Priority:** P3
* **Effort:** 7-10 Days
* **Tech:** `NotificationListenerService`. Intercept notifications from locked apps and "cancel" or "modify" them. High risk of being killed by OEM battery savers.

### 27. Two-Factor Lock
* **Priority:** P3
* **Effort:** 5 Days
* **Tech:** Logic flow: Biometric Success -> Trigger PIN Screen. Needs careful state management to avoid loops.

### 28. Tasker Integration
* **Priority:** P4
* **Effort:** 5 Days
* **Tech:** Expose a `BroadcastReceiver`/Intent API for external apps to trigger locks.

### 29. Duress PIN
* **Priority:** P4
* **Effort:** 3 Days
* **Tech:** A secondary hash in the DB. If entered, unlock the app but secretly wipe data or send a silent alert.

### 30. Honeypot Apps
* **Priority:** P4
* **Effort:** 7 Days
* **Tech:** Create fake app icons (Aliases) that open a dummy activity which logs every interaction.

### 31. Location-based Unlocking
* **Priority:** P4
* **Effort:** 8-10 Days
* **Tech:** Geofencing API. Requires `ACCESS_BACKGROUND_LOCATION` (Hard to get Play Store approval). High battery drain.

### 32. Geofencing Alerts
* **Priority:** P4
* **Effort:** 8-10 Days
* **Tech:** (Same as above)

---

## 🔴 Tier 4: The "Moonshots" (Extreme Complexity / Backend)
*Features requiring a dedicated backend team, cloud infrastructure, or R&D.*

### 33. Cloud Sync
* **Priority:** P2
* **Effort:** 1 Month+
* **Tech:** Firebase/AWS, User Auth, Encrypted Cloud Storage. Shifts app from "Offline" to "Online" (Privacy Policy overhaul).

### 34. Remote Lock
* **Priority:** P3
* **Effort:** 2-3 Weeks
* **Tech:** FCM (Firebase Cloud Messaging) to send push triggers to the device.

### 35. Wear OS Support
* **Priority:** P4
* **Effort:** 2 Weeks
* **Tech:** A separate Wear OS app module. Communication via DataLayer API to unlock phone apps from watch.

### 36. Family Sharing
* **Priority:** P4
* **Effort:** 1 Month+
* **Tech:** Complex backend user management (Parent/Child accounts).

### 37. Enterprise Mode (MDM)
* **Priority:** P4
* **Effort:** 2 Months+
* **Tech:** Android Enterprise API integration.

### 38. AI-Powered Locking
* **Priority:** P4
* **Effort:** R&D
* **Tech:** On-device ML (TFLite) to learn patterns. Overkill for this project.

### 39. Context-Aware Security
* **Priority:** P4
* **Effort:** R&D
* **Tech:** Heuristics engine combining time, location, and network state.

### 40. Behavioral Analysis
* **Priority:** P4
* **Effort:** R&D
* **Tech:** Analyzing touch pressure/velocity to detect unauthorized users (Biometric behavior).

### 41. Voice Command Support
* **Priority:** P4
* **Effort:** 1 Week
* **Tech:** Google Assistant App Actions (`actions.xml`).

---

## 👷 Senior Engineer's Verdict

**Phase 1 (This Sprint):**
Complete Tier 1 items (UI polish) + Uninstall Protection (Tier 2, P0). The app needs to feel finished and be secure against simple deletion.

**Phase 2 (Next Month):**
Focus on Pattern Lock and Intruder Selfie. These are the most marketable features users look for in an App Locker.

**Phase 3 (Long Term):**
Tackle Decoy Mode and Local Backup. Avoid Tier 4 (Cloud/Backend) unless we pivot to a subscription model to pay for servers.