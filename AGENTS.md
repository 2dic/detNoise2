# Agents Context — noiseDet2

This file exists so that any AI agent (Cursor, Claude, Grok, Devin, etc.) can quickly understand the project context.

## Mandatory reading order

1. **README.md** — Project vision and high-level overview
2. **docs/DEVELOPMENT_PLAN.md** — Detailed current state + step-by-step development plan (source of truth)
3. Key source files:
   - `app/src/main/java/com/example/noisedet2/MainActivity.kt`
   - `app/src/main/java/com/example/noisedet2/ui/home/HomeFragment.kt`

## Project in one sentence
Android app for measuring and mapping urban noise. Currently local-only storage. Future goal — crowdsourced public noise map + quiet route planning.

## Current priority
**Phase 0 is completed.**  
Next: execute **Phase 1** (Noise Measurement Core) from `docs/DEVELOPMENT_PLAN.md`.

## Important constraints
- Storage is local for now (Room planned)
- Do not implement cloud/backend yet
- Yandex MapKit is already integrated (API key via BuildConfig + local.properties)
- Keep changes focused and incremental

## Yandex MapKit API key
Put your key in `local.properties` (gitignored):
```
YANDEX_MAPKIT_API_KEY=your_key_here
```
