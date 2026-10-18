# Plan 05 — Local data model and deterministic rule engine

## Objective
Implement durable settings and a testable pure-Kotlin rule evaluator before wiring platform monitoring.

## Data model
- `AppRule`: `packageName`, `enabled`, `dailyLimitMinutes`, `warningIntervalMinutes`, `maxWarnings`, `createdAt`, `updatedAt`.
- `UsageDay`: local calendar date and per-package accumulated duration, only if needed for reliable local aggregation.
- `WarningState`: package name, local date, warning count, last warning timestamp, and current threshold state.
- Settings: onboarding completed and notification preference. Store simple preferences in DataStore.
- Use Room only for relational/history records that need queries. Avoid storing app icons or redundant display labels unless measurement shows a need.

## Rule semantics
1. Define a day using the device's local time zone. Handle date/time-zone changes explicitly.
2. Threshold is reached when accumulated foreground time for a package is greater than or equal to its configured daily limit.
3. Warning interval is measured from the last warning, not from every polling tick.
4. `maxWarnings` means the maximum number of warnings shown after the limit is crossed; document whether the initial warning counts. Default decision: initial warning counts as warning 1.
5. Emit a warning only once per eligible interval. Do not emit repeated notifications on every usage event.
6. Once escalation is triggered for a package/day, do not trigger it repeatedly unless the user resets the rule or the next day begins.
7. Disabled rules never emit warnings.
8. Rule evaluation must be pure: input is current usage + rule + persisted warning state; output is a decision and updated state.

## Actions
1. Add data classes and persistence interfaces.
2. Implement DataStore and Room only for the fields above.
3. Implement `RuleEvaluator` with no Android dependencies.
4. Add unit tests for below threshold, exact threshold, repeated warning interval, max warning escalation, disabled rules, day rollover, and malformed settings.
5. Add schema migration tests when a schema change occurs.

## Acceptance criteria
- Rule engine unit tests pass without emulator.
- Re-evaluating the same state does not produce duplicate warnings.
- All setting bounds are validated at the domain boundary.
- Data model and rules are summarized in `dev/DATA_MODEL.md`.
