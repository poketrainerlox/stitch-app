# 🧵 Stitch — Peer Screen Time Accountability Manager

[![Build Stitch Android APK](https://github.com/OWNER/REPO/actions/workflows/build-apk.yml/badge.svg)](https://github.com/OWNER/REPO/actions/workflows/build-apk.yml)

**Stitch** is a modern, high-discipline Android peer-accountability app. Instead of self-imposed blockers that are trivial to bypass, Stitch allows friends to create mutual focus pacts—locking distracting apps on your device where **only your friend (or a physical exercise challenge) can unlock them**.

---

## ✨ Features Overview

### 1. 📊 Home Dashboard
- **Real-time Screen Time Tracking**: Native Android `UsageStatsManager` integration calculates exact daily app usage and categories.
- **Dynamic Donut Gauge**: Animated progress gauge comparing usage against your personalized daily budget limit.
- **Focus Streak & Pacts Sealed**: Tracks continuous daily focus discipline.
- **Instant Focus Lockdown**: One-tap emergency lockdown on top social apps.

### 2. 👥 Real Friends & Accountability Circle
- **No Draft / Fake People**: Fully backed by Firebase Firestore in real-time.
- **Add Friends via Invite Code**: Every user receives a unique 6-character code (e.g., `K9X2P4`).
- **One-Tap WhatsApp & Link Sharing**: Generates shareable deep links (`https://stitch.app/join?code=...` and `stitch://join?code=...`).
- **Live Status & Nudges**: Send haptic focus nudges and enforce emergency locks on wandering buddies.

### 3. 🔒 Mutual Focus Pacts
- **App Selection**: Select any installed app from your device (Instagram, TikTok, YouTube, Games, etc.).
- **Flexible Durations**: 25m Pomodoro, 45m Deep Work, 60m Sprint, 90m Exam Prep, or custom.
- **Strict Lockdown Overlay**: Displays a sleek lock shield over blocked apps using Android `WindowManager` and `AccessibilityService`.
- **Emergency 5-Minute Pass**: Request temporary access with a reason sent directly to your friend's device for approval.
- **Physical Penalty Unlock**: Complete a 25 push-up challenge to save your streak and void penalties without breaking discipline.

### 4. 📅 Calendar & Smart Auto-Scheduling
- **Week Calendar Strip**: Visual week view tracking planned and completed focus routines.
- **AI Smart Routine Auto-Scheduler**: Detects peak distraction windows (e.g. 2:00 PM – 4:00 PM afternoon dip, 11:00 PM – 7:00 AM sleep vault) and automatically applies protective app shields.
- **Custom Schedule Blocks**: Define repeating schedules by day of week (Weekdays, Weekends, Daily) and category.

### 5. 🛡️ Security, Access & Permissions
- **App Usage Access**: Reads device screen time telemetry.
- **Display Over Other Apps (`SYSTEM_ALERT_WINDOW`)**: Renders the non-intrusive focus shield over restricted apps.
- **Accessibility Service (`StitchAccessibilityService`)**: Provides instant zero-lag foreground app detection with minimal battery impact.
- **Cloud Firestore Security Rules**: Complete role-based access control (`firestore.rules`) ensuring data privacy between friends.

---

## 🚀 GitHub Actions — Download the Final APK

This repository includes a pre-configured GitHub Actions workflow (`.github/workflows/build-apk.yml`) that automatically compiles and packages the final `.apk` file:

1. Push this codebase to your GitHub repository:
   ```bash
   git init
   git add .
   git commit -m "Initial commit of Stitch Android app"
   git remote add origin https://github.com/YOUR_USERNAME/stitch-app.git
   git push -u origin main
   ```
2. Navigate to the **Actions** tab on your GitHub repository.
3. Click on the latest workflow run: **"Build Stitch Android APK"**.
4. Scroll to the **Artifacts** section at the bottom to download `Stitch-Focus-Guard-Debug-APK` or `Stitch-Focus-Guard-Release-APK`!

---

## 🛠️ Local Development & Build

### Prerequisites
- Android Studio Ladybug / Iguana or later
- JDK 17
- Android SDK 35 (Android 15)
- Android device or emulator with API 26+ (Android 8.0+)

### Building via Terminal
```bash
# Debug build
./gradlew assembleDebug

# Output APK location:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 📂 Project Structure

```
stitch-app/
├── .github/workflows/build-apk.yml   # CI/CD automated APK build
├── app/
│   ├── build.gradle.kts             # App dependencies & SDK config
│   ├── google-services.json         # Firebase client config
│   └── src/main/
│       ├── AndroidManifest.xml      # Permissions, deep links & services
│       ├── java/com/stitch/app/
│       │   ├── StitchApp.kt         # Application class & notification channels
│       │   ├── MainActivity.kt      # Main entry point & deep link routing
│       │   ├── di/                  # Hilt Dependency Injection
│       │   ├── data/
│       │   │   ├── model/           # StitchUser, Friendship, Pact, FocusSchedule
│       │   │   └── repository/      # Auth, Friends, Pacts, Schedule, Usage repos
│       │   ├── service/             # Overlay, Accessibility, FCM & Foreground services
│       │   └── ui/
│       │       ├── theme/           # Warm dark palette & clean typography
│       │       ├── navigation/      # Jetpack Compose Navigation graph
│       │       ├── components/      # Gauges, banners, bottom navigation
│       │       └── screens/         # Home, Friends, Pacts, Schedule, Settings, Auth
│       └── res/                     # Strings, icons, accessibility configs
├── firestore.rules                  # Production Firebase security rules
└── settings.gradle.kts              # Root Gradle configuration
```
