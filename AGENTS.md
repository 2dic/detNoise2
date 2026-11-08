# Agents Context — noiseDet2

This file exists so that any AI agent (Cursor, Claude, Grok, Devin, etc.) can quickly understand the project context.

## Mandatory reading order

1. **README.md** — Project vision and high-level overview
2. **docs/DEVELOPMENT_PLAN.md** — Detailed current state + step-by-step plan (source of truth)
3. This file

## Project in one sentence
Android app for measuring and mapping urban noise. Currently local-only storage. Future goal — crowdsourced public noise map + quiet route planning.

## Architecture

**Multi-module:**
```
:app
:core:common          ← NoiseMeter lives here
:core:ui
:core:data            ← Room (Entity, Dao, Database, Repository)
:feature:map
:feature:history
:feature:analysis
```

**Stack:** Jetpack Compose + Material 3 + Hilt + KSP + Room + Navigation Compose + Yandex MapKit + Coroutines/Flow

## Current priority

**Phase 4 completed** (Map markers + measurement history).  
**Next: Phase 5 — Analysis Feature** (statistics, category distribution, charts)

## Important constraints
- Storage is local for now
- Do not implement cloud/backend yet
- New UI should prefer Jetpack Compose
- Put new feature code into the corresponding `feature:*` module
- Yandex MapKit API key lives in `local.properties` only

## Key classes
- `core/common/.../noise/NoiseMeter.kt` — measures dB from microphone
- `core/data/.../entity/NoiseMeasurementEntity.kt` — Room entity
- `core/data/.../repository/NoiseRepository.kt` — single source of truth for measurements
- `core/ui/.../noise/NoiseUi.kt` — shared colors, labels and formatting for noise values
- `feature/map/.../MapScreen.kt` — Compose map screen (Yandex MapKit via AndroidView)
- `feature/history/.../HistoryScreen.kt` — Compose list of saved measurements
- `ui/home/HomeFragment.kt` — hosts the Compose Map screen
- `ui/history/HistoryFragment.kt` — hosts the Compose History screen
