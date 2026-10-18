# Online Research Log

Research must be re-checked when implementation begins because Android APIs, target SDK rules, and Google Play policy can change.

## Initial findings
- `UsageStatsManager` exposes aggregated usage history. It is not a browser URL API and should not be treated as guaranteed real-time foreground state.
- Android recommends selecting background-work mechanisms based on whether work is deferrable and its user-visible requirements. Excessive background work can drain battery.
- Background activity launches are restricted. Escalation should not assume it can automatically open an Activity over another app.
- AccessibilityService use is policy-sensitive. Do not declare the app an accessibility tool unless its primary purpose genuinely meets the policy definition. If considered later, obtain explicit informed consent and complete the relevant declarations.
- `VpnService` is a possible technical foundation for some domain-filtering designs, but it creates substantial consent, privacy, compatibility, and battery requirements.

## Record for each follow-up research session

- **Date checked:** 2026-10-18
- **Question:** What is the scope of `UsageStatsManager`?
- **Source and relevant section:** UsageStatsManager API (https://developer.android.com/reference/android/app/usage/UsageStatsManager)
- **Finding:** Gives access to device usage history and statistics. Requires `PACKAGE_USAGE_STATS` permission. Cannot track URLs.
- **Implementation impact:** Can be used to track app time limits. Website blocking is infeasible with this API alone.
- **Confidence / unresolved issue:** High.

- **Date checked:** 2026-10-18
- **Question:** How restricted are Background Activity Launches (BAL)?
- **Source and relevant section:** Background activity launch restrictions (https://developer.android.com/guide/components/activities/secure-bal)
- **Finding:** Apps running in the background cannot start activities directly starting from Android 10 (API 29). Android 14+ introduces stricter background activity launch rules.
- **Implementation impact:** When time limit is exceeded, we cannot just forcefully launch our lock screen activity while the user is using another app, unless we have the SYSTEM_ALERT_WINDOW (display over other apps) permission, or send a high-priority notification. We should rely on full-screen intent notifications or SYSTEM_ALERT_WINDOW for the interruption screen.
- **Confidence / unresolved issue:** High.

- **Date checked:** 2026-10-18
- **Question:** Does Google Play allow `AccessibilityService` for app blocking?
- **Source and relevant section:** AccessibilityService Google Play policy (https://support.google.com/googleplay/android-developer/answer/10964491)
- **Finding:** Only apps that are designed to help users with disabilities should use the AccessibilityService API. There are exceptions for some categories, but general use is heavily scrutinized.
- **Implementation impact:** We will explicitly avoid `AccessibilityService` to comply with Google Play policies and ensure smooth publishing. We will rely on `UsageStatsManager` and standard overlays (`SYSTEM_ALERT_WINDOW`).
- **Confidence / unresolved issue:** High.

- **Date checked:** 2026-10-18
- **Question:** What is the policy around `VpnService` for domain tracking?
- **Source and relevant section:** VpnService API / Play policy
- **Finding:** Requires strict disclosure and is complex for a simple MVP. It causes battery drain and conflicts with other VPN apps.
- **Implementation impact:** Domain blocking is explicitly excluded from the MVP.
- **Confidence / unresolved issue:** High.
