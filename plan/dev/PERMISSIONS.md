# Permissions Register

| Permission / access | Purpose | Request timing | Fallback |
|---|---|---|---|
| Usage Access via system Settings | Read app usage statistics | Onboarding, when user enables monitoring | Pause monitoring and show setup state |
| `POST_NOTIFICATIONS` where applicable | Show usage warnings | When alerts are enabled, after disclosure | Dashboard explains alerts are disabled |
| Package visibility `<queries>` | Find launchable apps | Manifest declaration, no runtime prompt | Show only discoverable apps |

## Not in MVP
- `QUERY_ALL_PACKAGES`: avoid unless narrow queries cannot meet the core feature and current Play policy permits it.
- `SYSTEM_ALERT_WINDOW`: not required for basic notifications and in-app interruption UI.
- `AccessibilityService`: not assumed; requires a separate necessity and policy review.
- `VpnService`: not used unless domain filtering is separately approved.
- Exact alarms / battery optimization exemption: do not request by default.

For every added permission, record the exact manifest entry, platform version requirement, user-facing rationale, denial behavior, and Play policy status.
