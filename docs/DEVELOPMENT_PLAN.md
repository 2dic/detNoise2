# Development Plan — noiseDet2

> This document is the **single source of truth** for the current development state and next steps of the project.  
> Any AI agent or developer working on this repository **must read this file first**.

**Last updated:** 2026-11-04  
**Project stage:** Multi-module foundation + modern stack migration  
**Storage model (current):** Fully local  
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
:core:common            # Pure Kotlin utilities
:core:ui                # Design system, Compose theme, common components
:core:data              # Room, repositories, data sources
:feature:map            # Map + noise measurement
:feature:history        # List of measurements
:feature:analysis       # Statistics & charts
```

### Technology stack (modern)

| Layer              | Technology                                      |
|--------------------|-------------------------------------------------|
| UI                 | Jetpack Compose + Material 3                    |
| Navigation         | Navigation Compose                              |
| Architecture       | MVVM + UDF (StateFlow)                          |
| DI                 | Hilt                                            |
| Async              | Coroutines + Flow / StateFlow                   |
| Local DB           | Room + KSP                                      |
| Maps               | Yandex MapKit + Compose AndroidView             |
| Noise measurement  | AudioRecord (later Oboe)                        |
| Build              | Version Catalog + KSP                           |

---

## 3. Current State

| Component                    | Status          | Notes                                      |
|-----------------------------|-----------------|--------------------------------------------|
| Multi-module structure      | Done            | core + feature modules created             |
| Version Catalog (modern)    | Done            | Compose, Hilt, KSP, Room, Navigation       |
| Hilt + KSP setup            | Done            | In app and library modules                 |
| Compose enabled             | Done            | In app and feature modules                 |
| Old View system             | Still present   | Temporary during migration                 |
| Yandex MapKit               | Working         | API key via BuildConfig                    |
| Location                    | Working         | Activity Result API                        |
| Noise measurement           | Missing         | Phase 1                                    |
| Room                        | Missing         | Phase 2                                    |

---

## 4. Step-by-step Development Plan

### Phase 0 — Foundation & Cleanup ✅ DONE
- Fixed HomeFragment, permissions, API key, RECORD_AUDIO, lifecycle

### Phase 0.5 — Multi-module + Modern Stack Foundation ✅ DONE
- Created multi-module structure
- Added Compose, Hilt, KSP, Navigation Compose, Room dependencies
- Updated Version Catalog

### Phase 1 — Noise Measurement Core (NEXT)
**Goal:** Ability to measure noise level in dB.

1. Create `NoiseMeter` in `core:common` or `core:data`
2. Implement RMS → dB calculation
3. Measurement duration 3–5 seconds
4. UI for starting measurement + showing dB value (Compose)
5. Proper microphone permission handling

### Phase 2 — Local Storage (Room)
1. `NoiseMeasurement` entity in `core:data`
2. Dao + Database + Repository
3. Hilt modules for providing Database/Dao/Repository
4. Save measurement after capture

### Phase 3 — Map Visualization (Compose)
1. Migrate Map screen fully to Compose + AndroidView(MapView)
2. Color-coded markers
3. Click on marker shows info

### Phase 4 — History Feature
1. Compose list of measurements in `feature:history`
2. Delete / clear functionality

### Phase 5 — Analysis Feature
1. Basic statistics
2. Simple charts

### Phase 6 — Quiet Route Planning (Later)
### Phase 7 — Cloud & Crowdsourcing (Future)

---

## 5. Immediate Next Actions (for any agent)

1. Read this file + `AGENTS.md` + `README.md`
2. Next priority: **Phase 1 — Noise Measurement Core**
3. Prefer writing new UI in Compose inside the correct feature module
4. Keep changes focused and incremental
5. After finishing a phase — update this document

---

## 6. Notes for AI Agents

- Do not implement cloud features until Phase 7
- Keep local-first philosophy
- Yandex MapKit key must stay in `local.properties`
- New screens → Compose
- Old XML screens will be migrated gradually
- Always handle runtime permissions properly

### How to set Yandex MapKit API key
In `local.properties` (gitignored):
```
YANDEX_MAPKIT_API_KEY=your_real_key_here
```

---

**End of Development Plan**
