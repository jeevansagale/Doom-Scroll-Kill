# Doomscroller Developer & Agent Guidelines

## Architecture & Testing

- **Testing Strategy**: Comprehensive unit, regression, and edge-case testing documentation is maintained in [docs/testing.md](docs/testing.md).
- **Core Principles**:
  - Bauhaus Visual Design: Strict adherence to `#F9F6F0` warm cream canvas, `#1A1A1A` high-contrast typography, primary color blocks (`#D32F2F` red, `#1976D2` blue, `#FBC02D` yellow), sharp 0.dp borders, and $\ge 48\text{ dp}$ accessible touch targets.
  - Dual-Condition Enforcement: Monitored apps under restriction require both a 5-minute cooldown duration AND completion of 2 educational quiz questions.
  - Security & Privacy: No external network transmission; strict intent validation for exported components; user authorization verification on every foreground change.

## Commands

- Run Unit Tests: `./gradlew testDebugUnitTest`
- Build Debug APK: `./gradlew assembleDebug`
- Build Release APK: `./gradlew assembleRelease`
