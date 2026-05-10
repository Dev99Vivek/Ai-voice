# SHADOW — Android APK Build Guide

## Requirements

| Tool | Version |
|------|---------|
| Android Studio | Ladybug (2024.2) or newer |
| JDK | 17 (bundled with Android Studio) |
| Kotlin | 2.0.0 |
| compileSdk | 35 (Android 15) |
| minSdk | 28 (Android 9) |
| Gradle | 8.7+ |

## Step 1 — Open in Android Studio

1. Clone / copy this `shadow-apk/` directory to your machine
2. Open Android Studio → **File → Open** → select the `shadow-apk` folder
3. Wait for Gradle sync (downloads ~300 MB of dependencies first time)

## Step 2 — Generate launcher icons (required)

The placeholder adaptive icon must be replaced with real PNGs before building:

1. In Android Studio: right-click `app/src/main/res` → **New → Image Asset**
2. Select **Launcher Icons (Adaptive and Legacy)**
3. For foreground: use the SHADOW "S" logo or any PNG/SVG
4. Set background color to `#080C10`
5. Android Studio generates all mipmap densities automatically

Alternatively: use https://adapticon.toplay.me to generate and drop the PNGs into the mipmap folders.

## Step 3 — (Optional) Porcupine wake word

For always-on wake word detection without draining battery:

1. Get a free access key from https://console.picovoice.ai (free tier: 3 wake words)
2. Un-comment the Porcupine dependency in `app/build.gradle.kts`:
   ```kotlin
   implementation(libs.porcupine)
   ```
3. Add your access key to `local.properties`:
   ```
   PORCUPINE_KEY=your_key_here
   ```
4. Download the `shadow_android.ppn` file from your Picovoice console
5. Place it in `app/src/main/assets/shadow_android.ppn`
6. Un-comment the Porcupine block in `WakeWordDetector.kt` and remove the fallback

Without Porcupine, SHADOW uses Android's built-in `SpeechRecognizer` as the wake mechanism.

## Step 4 — Build debug APK

```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

Or in Android Studio: **Build → Build Bundle(s)/APK(s) → Build APK(s)**

## Step 5 — Build release APK (for distribution)

### Create a keystore (first time only):
```bash
keytool -genkey -v -keystore shadow-release.keystore \
  -alias shadow -keyalg RSA -keysize 2048 -validity 10000
```

### Add signing config to `local.properties`:
```properties
KEYSTORE_PATH=/path/to/shadow-release.keystore
KEYSTORE_PASSWORD=your_store_password
KEY_ALIAS=shadow
KEY_PASSWORD=your_key_password
```

### Add signing to `app/build.gradle.kts`:
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file(project.property("KEYSTORE_PATH") as String)
            storePassword = project.property("KEYSTORE_PASSWORD") as String
            keyAlias = project.property("KEY_ALIAS") as String
            keyPassword = project.property("KEY_PASSWORD") as String
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

## Step 6 — Install on device

```bash
# Debug
adb install app/build/outputs/apk/debug/app-debug.apk

# Release
adb install app/build/outputs/apk/release/app-release.apk
```

Or: copy the APK to the phone and open it (enable "Install from unknown sources" in Settings first).

## Step 7 — First launch setup

1. Open SHADOW on the device
2. Grant all permissions on the onboarding screen:
   - **Microphone** → tap GRANT, allow
   - **Overlay** → tap GRANT, toggle SHADOW on in the list
   - **Accessibility** → tap GRANT, find SHADOW AI, enable it
   - **Battery Optimization** → tap GRANT, select "Don't optimize"
3. Tap **ACTIVATE SHADOW**
4. Say **"Shadow"** — the bubble will pulse cyan and start listening

## How SHADOW Works

```
[Always listening] WakeWordDetector (AudioRecord)
         │
         ▼ "Shadow" detected
[Voice capture] VoiceRecognitionEngine (Android SpeechRecognizer)
         │
         ▼ transcript
[NLU] IntentProcessor → ShadowAction(type, app, target, ...)
         │
         ▼
[Dispatch] ActionExecutor
    ├── AppController        → launch/close apps
    ├── ShadowAccessibility  → tap, type, scroll, swipe
    ├── OCREngine (ML Kit)   → read text from screen
    ├── System APIs          → WiFi, Bluetooth, volume, brightness
    └── RoutineEngine        → multi-step macros
         │
         ▼
