I apologize for that. You are absolutely right—I aggressively summarized the "Developer Notes" and "Roadmap" sections, which contained critical technical context about the flicker trade-off and biometric implementation.

Here is the **complete** `README.md`, restoring all original sections while integrating our new PIN management features.

````markdown
# 🛡️ Bawn - The Digital Fortress

**Bawn** is a high-performance, minimalist App Locker for Android, built with modern security protocols and Jetpack Compose. It serves as a secure overlay "fortress," intercepting unauthorized access to your selected applications with biometric or PIN verification.

> *"Bawn" (noun): A defensive wall or fortified enclosure.*

---

## 🚀 Features

* **⚡ Zero-Latency Locking:** Optimized overlay engine (`windowAnimationStyle="@null"`, `taskAffinity=""`) ensures the lock screen appears almost instantly, minimizing the "flicker" common in Accessibility-based lockers.
* **🔐 Secure PIN & Biometrics:**
    * **Database-Backed Security:** PINs are hashed (SHA-256) and stored securely in a local Room Database.
    * **Biometric Auth:** Native integration with Android's `BiometricPrompt` API (Fingerprint, Face Unlock).
    * **Intelligent Routing:** The app automatically detects fresh installs and forces users to establish a master PIN before accessing the dashboard.
* **🔒 Smart Session Management:** "Unlock once, stay unlocked." The intelligent session manager keeps an app unlocked until the device screen turns off, preventing annoying repetitive lock screens.
* **🛑 Sideload Protection (Android 13+):** Includes a sophisticated detection system for "Restricted Settings," guiding users through the complex permissions flow required for manually installed (sideloaded) accessibility tools on modern Android.
* **🎨 Modern UI:** Built 100% with **Jetpack Compose**, featuring a dark-themed, neon-accented aesthetic (Basalt Grey & Neon Moss).
* **🕵️ Privacy First:**
    * Offline-only architecture (No internet permission).
    * `FLAG_SECURE` enabled on lock screens to prevent screenshots or Recents screen peeking.
    * Strict privacy: No data leaves the device.

---

## 🛠️ Architecture & Tech Stack

Bawn is built using **Clean Architecture** principles and the **MVVM (Model-View-ViewModel)** pattern.

