# Performance and Battery

## Principles
- No busy loops or sub-second polling.
- No network activity in the monitoring path.
- Do not run a foreground service unless a verified product requirement requires continuous user-visible work.
- Use WorkManager for deferrable maintenance, not precise countdown timing.
- Cache the selected app catalog; refresh on meaningful lifecycle/settings events.
- Persist only state needed for correct recovery and user-facing history.

## Measurements to collect
- Device model, Android version, battery level, battery saver state.
- Monitoring disabled vs enabled, same duration and usage pattern.
- Idle battery percentage delta over a repeatable window.
- CPU time / wakeups where available.
- Memory footprint, dashboard cold start, database write frequency.
- Warning latency and usage-total difference from Android Settings.

## Results
Not measured yet. Never fill this section with estimates presented as measurements.
