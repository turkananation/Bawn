# 🛡️ Bawn - The Digital Fortress

**Bawn** is a high-performance, minimalist App Locker for Android, built with modern security protocols and Jetpack Compose. It serves as a secure overlay "fortress," intercepting unauthorized access to your selected applications with biometric or PIN verification.

> *"Bawn" (noun): A defensive wall or fortified enclosure.*

---

## 🚀 Features

* **⚡ Zero-Latency Locking:** Optimized overlay engine (`windowAnimationStyle="@null"`, `taskAffinity=""`) ensures the lock screen appears almost instantly, minimizing the "flicker" common in Accessibility-based lockers.
* **👆 Biometric Authentication:** Native integration with Android's `BiometricPrompt` API, supporting Fingerprint, Face Unlock, and Iris scanning.
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
* **Security:** `androidx.biometric`
* **Dependency Injection:** Manual DI (via `ViewModelFactory` for lightweight efficiency).

### 🏗️ Core Components

1.  **`BawnAccessibilityService` (The Watchman):**
    * The heart of the app. It listens for `TYPE_WINDOW_STATE_CHANGED` events.
    * Checks the foreground package against the `LockedAppDao`.
    * Triggers the `LockScreenActivity` if a match is found and no active session exists.

2.  **`RestrictionHelper` (The Gatekeeper):**
    * Detects if the app was installed via "untrusted" sources (ADB, Sideloading).
    * On Android 13+, it determines if the user needs to manually "Allow Restricted Settings" before enabling the service.

3.  **`LockScreenActivity` (The Shield):**
    * A specialized `FragmentActivity` (required for Biometrics).
    * Configured with `launchMode="singleInstance"` and `taskAffinity=""` to detach it from the main app stack for speed.
    * Disables screenshotting via `FLAG_SECURE`.

---

## 📱 Installation & Setup

### Prerequisites
* Android Studio Ladybug (or newer) recommended.
* Min SDK: 26 (Android 8.0)
* Target SDK: 35 (Android 15)

### Building the Project
1.  Clone the repository.
2.  Open in Android Studio.
3.  Sync Gradle with Project Files.
4.  Run on an emulator or physical device.

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
│   └── LockedAppEntity.kt # Database Table Schema
├── service/               # Background Services
│   ├── BawnAccessibilityService.kt # The core event listener
│   └── SessionManager.kt           # Manages unlock states
├── ui/                    # UI Layer (Compose)
│   ├── theme/             # Bawn Theme (Colors, Type)
│   ├── AppListViewModel.kt # State management for main list
│   ├── LockScreenActivity.kt # The actual PIN/Bio lock screen
│   └── MainActivity.kt    # The Dashboard / Config screen
└── util/                  # Utilities
    └── RestrictionHelper.kt # Logic for Android 13+ restrictions
```

---

## 🔮 Future Roadmap

* [ ] **Intruder Selfie:** Capture a photo using the front camera after 3 failed PIN attempts.
* [ ] **Pattern Lock:** Add a 3x3 pattern grid as an alternative to PIN.
* [ ] **Theming:** Allow users to change the "Shield" color (Gold, Neon Blue, Red).
* [ ] **Fake Crash:** An option to show a "Force Close" dialog instead of a lock screen to fool intruders.

---

## 📄 License

Copyright © 2025 Bawn Security. All Rights Reserved.

This project is licensed under the MIT License.

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.