[Overlay] OverlayManager → show result in floating bubble
[Memory] MemoryStore (Room) → log command history
```

## Voice Command Examples

| Say | What happens |
|-----|-------------|
| "Shadow, open Instagram" | Launches Instagram |
| "Shadow, turn on flashlight" | Toggles torch |
| "Shadow, turn off WiFi" | Opens WiFi settings |
| "Shadow, scroll down" | Scrolls active view |
| "Shadow, click Send" | Finds & taps "Send" element |
| "Shadow, type Hello World" | Types into focused input |
| "Shadow, read the screen" | OCR reads all visible text |
| "Shadow, gaming mode" | Runs Gaming Mode routine |
| "Shadow, send message to John saying I'm on my way" | Opens WhatsApp, sends message |
| "Shadow, take screenshot" | Captures screen |
| "Shadow, set brightness to 80" | Sets screen brightness |
| "Shadow, volume 50" | Sets media volume to 50% |

## Updating the APK Download URL

Once the APK is built and hosted (e.g. GitHub Releases, Firebase Storage, your CDN):

1. Connect to the Replit project database
2. Update the `apk_versions` table:
   ```sql
   UPDATE apk_versions SET download_url = 'https://your-host/shadow-v1.0.apk' WHERE version = '1.0';
   ```
3. The landing site will automatically show the Download button

Or via the API:
```bash
curl -X PATCH /api/apk/versions/1 \
  -H "Content-Type: application/json" \
  -d '{"downloadUrl": "https://your-host/shadow-v1.0.apk"}'
```

## Architecture: Key Files

```
shadow-apk/
├── app/src/main/
│   ├── AndroidManifest.xml              ← All permissions + service declarations
│   ├── java/com/shadow/ai/
│   │   ├── ShadowApplication.kt         ← App singleton, notification channels
│   │   ├── models/ShadowAction.kt       ← All action types + data models
│   │   ├── data/MemoryStore.kt          ← Room DB: history, preferences, routines
│   │   ├── voice/
│   │   │   ├── WakeWordDetector.kt      ← Always-on "Shadow" trigger
│   │   │   └── VoiceRecognitionEngine.kt← Speech-to-text via SpeechRecognizer
│   │   ├── engine/
│   │   │   ├── IntentProcessor.kt       ← Text → ShadowAction (rule-based NLU)
│   │   │   ├── ActionExecutor.kt        ← Dispatches every action type
│   │   │   ├── AppController.kt         ← 50+ app alias map + fuzzy search
│   │   │   └── RoutineEngine.kt         ← Multi-step macro execution
│   │   ├── vision/OCREngine.kt          ← ML Kit on-device OCR
│   │   ├── services/
│   │   │   ├── ShadowForegroundService.kt  ← Main background loop
│   │   │   └── ShadowAccessibilityService.kt ← UI automation engine
│   │   ├── overlay/OverlayManager.kt    ← Floating draggable bubble
│   │   ├── receivers/BootReceiver.kt    ← Auto-start on boot
│   │   └── ui/
│   │       ├── MainActivity.kt          ← Compose onboarding screen
│   │       └── PermissionHelper.kt      ← Permission state + intent helpers
│   └── res/
│       ├── layout/overlay_shadow_bubble.xml
│       ├── drawable/                    ← Bubble, panel, state dot, notification icon
│       ├── values/colors.xml themes.xml strings.xml
│       └── xml/accessibility_service_config.xml
└── BUILD_GUIDE.md                       ← This file
```

## Troubleshooting

**"SHADOW bubble not showing"**
→ Grant SYSTEM_ALERT_WINDOW permission in Settings → Apps → SHADOW → Display over other apps

**"Voice not recognized"**
→ Ensure microphone permission granted + internet connection (SpeechRecognizer sends audio to Google by default)
→ For offline: integrate Vosk (vosk-api.org) — same interface, replace `VoiceRecognitionEngine.kt`

**"Tap/click actions not working"**
→ Ensure Accessibility Service is enabled in Settings → Accessibility → SHADOW AI

**"SHADOW stops when screen turns off"**
→ Grant Battery Optimization exemption in Settings → Battery → App Battery → SHADOW

**Gradle build fails on M1/M2 Mac**
→ In `gradle.properties` add: `org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8 -XX:+UseG1GC`
