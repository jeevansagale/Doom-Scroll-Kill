# Data Model

## SharedPreferences (or equivalent simple Key-Value store)
* **doomscroller_rules**: Key: package name (String). Value: "limitMinutes,isEnabled" (String)

## Core Domain Classes
* **AppRule**: (packageName: String, limitMinutes: Int, isEnabled: Boolean)
* **WarningState**: (packageName: String, date: LocalDate, warningCount: Int, lastWarningTimeMillis: Long)
* **EvaluationResult**: (NoAction | ShowWarning(newWarningState) | Escalate(newWarningState))

## Rule Semantics
1. Day starts relative to the device's local timezone (`LocalDate`).
2. Threshold is exceeded when usedMinutes >= rule.limitMinutes.
3. Disabled rules never trigger an action.
4. Escalate is triggered after 3 warnings for the MVP.
5. `RuleEvaluator` is a pure Kotlin class evaluated against rule inputs and previous state.

Room database is avoided currently since persistent history logs are not required for MVP tracking.
