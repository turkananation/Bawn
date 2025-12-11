# 🏯 Bawn: Command Center & Engineering Mainframe

**Operational Status:** 🟡 Active Development
**Security Protocol:** Zero Latency
**Theme System:** Neon Fortress

> **⚠️ AUTHORIZED PERSONNEL ONLY**
> This dashboard tracks the construction of the Bawn defense system. All contributors must adhere to the **Standard Operating Procedures (SOP)** listed below before deploying code.

---

## 📡 Mission Parameters
* **Objective:** Build the world's fastest "Digital Fortress" for Android.
* **Target SDK:** Android 16 (API 36) | **Min SDK:** Android 8.0 (API 26)
* **Core Philosophy:** Active Defense. We don't just lock apps; we aggressively block intruders.

---

## 🔐 Standard Operating Procedures (SOP)
*Strict engineering protocols derived from the Product Specification.*

### 1. Visual Protocol: "The Neon Fortress"
All UI components must adhere to the high-contrast aesthetic.
* **Colors:** Use `LocalNeonColors` in `Theme.kt`.
    * `Gold (0xFFFFD700)`: Primary Accents
    * `Green (0xFF00FF9D)`: Safe/Active States
    * `Red (0xFFFF3333)`: Alerts/Restricted States
* **Components:** All cards must use a `BorderStroke` to simulate a "glowing edge".
* **Motion:** Use `Crossfade` for tab switching. **No sliding animations** (maintain stability).

### 2. Security Critical Path
* **Re-Authentication:** Accessing *Tab 3 (Security Lab)* or *Tab 4 (Settings)* **MUST** trigger a biometric/PIN re-check if the session is > 5 minutes old.
* **Data Safety:** Destructive actions (e.g., turning off Uninstall Protection) require the Master PIN.

---

## 🗺️ Tactical Roadmap

### 🟢 Phase 1: Perimeter Defenses (Quick Wins)
*Status: Active Construction*

- [ ] **1. Batch Operations (Lock/Unlock All)** `P1` `1 Day`
  > **Tech Brief:** SQL query update in `LockedAppDao`. Simple UI toggle in `MainActivity`.

- [ ] **2. Dark/Light Mode Toggle** `P1` `2 Days`
  > **Tech Brief:** Jetpack Compose `LocalTheme` provider switching.

- [ ] **3. Quick Actions (Notification Shade)** `P2` `2 Days`
  > **Tech Brief:** `TileService` for Quick Settings tiles (e.g., "Pause Protection").

- [ ] **4. Fake Crash Screen** `P2` `2-3 Days`
  > **Tech Brief:** Dedicated Dialog in `LockScreenActivity` intercepting touches. Triple-tap to dismiss.

- [ ] **5. Custom Themes** `P2` `3 Days`
  > **Tech Brief:** `ColorPalette` objects in Compose stored in DataStore.

- [ ] **6. Usage Statistics** `P3` `3 Days`
  > **Tech Brief:** `usage_logs` table in Room. Log events in `BawnAccessibilityService`.

- [ ] **7. Lock Screen Customization** `P3` `3 Days`
  > **Tech Brief:** User-uploaded background images (URI storage) and dynamic text colors.

- [ ] **8. Shake to Lock** `P3` `2 Days`
  > **Tech Brief:** `SensorEventListener` (Accelerometer) in a foreground service.

- [ ] **9. Panic Button** `P3` `1 Day`
  > **Tech Brief:** Floating widget or Quick Tile that calls `lockAll()` and `clearSession()`.

- [ ] **10. Recommended Apps** `P4` `1 Day`
  > **Tech Brief:** Hardcoded list (Banking, Social) highlighted in `AppListViewModel`.

- [ ] **11. Widget Support** `P4` `3-4 Days`
  > **Tech Brief:** Glance (Jetpack Compose for Widgets) for Home Screen control.


### 🟡 Phase 2: Core Security Protocols
*Status: Planning / High Priority*

- [ ] **12. Uninstall Protection** `P0` `CRITICAL` `3-5 Days`
  > **Tech Brief:** `DeviceAdminReceiver`. Prevent app removal by unauthorized users.

- [ ] **13. Pattern Lock** `P1` `5-7 Days`
  > **Tech Brief:** Custom Compose Canvas for nodes/paths. Hashing node sequences.

- [ ] **14. Intruder Selfie** `P1` `5-7 Days`
  > **Tech Brief:** `CameraX` hidden `ImageCapture` on failed attempts.

- [ ] **15. Break-in Alerts** `P2` `3-4 Days`
  > **Tech Brief:** Logic hook in failure state. Local notification or SMTP email.

- [ ] **16. App Groups (Profiles)** `P2` `4 Days`
  > **Tech Brief:** DB relations (One-to-Many). "Work Profile" vs "Home Profile".

- [ ] **17. Time-based Locking** `P2` `5 Days`
  > **Tech Brief:** `WorkManager` scheduling. Requires exact alarm permissions.

- [ ] **18. Auto-lock on USB Debug** `P2` `2 Days`
  > **Tech Brief:** Listen for `Settings.Global.ADB_ENABLED` via `ContentObserver`.

- [ ] **19. Guest Mode** `P3` `4 Days`
  > **Tech Brief:** Temporary "Session" state overriding main database locks.

- [ ] **20. Fingerprint Limit** `P3` `3 Days`
  > **Tech Brief:** Invalidate keys on new enrollments via `KeyGenParameterSpec`.

- [ ] **21. Screenshot Detection** `P3` `3 Days`
  > **Tech Brief:** `FLAG_SECURE` (Prevention) + `FileObserver` (Detection backup).

- [ ] **22. Child Lock Profile** `P3` `4 Days`
  > **Tech Brief:** Timer-based locking logic similar to Guest Mode.


### 🟠 Phase 3: Heavy Armor (Advanced)
*Status: Backlog*

- [ ] **23. Decoy Mode** `P2`
- [ ] **24. Local Backup & Restore** `P2`
- [ ] **25. Stealth Mode** `P3`
- [ ] **26. Notification Privacy** `P3`
- [ ] **27. Two-Factor Lock** `P3`
- [ ] **28. Tasker Integration** `P4`
- [ ] **29. Duress PIN** `P4`
- [ ] **30. Honeypot Apps** `P4`
- [ ] **31. Location-based Unlocking** `P4`
- [ ] **32. Geofencing Alerts** `P4`

### 🔴 Phase 4: Moonshot R&D
*Status: Classified / Experimental*

- [ ] **33. Cloud Sync** `P2`
- [ ] **34. Remote Lock** `P3`
- [ ] **35. Wear OS Support** `P4`
- [ ] **36. Family Sharing** `P4`
- [ ] **37. Enterprise Mode (MDM)** `P4`
- [ ] **38. AI-Powered Locking** `P4`
- [ ] **39. Context-Aware Security** `P4`
- [ ] **40. Behavioral Analysis** `P4`
- [ ] **41. Voice Command Support** `P4`

---

## 🧬 Legend & Taxonomy

| Rank | Definition |
| :--- | :--- |
| `P0` | **Critical:** Blockers. The fortress is vulnerable without this. |
| `P1` | **High:** Core functionality. Required for v1.0 Release. |
| `P2` | **Medium:** Essential upgrades. Scheduled for v1.1+. |
| `P3` | **Low:** Tactical advantages. Nice to have. |
| `P4` | **Experimental:** R&D projects. |

---
> **System Log:** Last updated by High Command.