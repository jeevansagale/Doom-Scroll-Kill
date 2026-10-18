# Plan 06 — Usage monitoring with battery constraints

## Objective
Collect selected-app usage with the least frequent, least expensive mechanism that still meets the agreed warning latency.

## Research gate
Before implementation, re-check `UsageStatsManager`, `UsageEvents`, background-work limits, foreground-service rules, and device-specific behavior. Record findings in `dev/RESEARCH_LOG.md`. Usage statistics can be aggregated and delayed; they are not guaranteed to be a precise real-time foreground-app callback.

## Implementation approach
1. Build a `UsageRepository` adapter around `UsageStatsManager`. Keep Android API calls outside the domain rule engine.
2. Check Usage Access before querying. If access is missing, return an explicit permission-required result.
3. Use usage events and/or interval summaries according to observed reliability on supported Android versions. Do not assume one event source behaves identically across OEMs.
4. Maintain a local baseline and compute per-package foreground duration without double-counting overlapping intervals. Test screen-off, device reboot, app process death, time change, and midnight rollover.
5. Define a measurable target warning latency with the client. Start with a conservative, low-frequency check and tune only after device testing; do not use a tight loop or a continuously running timer.
6. Prefer event-driven callbacks when a supported and policy-compliant API exists. Otherwise use the lowest-frequency mechanism that satisfies the latency target, and document the accuracy trade-off.
7. Use WorkManager for deferrable reconciliation and history cleanup, not as a precision real-time timer.
8. Do not add a foreground service solely to poll usage. If one becomes necessary, justify it, show a persistent notification, implement stop behavior, declare the correct service type and permissions for the target SDK, and test background-start restrictions.
9. Treat device reboot, process death, permission revocation, and vendor battery management as expected states. Reconstruct from persisted state; never depend on an in-memory coroutine loop surviving.

## Battery and performance requirements
- No busy-waiting, 100–1000 ms polling, or unbounded coroutine loops.
- No network requests in the monitoring path.
- Cache the selected package set; do not enumerate all installed apps repeatedly.
- Batch persistence writes where possible.
- Stop timers and collectors when not needed.
- Measure idle battery impact and CPU wakeups on a physical device over a repeatable test window; record device model, OS version, app state, and test duration.
- Do not claim “zero battery use”; provide measured results and caveats.

## Acceptance criteria
- Usage totals are consistent with Android Settings within a documented tolerance.
- No duplicate or negative durations across lifecycle boundaries.
- Permission loss and unavailable statistics result in a clear degraded state.
- Battery test results and chosen check strategy are documented in `dev/PERFORMANCE.md`.
