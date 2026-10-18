# Plan 07 — Notifications and escalation state machine

## Objective
Send notifications once per valid rule decision and apply deterministic escalation without spamming users.

## Actions
1. Create a notification channel for usage warnings with a user-facing name and description.
2. Request notification permission on supported Android versions only after explaining the feature.
3. Use stable notification IDs per package/rule so updates replace previous alerts instead of flooding the shade.
4. Add a `PendingIntent` that opens the relevant app-rule or interruption screen. Use immutable/update flags appropriate to the target SDK.
5. Use notification copy that names the selected app and the configured limit. Avoid shaming or misleading urgency.
6. Feed `RuleEvaluator` decisions into a single `NotificationCoordinator`; UI and repository layers must not independently post alerts.
7. Persist warning state before or atomically with dispatch so process restarts do not repeat the same warning.
8. Define dismissal behavior: dismissing a notification is not the same as acknowledging the warning. Record acknowledgement only when the user takes the explicit action designed for it.
9. Handle notification permission denial, channel disabled, Do Not Disturb, and notification delivery limits without crashing. Explain that the OS/user can suppress alerts.
10. Do not launch an Activity directly from the background as an escalation shortcut. Android restricts background activity launches. Use a notification tap or a specifically reviewed, user-consented mechanism.

## State sequence
`WithinLimit -> LimitReached -> WarningSent -> WaitingForInterval -> WarningSent ... -> EscalationReached`.
Persist state per package and local date. Reset only on defined day rollover or an explicit user reset.

## Acceptance criteria
- No duplicate notification for the same state transition.
- Notification opens the correct screen.
- Denied/disabled notifications are visible as a dashboard status.
- Tests cover repeated evaluation, app process restart, dismiss/tap, and day rollover.
