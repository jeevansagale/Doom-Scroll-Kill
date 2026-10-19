# Testing Strategy & Architecture — Doomscroller

## 1. Overview
Doomscroller enforces digital wellbeing quotas and penalty cooldowns using a deterministic finite state machine, dual-condition restriction gates (5-minute cooldown + educational quiz completion), strict intent validation, and event-based Android usage tracking aligned with Android Digital Wellbeing. This document details the testing architecture, suites, edge cases, and continuous verification workflows.

---

## 2. Testing Framework & Architecture

- **Test Runner**: JUnit 4 (`testImplementation(libs.junit)`)
- **Execution Target**: Local JVM (`./gradlew testDebugUnitTest`)
- **Target Subsystems**:
  - `UsageCalculator` & `UsageRepository`: Event-based foreground tracking from local midnight, exclusion of Doomscroll own package, screen-off/lock pauses, timezone/DST boundaries, and unavailable state handling.
  - `MotivationalQuoteEngine` & `QuestionBank`: Rotation across application sessions without immediate repeats, content validation (50+ quotes, 50+ questions, zero empty fields, unique IDs).
  - `ReminderEscalationEvaluator`: Finite state machine governing usage threshold reminders, consecutive ignored increments, escalation triggers, and post-penalty recovery.
  - `NotificationRateLimiter`: Rolling 15-minute sliding window rate limiting and minimum interval throttling to prevent notification spam.
  - `RuleEvaluator`: Daily usage quota calculations and warning state progressions.
  - `SecurityAndEdgeCaseTest`: Intent injection sanitization, dual-condition penalty release, self-lockout defense, parameter boundary clamping, screen-off/background attribution.

---

## 3. Test Suites & Coverage Matrix

| Test Suite | File | Tests | Status | Key Focus Areas |
| :--- | :--- | :---: | :---: | :--- |
| `UsageTrackingAndMidnightTest` | `app/src/test/.../UsageTrackingAndMidnightTest.kt` | 9 | 100% Pass | Self-package exclusion, midnight boundary clipping, yesterday leak prevention, screen-off/lock pauses, DST shifts, permission denial state. |
| `QuoteRotationAndContentTest` | `app/src/test/.../QuoteRotationAndContentTest.kt` | 7 | 100% Pass | 50+ unique quotes and questions, zero empty fields, unique IDs, safe single-item handling, session rotation without immediate repeat, recomposition stability. |
| `ReminderEscalationEvaluatorTest` | `app/src/test/.../ReminderEscalationEvaluatorTest.kt` | 9 | 100% Pass | Reminder interval timing, consecutive ignored escalation, educational quiz question verification, recovery after penalty. |
| `NotificationRateLimiterTest` | `app/src/test/.../NotificationRateLimiterTest.kt` | 12 | 100% Pass | Sliding 15-minute window rate limit cap (default 3), minimum spacing interval (default 5 min), per-package history isolation. |
| `RuleEvaluatorTest` | `app/src/test/.../RuleEvaluatorTest.kt` | 7 | 100% Pass | Quota threshold detection, warning escalation states, penalty cooldown timestamp calculations. |
| `EnforcementTest` | `app/src/test/.../EnforcementTest.kt` | 11 | 100% Pass | End-to-end quota escalation flow, repeated warning suppression, expiration reactivation. |
| `SecurityAndEdgeCaseTest` | `app/src/test/.../SecurityAndEdgeCaseTest.kt` | 12 | 100% Pass | Dual-condition restriction release, intent sanitization, self-lockout prevention, boundary clamping, background transitions, clock skew. |
| `ExampleUnitTest` | `app/src/test/.../ExampleUnitTest.kt` | 1 | 100% Pass | Sanity check. |
| **Total** | | **68** | **100% Pass** | **Zero Failures** |

---

## 4. Key Edge Cases Verified

1. **Exclusion of Doomscroll's Own Package**:
   - Time spent inside `com.example.doomscroller` is tracked for internal lifecycle but strictly excluded from external app usage totals and the hero screen time display.
2. **Midnight Boundary & Zero Leakage**:
   - `UsageCalculator.computeUsageFromEvents` clamps sessions to `maxOf(sessionStart, startOfDayMillis)`. Sessions crossing midnight only count the minutes that transpired after 00:00 local time.
3. **Screen-Off & Lock State Transitions**:
   - Device sleep (`SCREEN_NON_INTERACTIVE`) and keyguard lock (`KEYGUARD_SHOWN`) immediately close active foreground sessions. Pocket / screen-off time is never attributed as screen time.
4. **Timezone Changes & Daylight Saving Time (DST)**:
   - Evaluated across multiple world timezones (UTC, America/New_York, Asia/Tokyo, Europe/London).
   - DST shifts (23-hour spring forward and 25-hour fall back days) calculate the exact local start of day without assuming a rigid 24-hour day length.
5. **Clear Unavailable / Denied State**:
   - When Usage Access permission is missing, `UsageRepository` returns `UsageDataState.PermissionRequired`, displaying `--` and an action banner on the dashboard rather than misleading the user with a verified zero.
6. **Quote Rotation Across Sessions**:
   - `MotivationalQuoteEngine.getNextSessionQuote(lastId)` filters out the previously displayed quote ID. Recompositions and screen rotations do not alter the session quote. Single-item datasets are handled gracefully without recursion.
7. **Content Quality & Richness**:
   - 50 quotes across 6 categories with documented attribution sources and URLs.
   - 53 questions (48 reflection across 8 categories + legacy technical questions). All questions verified to have non-blank text, exactly 4 distinct options, valid correct answers, and non-blank explanations.

---

## 5. Digital Wellbeing Comparison Procedure

To validate usage accuracy against Android OS system metrics:
1. Grant Usage Access via **Settings > Apps > Special App Access > Usage Access > Doomscroller**.
2. Open target external applications (e.g. YouTube, Instagram, Chrome) for a known duration (e.g. 10 minutes).
3. Navigate to system **Settings > Digital Wellbeing & Parental Controls**.
4. Check the total device screen time for today and individual app breakdowns.
5. Open Doomscroller. Verify that:
   - "DEVICE SCREEN TIME (TODAY)" closely aligns with Android Digital Wellbeing's total.
   - Doomscroller's own screen time is omitted from the external total.
   - Per-app cards reflect individual app foreground durations accurately.

---

## 6. Execution Commands

### Run All Local Unit & Edge Case Tests
```bash
./gradlew testDebugUnitTest
```

### Build Debug APK
```bash
./gradlew assembleDebug
```
Artifact: `app/build/outputs/apk/debug/app-debug.apk`

### Build Release APK
```bash
./gradlew assembleRelease
```
Artifact: `app/build/outputs/apk/release/app-release.apk`
