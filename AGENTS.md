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

**Phase 2 completed** (Local storage with Room works).  
**Next: Phase 3 — Map Visualization** (color-coded placemarks from saved measurements)

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
- `ui/home/HomeFragment.kt` — current Map screen (still View-based, will migrate)
