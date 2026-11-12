# Doom-Scroll-Kill

<p align="center">
  <img src="app_logo.png" alt="DoomScroller app logo" width="180" />
</p>

<p align="center">
  <strong>An Android digital-wellbeing app that helps interrupt intentional app-use limits with reminders, escalation, and cooldowns.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat-square" alt="Jetpack Compose" />
</p>

---

## Overview

Doom-Scroll-Kill is an Android application for setting usage rules on selected apps. It reads Android usage events, tracks qualifying foreground usage, and responds to configured limits with reminders and escalating interventions.

The project explores the practical constraints of building a digital-wellbeing tool on Android: usage-access permissions, background execution, notification delivery, app lifecycle changes, and persistent enforcement state.

## Features

- **Per-app rules:** Select apps to monitor and configure their usage limits.
- **Usage tracking:** Uses `UsageStatsManager` and usage events, with a daily-statistics fallback when event data is unavailable.
- **Reminder policy:** Configurable reminder interval, rolling notification cap, and ignored-reminder threshold.
- **Escalation flow:** Tracks reminder and intervention states, with configurable cooldown duration.
- **Learning gate:** Includes a short question-based step as part of the intervention flow.
- **Usage history and dashboard:** Displays tracked usage and intervention state.
- **Content discovery:** Offers curated learning content, with live YouTube search when an API key is configured.
- **Local preferences:** Rules and session state are persisted using Android SharedPreferences.

## How It Works

```text
Selected Applications
        |
        v
Android Usage Events
        |
        v
Usage Calculation
        |
        v
Per-App Rule Evaluation
        |
        v
Reminder / Intervention
        |
        v
Learning Gate + Cooldown
        |
        v
Session State Persistence
```

## Platform Notes

Android controls how third-party apps can observe usage and present interruptions. This project relies on user-granted Usage Access and notification/overlay capabilities where applicable. It does not silently terminate other applications.

Actual interruption behavior depends on Android version, device manufacturer, permission state, and background-execution restrictions. Grant only the permissions required by the features you choose to use.

## Tech Stack

| Component | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| State and concurrency | Android lifecycle components, Kotlin coroutines |
| Usage data | Android `UsageStatsManager`, `UsageEvents` |
| Notifications | Android `NotificationManager` |
| Persistence | SharedPreferences |
| Build | Gradle, Android Gradle Plugin |

## Requirements

- Android Studio with a compatible Android SDK.
- JDK compatible with the Gradle 9.5 wrapper.
- Android device or emulator. Usage monitoring and interruption behavior should be validated on a physical device.
- Usage Access permission for app-usage tracking.

The Android module currently declares `minSdk 28`, `compileSdk 37`, and `targetSdk 37`.

## Build

```bash
git clone https://github.com/jeevansagale/Doom-Scroll-Kill.git
cd Doom-Scroll-Kill

# Run local unit tests
./gradlew testDebugUnitTest

# Build a debug APK
./gradlew assembleDebug
```

The debug APK is generated under `app/build/outputs/apk/debug/`.

## Tests

The repository includes unit tests for rule evaluation, reminder escalation, enforcement state, notification rate limiting, usage tracking, discovery caching, and related edge cases. Instrumentation tests are also present.

Run the local unit-test suite with:

```bash
./gradlew testDebugUnitTest
```

## Repository Structure

```text
app/src/main/java/com/example/doomscroller/
├── data/          # Preferences, usage data, and repositories
├── domain/        # Rule evaluation and enforcement logic
├── feature/       # Content discovery
├── platform/      # Android services and permissions
├── ui/            # Screens, navigation, and app state
└── MainActivity.kt
```

## Configuration

The YouTube content-discovery feature can use a YouTube Data API key. Without a key, the app falls back to curated content. Do not commit personal API keys or secrets to the repository.

## Current Limitations

- Usage data depends on Android's reporting APIs and granted permissions.
- Background execution and interruption presentation vary across Android versions and device vendors.
- Instagram discovery uses curated topic links and creator suggestions; it is not live keyword search through an official public consumer API.
- YouTube live search requires a configured API key and is subject to API quota limits.

## License

No license file is currently included. Unless a license is added, the repository should be treated as source available for viewing, not as granting general permission to reuse or redistribute the code.
## Release Signing

Release builds are not signed with the Android debug key.
To produce a signed release APK, provide these environment variables to Gradle:

```bash
export ANDROID_KEYSTORE_PATH="/absolute/path/to/release-keystore.jks"
export ANDROID_KEYSTORE_PASSWORD="<keystore-password>"
export ANDROID_KEY_ALIAS="<key-alias>"
export ANDROID_KEY_PASSWORD="<key-password>"
./gradlew assembleRelease
```

If these variables are not all present, Gradle produces an unsigned release artifact. Keep the keystore and passwords outside the repository and store them in your CI secret manager when building releases.
