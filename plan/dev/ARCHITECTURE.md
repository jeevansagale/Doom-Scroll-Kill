# Architecture

## Intended MVP boundaries
- `ui/`: Compose screens, navigation, screen state rendering.
- `domain/`: `AppRule`, warning state, pure `RuleEvaluator`.
- `data/`: Room/DataStore implementations and repositories.
- `platform/`: UsageStatsManager adapter, permission checks, notification adapter.

## Data flow
Usage data adapter -> usage aggregation -> rule evaluator + persisted warning state -> notification coordinator -> Android notification / user-visible screen.

## Rules
- UI does not query Android usage APIs directly.
- Domain logic has no Android imports.
- Platform adapters do not contain business rules.
- Do not introduce a DI framework or extra module until a concrete complexity justifies it.
- Do not assume exact real-time monitoring or force-stop capabilities.
