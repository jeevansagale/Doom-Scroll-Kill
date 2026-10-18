# Testing Strategy & Architecture — Doomscroller

## 1. Overview
Doomscroller enforces digital wellbeing quotas and penalty cooldowns using a deterministic finite state machine, dual-condition restriction gates (5-minute cooldown + educational quiz completion), and strict intent validation. This document details the testing architecture, suites, edge cases, and continuous verification workflows.

---

## 2. Testing Framework & Architecture

- **Test Runner**: JUnit 4 (`testImplementation(libs.junit)`)
- **Execution Target**: Local JVM (`./gradlew testDebugUnitTest`)
- **Target Subsystems**:
  - `ReminderEscalationEvaluator`: Finite state machine governing usage threshold reminders, consecutive ignored increments, escalation triggers, and post-penalty recovery.
  - `NotificationRateLimiter`: Rolling 15-minute sliding window rate limiting and minimum interval throttling to prevent notification spam.
  - `RuleEvaluator`: Daily usage quota calculations and warning state progressions.
  - `SecurityAndEdgeCaseTest`: Intent injection sanitization, dual-condition penalty release, self-lockout defense, parameter boundary clamping, screen-off/background attribution, and question bank integrity.

---

## 3. Test Suites & Coverage Matrix

| Test Suite | File | Tests | Status | Key Focus Areas |
| :--- | :--- | :---: | :---: | :--- |
| `ReminderEscalationEvaluatorTest` | `app/src/test/.../ReminderEscalationEvaluatorTest.kt` | 9 | 100% Pass | Reminder interval timing, consecutive ignored escalation, educational quiz question verification, recovery after penalty. |
| `NotificationRateLimiterTest` | `app/src/test/.../NotificationRateLimiterTest.kt` | 12 | 100% Pass | Sliding 15-minute window rate limit cap (default 3), minimum spacing interval (default 5 min), per-package history isolation. |
| `RuleEvaluatorTest` | `app/src/test/.../RuleEvaluatorTest.kt` | 7 | 100% Pass | Quota threshold detection, warning escalation states, penalty cooldown timestamp calculations. |
| `EnforcementTest` | `app/src/test/.../EnforcementTest.kt` | 11 | 100% Pass | End-to-end quota escalation flow, repeated warning suppression, expiration reactivation. |
| `SecurityAndEdgeCaseTest` | `app/src/test/.../SecurityAndEdgeCaseTest.kt` | 12 | 100% Pass | Dual-condition restriction release, intent sanitization, self-lockout prevention, boundary clamping, background transitions, clock skew. |
| `ExampleUnitTest` | `app/src/test/.../ExampleUnitTest.kt` | 1 | 100% Pass | Sanity check. |
| **Total** | | **52** | **100% Pass** | **Zero Failures** |

---

## 4. Key Edge Cases Verified

1. **Dual-Condition Restriction Release**:
   - Condition A: Cooldown timer $\ge 5$ minutes has elapsed.
   - Condition B: User has answered 2 educational questions correctly.
   - *Result*: Evaluated independently. If timer expires but questions are incomplete, restriction stays active. If questions are solved before timer expires, restriction stays active until cooldown elapses.
2. **Security & Intent Injection**:
   - `MainActivity.kt` filters all incoming package strings against `^[a-zA-Z0-9_.]+$`.
   - Incoming target package must be an actively monitored and restricted rule in `PreferencesManager`. Arbitrary injection attempts are rejected.
3. **Self-Lockout Prevention**:
   - `com.example.doomscroller` is prevented from targeting or restricting itself, ensuring the user is never locked out of their wellbeing settings.
4. **Boundary Clamping**:
   - Reminder intervals clamped to `1..60` minutes.
   - Ignored reminder limit clamped to `1..10`.
   - Rate limit cap clamped to `1..6` notifications per 15 minutes.
5. **Background Attribution**:
   - Usage when an app is in the background (`isForeground == false`) does not advance or reset qualifying usage baselines.
6. **Clock Skew / Backward Time Jump**:
   - Negative time deltas resulting from manual clock adjustments or NTP sync are handled gracefully without false triggers or exceptions.
7. **Question Bank Quality**:
   - Every question in `QuestionBank` contains non-blank ID, text, explanation, and exactly 4 distinct options with the correct answer present.

---

## 5. Execution Commands

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
