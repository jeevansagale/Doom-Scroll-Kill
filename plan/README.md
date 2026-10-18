# Less-Mobile World — Android Implementation Plans

This archive contains small, ordered implementation plans for a Kotlin Android app that helps users reduce time spent in selected apps and optionally selected website domains.

## How to use these plans
1. Start with `plan01_scope_and_research.md`.
2. Execute plans in numerical order.
3. Before changing code, read `dev/PROJECT_STATUS.md`, `dev/ARCHITECTURE.md`, and the plan being executed.
4. After each plan, update the relevant `dev/*.md` files and run that plan's acceptance checks.
5. Do not implement later plans early, add speculative abstractions, or make unrelated refactors.

## Scope and platform constraints
- The MVP tracks usage of user-selected installed apps, issues notifications, records warning escalation, and presents a user-visible interruption screen.
- An ordinary third-party Android app cannot reliably force-stop arbitrary other apps. The supported fallback is to show a notification/interruption UI and offer a user-initiated return to the launcher.
- Website-domain monitoring/blocking is a separate optional phase. App usage APIs do not expose URLs. Domain control needs a deliberate browser integration or a user-consented `VpnService` implementation and a separate privacy/policy review.
- AccessibilityService is not assumed in the MVP. Do not use it as a shortcut to bypass Android restrictions or Play policy.
- This is an implementation plan, not a claim that a working app or APK has already been built.

## Suggested baseline
- Kotlin, Jetpack Compose, Material 3, MVVM with a small repository boundary.
- Room only for durable structured records if required; DataStore for small preferences/settings.
- `UsageStatsManager` for aggregated usage and usage events, subject to user-granted Usage Access.
- `NotificationManager` for user-visible alerts.
- Use the minimum background execution mechanism that satisfies measured requirements. Avoid tight polling and always-on foreground services unless a tested requirement justifies one.

## Definition of done
The MVP builds, runs on a physical Android device, explains and requests permissions in context, monitors selected apps within documented Android limitations, persists rules across process restarts, delivers notifications, handles denied/revoked permissions, and passes the acceptance tests in the final plan.
