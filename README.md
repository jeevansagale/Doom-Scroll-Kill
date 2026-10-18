# Doom-Scroll-Kill (Doomscroller)

<p align="center">
  <img src="app_logo.png" alt="DoomScroller Logo" width="220" />
</p>

> A focused, privacy-respecting Android application engineered to interrupt doomscrolling habits through deterministic reminder escalation, penalty cooldowns, and a clean Bauhaus design language.

---

## Overview

Modern social media and short-form video platforms employ aggressive retention mechanisms. **Doomscroller** provides a system-level countermeasure. Unlike passive screen-time dashboards, Doomscroller tracks active foreground application use, issues escalating periodic reminders, and activates full-screen enforcement cooldowns when reminders are repeatedly ignored.

---

## Key Features

### 1. Three-Parameter Escalation Architecture ($X / Y / Z$)
- **$X$ — Reminder Interval**: Configurable threshold (1–60 minutes, default: 5 min) defining how long a monitored app may be used before a reminder appears.
- **$Y$ — Target Monitored App**: Isolated session tracking per configured application (e.g., Instagram, YouTube, TikTok).
- **$Z$ — Ignored Reminder Limit**: Number of consecutive reminders (1–10, default: 3) the user may ignore before escalating to enforcement.
- **Strict Ignore Semantics**: Continuing to use a monitored app beyond the next interval without acknowledging the reminder increments the ignored counter.
- **Session Isolation**: Switching to unmonitored apps pauses qualifying use without incrementing counters. Switching between monitored apps maintains independent session states.

### 2. Dual-Tier Warning Enforcement & Safe "Leave" Action
- **Heads-Up / Full-Screen Intent**: Employs high-priority Android alarm channels with `fullScreenIntent` to display warnings without violating Background Activity Launch (BAL) restrictions.
- **Direct Overlay Launch**: Supports `SYSTEM_ALERT_WINDOW` ("Display over other apps") to directly project the Warning UI on top of distracting apps when escalation occurs.
- **5-Minute Penalty Cooldown**: Exceeded limits trigger an unavoidable 5-minute penalty cooldown with a live countdown timer.
- **Safe Exit Navigation**: The **"EXIT TO HOME SCREEN"** action triggers `Intent.ACTION_MAIN, CATEGORY_HOME` and calls `moveTaskToBack(true)` to return the user to their launcher without forcefully terminating unrelated applications.

### 3. High-Performance Low-Overhead Usage Engine
- **$O(1)$ Aggregated Usage Queries**: Uses Android's pre-calculated daily aggregates via `queryUsageStats(INTERVAL_DAILY)` combined with a rolling 60-second window for foreground package detection.
- **Battery-Friendly Polling**: 10-second active polling when the screen is ON; throttled to 60-second intervals when the screen is OFF.
- **100% On-Device & Zero Telemetry**: All calculations and persistence happen locally in encrypted private storage. No data is ever collected or transmitted.

### 4. Bauhaus Design System
Adheres to the historic Bauhaus philosophy—*form follows function*—without skeuomorphic gradients or distractions:
- **Palette**: Warm canvas (`#F7F5F0`), stark black typography/borders (`#0A0A0A`), and primary accents: Bauhaus Red (`#D02020`), Blue (`#1848A0`), and Yellow (`#F0B800`).
- **Zero Rounded Corners**: Sharp, geometric `RoundedCornerShape(0.dp)` cards, panels, and indicators.
- **Touch Ergonomics**: All interactive elements and action buttons enforce touch targets $\ge 48\text{ dp}$.
- **Adaptive Layout**: Screens utilize `LazyVerticalGrid(columns = GridCells.Adaptive(300.dp))` for responsive rendering across standard phones, foldables, and tablets.

---

## Screen Highlights

1. **Dashboard**: Live screen time accumulation across monitored targets, active penalty cooldown banners with countdowns, and quick-access quota cards.
2. **Choose Apps**: Alphabetical catalog of installed launchable applications with geometric toggle indicators.
3. **App Rule Editor**: Custom per-application daily quotas, reminder intervals, and active monitoring toggles.
4. **Warning Interruption**: Stark red warning banner, target app identity, detailed metrics (reminders ignored, interval, qualifying use), cooldown countdown, and prominent "EXIT TO HOME SCREEN" button.
5. **System Configuration**: Real-time controls for Parameter $X$ (Interval), Parameter $Z$ (Ignored Limit), rolling 15-minute frequency cap, and Overlay Permission status.

---

## Tech Stack & Architecture

- **Language**: Kotlin 2.0
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: Clean Architecture / MVVM with pure Kotlin domain evaluators
- **Asynchrony**: Kotlin Coroutines & `StateFlow`
- **Platform APIs**:
  - `UsageStatsManager` (Usage Access)
  - `NotificationManager` (Channels, High-Priority Heads-Up & Full-Screen Intent)
  - `PowerManager` (Interactive screen-state detection)
  - `SYSTEM_ALERT_WINDOW` (Overlay enforcement)

---

## Building & Installation

### Prerequisites
- Android Studio Ladybug (2024.2.1+) or command-line JDK 17+
- Android SDK with Platform 35 / API 35 installed

### Build from Source
```bash
# Clone the repository
git clone https://github.com/jeevansagale/Doom-Scroll-Kill.git
cd Doom-Scroll-Kill

# Run unit tests
./gradlew testDebugUnitTest

# Build debug APK
./gradlew assembleDebug
```

### Install onto Device
```bash
# With USB debugging enabled:
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## Permissions Required

| Permission | Purpose |
|---|---|
| `PACKAGE_USAGE_STATS` | Detects active foreground application and tracks screen time. |
| `POST_NOTIFICATIONS` | Delivers reminder alerts and high-priority cooldown warning notifications. |
| `FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_SPECIAL_USE` | Ensures reliable, continuous monitoring without unexpected system termination. |
| `SYSTEM_ALERT_WINDOW` | *(Optional but recommended)* Allows warning UI to launch directly over monitored apps. |

---

## License

This project is licensed under the Apache License, Version 2.0.
