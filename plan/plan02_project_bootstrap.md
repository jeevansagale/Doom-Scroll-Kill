# Plan 02 — Android project bootstrap

## Objective
Create the smallest buildable Android project with the chosen Kotlin and Compose setup.

## Actions
1. Create/open the Android project using Android Studio and the stable compatible Android Gradle Plugin, Kotlin plugin, Compose BOM, and SDK versions confirmed in Plan 01.
2. Use one application module unless a concrete requirement justifies another module.
3. Configure package/application ID, `minSdk`, `targetSdk`, Java/Kotlin toolchain, Compose, and Material 3. Record exact versions in `dev/TECH_STACK.md`.
4. Add only dependencies needed for the selected architecture and first vertical slice. Do not add Hilt, networking, analytics, or testing libraries without a use case.
5. Create the package structure:
   - `ui/` — screens and reusable UI elements
   - `data/` — persistence and app-usage data access
   - `domain/` — rule evaluation and simple domain models
   - `platform/` — Android permission and notification adapters
6. Add a minimal Compose `MainActivity` with app theme and navigation placeholder.
7. Build and launch on emulator or device. Save exact build/run commands and known environment issues in `dev/PROJECT_STATUS.md`.

## Design constraints
- Material 3 components and system insets.
- Use a small spacing/type/color token set; do not create a large design system.
- Support dark and light themes using semantic color roles.
- No decorative animation on the dashboard unless it communicates state.

## Acceptance criteria
- Clean Gradle sync and debug build.
- App launches without runtime crash.
- No unused dependencies or placeholder classes that are not required by the next plan.
- Project versions and package name documented in `dev/TECH_STACK.md`.
