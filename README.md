# 🧵 Stitch — Peer Screen Time Accountability Manager

[![Build Stitch Android APK](https://github.com/poketrainerlox/stitch-app/actions/workflows/build-apk.yml/badge.svg)](https://github.com/poketrainerlox/stitch-app/actions/workflows/build-apk.yml)

**Stitch** is a modern, high-discipline Android peer-accountability app. Instead of self-imposed blockers that are trivial to bypass, Stitch allows friends to create mutual focus pacts—locking distracting apps on your device where **only your friend (or a physical exercise challenge) can unlock them**.

---

## 📲 Direct APK Download (No Unzipping Required)

Download the compiled Android APK directly onto your phone:

👉 **[Download Latest Stitch-Focus-Guard.apk](https://github.com/poketrainerlox/stitch-app/releases/latest)**

*Tap the `.apk` file on your Android phone and select "Install". (Ensure "Install unknown apps" is enabled for your browser if prompted).*

---

## ✨ Features Overview

### 1. 📊 Home Dashboard
- **Real-time Screen Time Tracking**: Native Android `UsageStatsManager` integration calculates exact daily app usage and categories.
- **Dynamic Donut Gauge**: Animated progress gauge comparing usage against your personalized daily budget limit.
- **Focus Streak & Pacts Sealed**: Tracks continuous daily focus discipline.
- **Instant Focus Lockdown**: One-tap emergency lockdown on top social apps.

### 2. 👥 Real Friends & Accountability Circle
- **No Draft / Fake People**: Fully backed by real-time sync with local/cloud hybrid support.
- **Add Friends via Invite Code**: Every user receives a unique 6-character code (e.g., `K9X2P4`).
- **One-Tap WhatsApp & Link Sharing**: Generates shareable deep links (`https://stitch.app/join?code=...` & `stitch://join?code=...`).
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

## 🔥 Firebase Setup (Optional for Multi-Device Cloud Sync)

The app now has a **smart hybrid engine**:
- **Offline / Local Mode**: Works immediately without any setup, preventing "API key invalid" errors.
- **Cloud Mode**: To enable multi-device real-time sync between separate phones:
  1. Create a project at [Firebase Console](https://console.firebase.google.com/).
  2. Enable **Authentication** (Email/Password) and **Cloud Firestore**.
  3. Download `google-services.json` and replace `app/google-services.json`.
  4. (Optional for CI/CD) Encode it to Base64 and add it as a GitHub Secret `GOOGLE_SERVICES_JSON_BASE64`.

---

## 🛠️ Local Development & Build

```bash
# Debug build
./gradlew assembleDebug

# Output APK location:
# app/build/outputs/apk/debug/app-debug.apk
```
