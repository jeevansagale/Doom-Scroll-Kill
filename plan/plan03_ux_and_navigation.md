# Plan 03 — UX flows and visual system

## Objective
Implement the primary screens with real navigation and accessible states, without implementing monitoring yet.

## Required screens
1. **Onboarding / explanation** — explain what is monitored, why Usage Access is needed, that data stays local for the MVP, and that the app cannot forcibly close other apps.
2. **Dashboard** — today's total tracked time, selected apps, per-app usage and limit progress, and monitoring/permission status.
3. **Choose apps** — searchable list of launchable installed apps with icon, label, selection state, and no duplicate package entries.
4. **App rule editor** — daily usage limit, repeat-warning interval, max warnings, and enabled/disabled state.
5. **Permission setup** — explain Usage Access and notification permission separately, with a button that opens the relevant Android Settings page or system permission prompt.
6. **History / settings** — simple daily totals, reset-day behavior, notification preference, and privacy information.
7. **Interruption screen** — calm, non-shaming copy; show the app and time used; actions such as “Leave app” and “Continue” only if the product policy intentionally permits an override.

## Visual specification
- Material 3, clear hierarchy, restrained palette, strong text contrast.
- Dashboard: one primary “today” summary, then compact per-app rows with progress indicators.
- Rule editor: numeric values displayed next to controls; use discrete steps and validation.
- Use text labels with icons; never communicate warning severity by color alone.
- Empty states must explain the next action. Permission-denied state must not look like a crash.
- Touch targets should follow Android accessibility guidance; support font scaling and TalkBack labels.

## Actions
1. Define routes and state ownership.
2. Implement screen composables and previews where useful.
3. Use fake repository data only until Plan 05; isolate it so it can be removed without refactoring UI.
4. Add navigation and back behavior.
5. Add UI tests for primary navigation and rule validation.

## Acceptance criteria
- Every required screen is reachable.
- App selection, rule editor, and permission states have loading/empty/error states where applicable.
- Layout works at small and large phone sizes and with large font settings.
- No monitoring logic is placed inside composables.
