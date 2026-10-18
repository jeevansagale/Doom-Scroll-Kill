# Plan 04 — Permission flow and installed-app catalog

## Objective
Build a transparent, user-controlled app-selection flow and handle Android package visibility correctly.

## Actions
1. Use `PackageManager` to enumerate only apps the user can reasonably select. Prefer launchable activities over enumerating every installed package.
2. Verify current package-visibility rules for the chosen target SDK. Declare the narrowest `<queries>` configuration that supports the feature; do not request `QUERY_ALL_PACKAGES` unless justified, policy-eligible, and approved by research.
3. Show each app's label and icon, deduplicate by package name, and sort alphabetically. Add local search; avoid querying PackageManager on every keystroke.
4. Persist selected package names and rules locally.
5. Add an onboarding explanation before sending users to Usage Access settings. Open `Settings.ACTION_USAGE_ACCESS_SETTINGS` and re-check access when the app resumes.
6. On Android versions requiring notification runtime permission, request it at the moment the user enables alerts. If denied, explain that alerts will not appear and provide a settings path.
7. Do not request overlay, Accessibility, VPN, exact-alarm, or battery-optimization exemption permissions in the MVP unless an approved implementation decision proves they are necessary.
8. Re-check permissions at every relevant resume/start boundary; never assume permission remains granted.

## Failure handling
- No launchable apps returned: show a recoverable empty state.
- Permission denied or revoked: pause monitoring, preserve rules, show a clear status and setup action.
- App uninstalled: remove or mark the rule as unavailable during catalog reconciliation.
- App label/icon lookup failure: show a safe fallback rather than crashing.

## Acceptance criteria
- Search and selection remain responsive with a large app list.
- Selected packages survive process death and device restart.
- Permission revocation does not crash the app or silently claim monitoring is active.
- Manifest declarations are minimal and each is documented in `dev/PERMISSIONS.md`.
