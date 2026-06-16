# 🚀 SHADOW APK — Quick Build Guide

## Build Option 1: GitHub Actions (Automated) ✅ RECOMMENDED

### For Debug APK (instant testing):
1. Push to `main` branch
2. Workflow auto-triggers (check Actions tab)
3. Download APK from Artifacts

### For Release APK (production):
1. Set GitHub Secrets:
   - `KEYSTORE_PASSWORD` — your keystore password
   - `KEY_PASSWORD` — your key password
   - `KEYSTORE_BASE64` — base64-encoded keystore

2. Push to `apk-release-v1.0` branch
3. APK builds + auto-releases on GitHub

---

## Build Option 2: Local Build (Manual)

### Prerequisites
```bash
# Install JDK 17 (or use Android Studio's bundled JDK)
java -version  # should be 17+

# Android SDK path
export ANDROID_HOME=$HOME/Library/Android/sdk  # macOS
# or
export ANDROID_HOME=/home/user/Android/Sdk  # Linux
```

### Step 1: Configure
```bash
cd shadow-apk
cp local.properties.example local.properties
# Edit local.properties with your paths
```

### Step 2: Build Debug APK
```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk (~45 MB)
```

### Step 3: Build Release APK (signed)
```bash
# Generate keystore (one time only)
keytool -genkey -v -keystore shadow-release.keystore \
  -alias shadow -keyalg RSA -keysize 2048 -validity 10000

# Update local.properties with keystore paths & passwords
# Then build:
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk (~15 MB)
```

### Step 4: Install on Device
```bash
# Connect Android device via USB (or use emulator)
adb install app/build/outputs/apk/debug/app-debug.apk

# Or open the APK file directly on device
# Settings → Unknown sources → tap APK file
```

---

## First Launch Setup

1. **Open SHADOW** on device
2. **Grant Permissions:**
   - Microphone → GRANT
   - Display over other apps → toggle ON
   - Accessibility → find "SHADOW AI", enable
   - Battery optimization → don't optimize

3. **Tap ACTIVATE SHADOW**
4. **Say "Shadow"** → floating bubble pulses cyan
5. **Try a command:** "Shadow, open Instagram"

---

## APK Specs

| Property | Value |
|----------|-------|
| **Package ID** | `com.shadow.ai` |
| **Min SDK** | Android 9.0 (API 28) |
| **Target SDK** | Android 15 (API 35) |
| **Version** | 1.0 (code: 1) |
| **Size (Debug)** | ~45 MB |
| **Size (Release)** | ~15 MB |

---

## Troubleshooting

### ❌ Gradle: `ANDROID_HOME not set`
```bash
export ANDROID_HOME=$HOME/Library/Android/sdk
./gradlew assembleDebug
```

### ❌ Build: `compileSdk 35 not found`
```bash
./gradlew installAndroidSdk
```

### ❌ Install: `app not installed`
```bash
# Try forcing reinstall
adb install -r app-debug.apk

# Check device logs
adb logcat | grep Shadow
```

### ❌ Bubble not showing
→ Settings → Apps → SHADOW → Display over other apps → toggle ON

### ❌ Voice not recognized
→ Ensure microphone permission + internet connection

---

## GitHub Actions Setup (CI/CD)

### Add Secrets to GitHub:
1. Go to **Settings → Secrets and variables → Actions**
2. Click **New repository secret**
3. Add:
   - `KEYSTORE_PASSWORD` = your password
   - `KEY_PASSWORD` = your password
   - `KEYSTORE_BASE64` = base64 keystore

### Generate KEYSTORE_BASE64:
```bash
base64 shadow-release.keystore | tr -d '\n' | pbcopy
# Paste into GitHub secret
```

### Deploy to GitHub Releases:
```bash
git push origin apk-release-v1.0
# Workflow runs → creates Release with APK attached
```

---

## Download APK

- **Debug:** GitHub Actions Artifacts (30-day retention)
- **Release:** GitHub Releases (permanent)

Both ready to install on Android 9.0+! 🎉
