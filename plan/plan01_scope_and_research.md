# Plan 01 — Scope, feasibility, and online research

## Objective
Freeze an implementable MVP and verify platform restrictions before creating application code.

## Actions
1. Read this file and `README.md`.
2. Review the official Android and Google Play sources listed below. Record access date, key finding, and any uncertainty in `dev/RESEARCH_LOG.md`.
3. Define MVP as: select installed apps; configure per-app daily usage threshold; configure warning interval and maximum warnings; show notifications; show an in-app interruption screen when escalation threshold is reached; show daily usage summary.
4. Explicitly exclude from MVP: forced termination of another app, covert monitoring, remote analytics, account/login, cloud sync, ad SDKs, and website-domain filtering.
5. Document website-domain tracking as a later, separately approved feature. `UsageStatsManager` cannot provide the current URL inside another app.
6. Define the target API range after checking the currently stable Android SDK and build-tool compatibility. Prefer a currently supported Android baseline; do not pick versions from memory alone.
7. Create `dev/PROJECT_STATUS.md`, `dev/DECISIONS.md`, and `dev/RESEARCH_LOG.md` using the templates in `dev/`.

## Required research sources
- UsageStatsManager: https://developer.android.com/reference/android/app/usage/UsageStatsManager
- Runtime permission best practices: https://developer.android.com/training/permissions/usage-notes
- Background work overview: https://developer.android.com/develop/background-work/background-tasks
- Foreground services: https://developer.android.com/develop/background-work/services/fgs
- Background activity launch restrictions: https://developer.android.com/guide/components/activities/secure-bal
- Notifications: https://developer.android.com/develop/ui/views/notifications
- AccessibilityService policy: https://support.google.com/googleplay/android-developer/answer/10964491
- Google Play sensitive APIs / permissions policy: https://support.google.com/googleplay/android-developer/answer/16558241
- VpnService API: https://developer.android.com/reference/android/net/VpnService
- Material 3: https://m3.material.io/

## Acceptance criteria
- `dev/RESEARCH_LOG.md` records findings and links, not just a list of URLs.
- Scope explicitly states that force-stopping arbitrary third-party apps is unsupported for a normal app.
- A decision is recorded on whether domain blocking is excluded from MVP (default: excluded).
- No source code is written until the scope and key platform constraints are documented.
