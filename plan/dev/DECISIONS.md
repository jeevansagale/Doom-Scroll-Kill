# Architecture and Product Decisions

| ID | Decision | Rationale | Status |
|---|---|---|---|
| D-001 | MVP does not claim to force-stop other apps | Standard Android apps cannot reliably force-stop arbitrary packages. | Accepted |
| D-002 | MVP uses user-granted Usage Access | Narrowest relevant API for app usage history. | Accepted |
| D-003 | Domain blocking is deferred | URLs are not exposed by app-usage statistics; VPN/browser scope needs separate review. Excluded from MVP. | Accepted |
| D-004 | Usage data stays on-device in MVP | Data minimization and reduced backend complexity. | Accepted |
| D-005 | Avoid tight polling and unjustified foreground services | Battery efficiency and Android background restrictions. | Accepted |
| D-006 | Exclude covert monitoring, remote analytics, account/login, cloud sync, ad SDKs | Keeping the app privacy-focused and minimalist. | Accepted |
| D-007 | Target API range is 28 to latest stable (35+) | Ensures compatibility with modern Android restrictions (App Standby, BAL restrictions) and uses latest features safely. | Accepted |
| D-008 | Define MVP scope clearly | Select installed apps; configure per-app daily usage threshold; configure warning interval and max warnings; show notifications; show in-app interruption screen on escalation; show daily summary. | Accepted |

Add a row when a decision changes. Never silently reverse a decision; include date and migration impact.