### 📚 Tech Stack
* **Language:** [Kotlin](https://kotlinlang.org/)
* **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material3)
* **Database:** [Room](https://developer.android.com/training/data-storage/room) (SQLite abstraction)
* **Asynchronicity:** [Coroutines & Flow](https://kotlinlang.org/docs/coroutines-overview.html)
* **Security:** `androidx.biometric`, SHA-256 Hashing
* **Dependency Injection:** Manual DI (via `ViewModelFactory` for lightweight efficiency).

### 🏗️ Core Components

1.  **`BawnAccessibilityService` (The Watchman):**
    * The heart of the app. It listens for `TYPE_WINDOW_STATE_CHANGED` events.
    * Checks the foreground package against the `LockedAppDao`.
    * Triggers the `LockScreenActivity` if a match is found and no active session exists.

2.  **`BawnApplication` (The Overseer):**
    * Manages app-wide lifecycle events.
    * Enforces a "Self-Lock" mechanism: If the user leaves Bawn for more than 30 seconds, they must re-authenticate.

3.  **`LockScreenActivity` (The Shield):**
    * A specialized `FragmentActivity` (required for Biometrics).
    * Configured with `launchMode="singleInstance"` and `taskAffinity=""` to detach it from the main app stack for speed.
    * Disables screenshotting via `FLAG_SECURE`.

---

## 📱 Installation & Setup

### Prerequisites
* Android Studio Ladybug (or newer) recommended.
* Min SDK: 26 (Android 8.0)
* Target SDK: 36 (Android 16)

### First Run Experience
1.  **Install & Open:** Upon first launch, Bawn detects that no PIN is set.
2.  **Auto-Routing:** You are immediately redirected to the **Set PIN** screen.
3.  **Setup:** Create and confirm a 4-digit PIN.
4.  **Dashboard:** Once secured, the app allows access to the main dashboard.

### ⚠️ Important Note for Testing (Restricted Settings)
If you install the app via **USB Debugging (Android Studio)**, Android considers it a "Test" installation and **will not** trigger the "Restricted Setting" block.

**To test the Sideload Protection flow:**
1.  Generate a Signed Release APK (`Build > Generate Signed Bundle / APK`).
2.  Uninstall the debug version from your phone.
3.  Transfer the `app-release.apk` to your phone.
4.  Install it manually via your File Manager.
5.  You will now see the Red "Restricted" card in the app.

---

## 🧩 Developer Notes

### The "Flicker" Trade-off
Accessibility Services are **reactive**. The OS launches the target app *first*, then tells Bawn "App X just opened." Bawn then launches the lock screen on top.
* **Optimization:** We mitigate this by removing window animations (`@null`) and using `FLAG_ACTIVITY_NO_ANIMATION`.
* **Alternative:** "Window Manager" overlays are faster (20ms vs 200ms) but suffer from severe keyboard handling issues and back-stack bugs. The Activity-based approach is chosen for stability.

### Adaptive Icons
The app uses two different image assets:
1.  **Launcher Icon:** An Adaptive Icon (`mipmap/ic_launcher`) complying with Android standards (background + foreground layers).
2.  **In-App Logo:** A standalone PNG (`drawable/logo_bawn_gold.png`) used in the Compose UI, as `painterResource` cannot render adaptive XML icons directly.

### Biometric Implementation
**Crucial:** The `LockScreenActivity` must inherit from `FragmentActivity`, not `ComponentActivity`. The `androidx.biometric` library relies on `Fragment` support managers to display the system-level fingerprint dialog.

---

## 📂 Project Structure

```text
app/src/main/java/app/bawn
├── data/                  # Database Layer
│   ├── AppDatabase.kt     # Room Database instance
│   ├── LockedAppDao.kt    # Data Access Object
│   ├── LockedAppEntity.kt # Database Table Schema
│   ├── UserSecurityDao.kt # DAO for PIN/Auth data
│   └── UserSecurityEntity.kt # Schema for PIN hash
├── service/               # Background Services
│   ├── BawnAccessibilityService.kt # The core event listener
│   └── SessionManager.kt           # Manages unlock states
├── ui/                    # UI Layer (Compose)
│   ├── theme/             # Bawn Theme (Colors, Type)
│   ├── AppListViewModel.kt # State management for main list
│   ├── LockScreenActivity.kt # The actual PIN/Bio lock screen
│   ├── LockScreenContent.kt  # Reusable Lock UI components
│   ├── MainActivity.kt    # The Dashboard / Config screen
│   ├── SetPinActivity.kt  # Initial PIN setup flow
│   └── RestrictedHelpDialog.kt # Android 13+ help UI
└── util/                  # Utilities
    ├── RestrictionHelper.kt # Logic for Android 13+ restrictions
    └── SecurityUtils.kt     # PIN hashing and validation logic
````

-----

## 🔮 Future Roadmap

### 🔐 Security Enhancements

* [ ] **Intruder Selfie:** Capture a photo using the front camera after 3 failed PIN attempts
* [ ] **Break-in Alerts:** Email/notification when someone fails authentication multiple times
* [ ] **Fake Crash Screen:** Show a "Force Close" dialog instead of lock screen to deceive intruders
* [ ] **Decoy Mode:** Show fake empty app content instead of lock screen
* [ ] **Panic Button:** Quick gesture to instantly lock all apps and clear recent sessions
* [ ] **Stealth Mode:** Hide Bawn from app drawer and launcher

### 🎨 UI/UX Features

* [ ] **Pattern Lock:** Add 3x3 or 4x4 pattern grid as alternative to PIN
* [ ] **Custom Themes:** Multiple color schemes (Gold, Neon Blue, Red, Purple, Minimal White)
* [ ] **Lock Screen Customization:** Custom messages, wallpapers, or branding on lock screen
* [ ] **Widget Support:** Quick lock/unlock widget for home screen
* [ ] **Dark/Light Mode:** Toggle between dark and light theme variations

### 📊 Advanced Functionality

* [ ] **Usage Statistics:** Track how many times each app was locked/unlocked
* [ ] **Time-based Locking:** Auto-lock specific apps during work hours or bedtime
* [ ] **Location-based Unlocking:** Auto-unlock apps when at trusted locations (home, office)
* [ ] **App Groups:** Lock multiple apps with one toggle (Social Media, Banking, etc.)
* [ ] **Guest Mode:** Temporary access to selected apps without unlocking others
* [ ] **Child Lock Profile:** Separate profile with parental controls and time limits

### 🔄 App Management

* [ ] **Backup & Restore:** Export/import locked app configurations
* [ ] **Cloud Sync:** Sync settings across multiple devices (optional, privacy-conscious)
* [ ] **Recommended Apps:** Smart suggestions for apps that should be locked
* [ ] **Quick Actions:** Lock/unlock apps directly from notification shade
* [ ] **Batch Operations:** Lock/unlock multiple apps simultaneously

### 🛡️ Advanced Security

* [ ] **Screenshot Detection:** Alert when someone tries to screenshot lock screen
* [ ] **Uninstall Protection:** Require authentication before uninstalling Bawn
* [ ] **Notification Privacy:** Hide notification content for locked apps
* [ ] **Fingerprint Limit:** Allow only specific enrolled fingerprints
* [ ] **Two-Factor Lock:** Require both biometric AND PIN for sensitive apps
* [ ] **Auto-lock on USB Debug:** Detect developer mode and enforce extra security

### 🎯 Smart Features

* [ ] **AI-Powered Locking:** Learn usage patterns and suggest apps to lock
* [ ] **Context-Aware Security:** Adjust security level based on WiFi network or Bluetooth devices
* [ ] **Voice Command Support:** "Hey Google, lock my banking apps"
* [ ] **Tasker Integration:** Automation support for power users
* [ ] **Shake to Lock:** Lock all apps with device shake gesture

### 📱 Connectivity & Integration

* [ ] **Wear OS Support:** Unlock apps from smartwatch
* [ ] **Remote Lock:** Lock/unlock apps from web dashboard or another device
* [ ] **Family Sharing:** Parent can manage locks on children's devices
* [ ] **Enterprise Mode:** MDM integration for corporate device management

### 🧪 Experimental

* [ ] **Honeypot Apps:** Fake versions of locked apps that log intrusion attempts
* [ ] **Geofencing Alerts:** Notify if locked apps accessed outside safe zones
* [ ] **Behavioral Analysis:** Detect unusual unlock patterns and trigger extra verification
* [ ] **Duress PIN:** Special PIN that appears to unlock but secretly alerts trusted contacts

-----

## 📄 License

Copyright © 2025 Bawn Security. All Rights Reserved.

This project is licensed under the MIT License.

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

```
```