# Development Plan — noiseDet2

> This document is the **single source of truth** for the current development state and next steps of the project.  
> Any AI agent or developer working on this repository **must read this file first**.

**Last updated:** 2026-11-04  
**Project stage:** Phase 2 complete — Local storage working  
**Storage model (current):** Fully local (Room)  
**Target:** Crowdsourced urban noise mapping app

---

## 1. Project Goal (Context)

**noiseDet2** is an Android application for measuring and mapping urban noise pollution.

### Core value propositions
- Ordinary users measure noise levels and see quiet/noisy places on a map.
- Users can plan walking routes that avoid noisy areas.
- In the future — crowdsourced public noise map for citizens and authorities.

### Current constraint
All measurement history is stored **only locally** on the device.  
Cloud synchronization and public shared map are **future** goals.

---

## 2. Target Architecture & Stack

### Multi-module structure

```
:app                    # Thin application module
:core:common            # Pure Kotlin utilities (NoiseMeter lives here)
:core:ui                # Design system, Compose theme, common components
:core:data              # Room, repositories, data sources
:feature:map            # Map + noise measurement
:feature:history        # List of measurements
:feature:analysis       # Statistics & charts
```

### Technology stack (modern)

| Layer              | Technology                                      |
|--------------------|-------------------------------------------------|
| UI                 | Jetpack Compose + Material 3 (migration ongoing)|
| Navigation         | Navigation Compose                              |
| Architecture       | MVVM + UDF (StateFlow)                          |
| DI                 | Hilt                                            |
| Async              | Coroutines + Flow / StateFlow                   |
| Local DB           | Room + KSP                                      |
| Maps               | Yandex MapKit + Compose AndroidView             |
| Noise measurement  | AudioRecord via NoiseMeter                      |
| Build              | Version Catalog + KSP                           |

---

## 3. Current State

| Component                    | Status          | Notes                                      |
|-----------------------------|-----------------|--------------------------------------------|
| Multi-module structure      | Done            | core + feature modules                     |
| Modern stack foundation     | Done            | Compose, Hilt, KSP, Room ready             |
| Yandex MapKit               | Working         | API key via BuildConfig                    |
| Location                    | Working         | Activity Result API                        |
| Noise measurement (dB)      | **Working**     | NoiseMeter + UI on Map tab                 |
| Live dB + category colors   | Working         | Green / Yellow / Orange / Red              |
| Room / local storage        | **Working**     | Entity + Dao + Repository + Hilt           |
| Save after measurement      | **Working**     | dB + lat/lng + timestamp                   |
| Markers for measurements    | Missing         | Phase 3                                    |
| History / Analysis tabs     | Empty           | Phase 4 / 5                                |

---

## 4. Step-by-step Development Plan

### Phase 0 — Foundation & Cleanup ✅ DONE
### Phase 0.5 — Multi-module + Modern Stack ✅ DONE

### Phase 1 — Noise Measurement Core ✅ DONE
- [x] `NoiseMeter` class (AudioRecord → RMS → dB)
- [x] Continuous measurement Flow + auto-stop after ~4s
- [x] Measure button + live dB display on Map screen
- [x] Microphone permission handling
- [x] Color-coded noise categories (Quiet / Moderate / Loud / Very loud)

### Phase 2 — Local Storage (Room) ✅ DONE
- [x] `NoiseMeasurementEntity` in `core:data`
- [x] `NoiseMeasurementDao` + `AppDatabase`
- [x] `NoiseRepository`
- [x] Hilt `DatabaseModule`
- [x] `@HiltAndroidApp` + `@AndroidEntryPoint`
- [x] Save measurement (dB + lat/lng + timestamp) after successful capture

### Phase 3 — Map Visualization  ← NEXT
1. Show saved measurements as color-coded placemarks
2. Marker click → show dB + time
3. Migrate Map screen toward Compose

### Phase 4 — History Feature
### Phase 5 — Analysis Feature
### Phase 6 — Quiet Route Planning (Later)
### Phase 7 — Cloud & Crowdsourcing (Future)

---

## 5. Immediate Next Actions (for any agent)

1. Read this file + `AGENTS.md` + `README.md`
2. **Next priority: Phase 3 — Map Visualization**
3. New code should go into the correct module (`core:data`, `feature:*`)
4. Prefer Compose for new UI
5. Update this document after finishing a phase

---

## 6. Notes for AI Agents

- Do not implement cloud features until Phase 7
- Keep local-first philosophy
- Yandex MapKit key must stay in `local.properties`
- `NoiseMeter` lives in `:core:common`
- Always handle runtime permissions properly

### How to set Yandex MapKit API key
In `local.properties` (gitignored):
```
YANDEX_MAPKIT_API_KEY=your_real_key_here
```

---

**End of Development Plan**
