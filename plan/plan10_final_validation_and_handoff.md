# Plan 10 — Final validation and agent handoff

## Objective
Deliver a reproducible MVP and a compact `dev/` knowledge base so a new developer or agent can continue without reading the entire repository.

## Final validation
1. Run the project’s documented clean build and all unit/instrumentation tests.
2. Install the release candidate on a physical device.
3. Verify app selection, per-app settings, usage aggregation, warning timing, escalation state, persistence, permission recovery, and daily reset.
4. Verify that the UI never claims to force-close another app.
5. Repeat the battery/performance test and compare against Plan 06 baseline.
6. Review manifest permissions and Gradle dependencies. Remove only demonstrably unused code/dependencies introduced by this project; do not perform unrelated refactors.
7. Capture screenshots of onboarding, dashboard, app selection, rule editor, notification, and interruption UI.
8. Record known limitations, supported API range, build variants, and test-device details.

## Required handoff files
Update all relevant files under `dev/`:
- `PROJECT_STATUS.md` — what works now, current branch/commit, exact next action.
- `ARCHITECTURE.md` — module/package boundaries and data flow.
- `TECH_STACK.md` — toolchain and dependency versions.
- `DECISIONS.md` — decision log and rationale.
- `RESEARCH_LOG.md` — official sources, findings, dates, unresolved questions.
- `DATA_MODEL.md` — entities, fields, retention and migrations.
- `PERMISSIONS.md` — each permission, purpose, request timing, fallback, and policy notes.
- `PERFORMANCE.md` — monitoring strategy, test conditions, measured results, known trade-offs.
- `TESTING.md` — commands, automated coverage, device matrix, known failures.
- `CHANGELOG.md` — concise changes per completed plan.

## Handoff format
`PROJECT_STATUS.md` must begin with:
- **Current state**
- **Last completed plan**
- **Build/test status**
- **Known blockers**
- **Next exact task**
- **Files changed in the last task**

Each plan is complete only when its acceptance criteria pass and the relevant `dev/` files are updated.

## Final deliverables
- Source project that builds from a clean checkout.
- Test evidence and physical-device validation notes.
- Updated `dev/` documentation.
- A final limitations statement, especially around app force-stop and website-domain visibility.
