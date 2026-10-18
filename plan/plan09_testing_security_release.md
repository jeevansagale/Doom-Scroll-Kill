# Plan 09 — Testing, privacy, compatibility, and release readiness

## Objective
Prove that the MVP works across lifecycle events and permissions, without excessive battery use or misleading claims.

## Automated tests
- Rule engine: threshold boundaries, warning intervals, maximum warnings, disabled rule, date rollover, invalid values.
- Persistence: save/load rules, process recreation, migration, deletion of an app rule.
- UI: onboarding, search/select apps, edit rule, denied permission state, empty state, notification destination.
- Notification coordinator: stable IDs, duplicate suppression, permission/channel disabled.
- Usage adapter: mocked usage events, missing access, empty statistics, reordered/duplicate events, time changes.
- Static checks: lint, formatting if configured, manifest permission review, dependency review.

## Device test matrix
Test at minimum:
- One current Pixel/AOSP-like device or emulator and one physical OEM device.
- Supported minimum Android API and current target Android version.
- Permission grant, denial, later revocation, notification channel disabled.
- Screen off/on, reboot, force-stop of this app, process death, app update, local midnight, time-zone change.
- Long foreground session, quick app switching, target app uninstalled, selected app disabled.
- Battery saver and vendor background restrictions.
- Large font, TalkBack, light/dark themes.

## Privacy/security
- Keep usage data on-device for MVP.
- Do not add internet permission unless a justified feature needs it.
- Do not collect accessibility events, URLs, or unrelated package data.
- Explain data use in onboarding and a privacy page.
- Do not log detailed usage history in production logs.
- Ensure no secrets or user-specific usage records are committed to Git.
- If AccessibilityService or VpnService is later considered, redo disclosure, consent, policy declaration, and privacy review before implementation.

## Performance gates
- Record baseline cold-start time, dashboard rendering, memory, CPU, and idle battery.
- Compare monitoring enabled versus disabled over the same device/time conditions.
- Investigate wakeups, repeated database writes, excess recomposition, and unbounded event retention.
- Set numerical performance budgets only after measuring a baseline and agreeing targets with the client.

## Release criteria
- Debug and release builds succeed.
- No known crash in core flows.
- Permissions are declared and justified.
- App listing and in-app claims accurately describe limits.
- `dev/PROJECT_STATUS.md` includes build steps, test results, unresolved issues, and current branch/commit.
