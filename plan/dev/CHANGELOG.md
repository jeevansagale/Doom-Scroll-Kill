# Changelog

## Unreleased
- Fixed usage tracking: Switched to `queryEvents` in `UsageStatsManager` for precise real-time daily measurement instead of `queryAndAggregateUsageStats`, which can be significantly delayed and ignores active foreground sessions.
- Redesigned Dashboard: Updated `DashboardScreen` to use Material 3 cards, explicit summary statistics, progress bars with threshold colors, and clear empty/missing-permissions states.
