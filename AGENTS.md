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
:core:common
:core:ui
:core:data
:feature:map
:feature:history
:feature:analysis
```

**Stack:** Jetpack Compose + Material 3 + Hilt + KSP + Room + Navigation Compose + Yandex MapKit + Coroutines/Flow

## Current priority

**Phase 0 + 0.5 completed.**  
Next: **Phase 1 — Noise Measurement Core**

## Important constraints
- Storage is local for now
- Do not implement cloud/backend yet
- New UI must be written in Jetpack Compose
- Put new feature code into the corresponding `feature:*` module
- Yandex MapKit API key lives in `local.properties` only

## Yandex MapKit API key
```
YANDEX_MAPKIT_API_KEY=your_key_here
```
(in local.properties)
