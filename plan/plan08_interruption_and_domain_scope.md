# Plan 08 — Interruption UI and website-domain decision

## Objective
Implement a realistic escalation action and decide whether website support is in scope for a later release.

## MVP interruption behavior
1. When the rule engine reaches escalation, show an in-app interruption screen through a user-visible entry point, such as tapping the notification.
2. Offer a clear “Leave app” action that opens the home screen using the supported Android intent behavior.
3. Do not represent the target app as forcibly closed or killed. The app may still exist in the recent-apps list or resume later.
4. Do not launch activities from the background without a valid Android-supported exemption and policy review.
5. If the client requires an interruption that appears while another app is in foreground, pause implementation and run a design/technical review. Overlay permission and AccessibilityService introduce consent, policy, and reliability trade-offs; neither should be added silently.

## Website-domain feature — separate decision
1. Do not infer URLs from browser app usage.
2. If domain blocking is required, compare:
   - Browser extension/integration (limited to supported browsers).
   - `VpnService` local filtering (broad but higher complexity, network/privacy disclosures, VPN coexistence and battery considerations).
3. Research current Google Play VPN policy, VPN consent UX, DNS/HTTPS limitations, IPv4/IPv6 handling, private DNS, app-specific exclusions, and OEM behavior.
4. A DNS-only filter cannot reliably identify every full URL or path; HTTPS encrypts request details. Specify precisely whether the feature blocks domains, subdomains, or full URLs.
5. Do not transmit browsing history to a remote server. For an on-device implementation, minimize logs and avoid retaining domains unless required by an explicit feature.
6. Build domain support only after written client approval, a threat/privacy review, and a test matrix. Keep it behind a separate feature boundary.

## Acceptance criteria
- Escalation wording matches actual behavior.
- User can exit the interruption screen and return to normal app use.
- Domain support is either explicitly deferred or separately scoped with privacy and compatibility requirements.
