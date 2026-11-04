# Development Plan — noiseDet2

> This document is the **single source of truth** for the current development state and next steps of the project.  
> Any AI agent or developer working on this repository **must read this file first**.

**Last updated:** 2026-11-04  
**Project stage:** Early MVP (map + location only)  
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

## 2. Current State (What already exists)

| Component                    | Status      | Notes                                      |
|-----------------------------|-------------|--------------------------------------------|
| Project structure           | Done        | Standard Android + Bottom Navigation       |
| Yandex MapKit integration   | Done        | API key hardcoded in MainActivity          |
| Location detection          | Partially   | Works, but permission handling is weak     |
| Map tab (Home)              | Basic       | Full-screen map + location button          |
| Database tab                | Empty       | Only template text                         |
| Analysis tab                | Empty       | Only template text                         |
| Noise measurement           | Missing     | —                                          |
| Local storage (Room)        | Missing     | —                                          |
| Markers on map              | Missing     | —                                          |
| Proper architecture (MVVM)  | Weak        | Almost no ViewModels used properly         |

### Known technical debt
1. Double layout inflation in `HomeFragment.onCreateView`
2. Hardcoded Yandex API key
3. No proper permission result handling
4. Package name still `com.example.noisedet2`
5. No `RECORD_AUDIO` permission yet
6. Markers accumulate on every location click

---

## 3. Step-by-step Development Plan

### Phase 0 — Foundation & Cleanup (Priority: Critical)
**Goal:** Make the existing code clean and ready for real features.

1. Fix `HomeFragment` (remove double inflation, use ViewBinding correctly)
2. Move Yandex API key out of source code (`local.properties` + BuildConfig)
3. Implement modern permission handling (Activity Result API) for Location + future Audio
4. Add `RECORD_AUDIO` permission to Manifest
5. Change applicationId / namespace from `com.example.noisedet2` to something proper (e.g. `com.noisedet.app` or keep for now)
6. Clean up unused code and comments
7. Add basic error handling for MapKit lifecycle

**Definition of Done:** App launches cleanly, location works reliably, no critical code smells.

---

### Phase 1 — Noise Measurement Core
**Goal:** Ability to measure noise level in dB.

1. Create `NoiseMeter` utility class using `AudioRecord`
2. Implement RMS → dB calculation
3. Add measurement duration (3–5 seconds recommended)
4. Show live / final dB value in UI (floating card or bottom sheet on Map tab)
5. Handle microphone permission properly
6. Add simple calibration offset (optional, relative)

**Definition of Done:** User can press a button and get a reasonably stable dB value.

---

### Phase 2 — Local Storage
**Goal:** Persist measurements on device.

1. Add Room dependencies
2. Create `NoiseMeasurement` entity:
   ```kotlin
   id, latitude, longitude, dbLevel, timestamp, note?
   ```
3. Create Dao + Database + Repository
4. Save measurement after successful noise capture + current location
5. Load all measurements for the map and list

**Definition of Done:** Measurements survive app restart.

---

### Phase 3 — Map Visualization
**Goal:** Show noise data on the map.

1. Display saved measurements as placemarks
2. Implement color scale:
   - < 50 dB → Green
   - 50–70 → Yellow
   - 70–85 → Orange
   - > 85 → Red
3. Clear old temporary location markers
4. Optional: cluster markers when zoomed out
5. Click on marker → show dB + time info

**Definition of Done:** Map shows historical noise points with correct colors.

---

### Phase 4 — Database Tab
**Goal:** Manage local measurements.

1. RecyclerView with list of measurements
2. Show: dB, date/time, approximate address (optional reverse geocoding)
3. Delete single measurement
4. Clear all (with confirmation)
5. Basic sorting / filtering by date or noise level

**Definition of Done:** User can review and delete local history.

---

### Phase 5 — Analysis Tab (Basic)
**Goal:** Simple insights.

1. Average / Max / Min noise level
2. Number of measurements
3. Simple time-based chart (last 7 / 30 days)
4. Optional: distribution by noise categories

**Definition of Done:** User sees basic personal statistics.

---

### Phase 6 — Route Planning (Avoid Noise) — Later
1. Allow user to choose start + end point
2. Use noise points as "cost" for routing (or simple avoidance zones)
3. Integrate with Yandex routing or custom logic
4. Display quietest reasonable route

---

### Phase 7 — Cloud & Crowdsourcing (Future)
1. Backend (Firebase / custom)
2. Authentication
3. Upload measurements (with privacy controls)
4. Download public noise layer
5. Dynamic public map
6. Moderation tools

---

## 4. Recommended Architecture Going Forward

```
app/
├── data/
│   ├── local/
│   │   ├── NoiseMeasurement.kt
│   │   ├── NoiseDao.kt
│   │   └── AppDatabase.kt
│   ├── model/
│   └── repository/
│       └── NoiseRepository.kt
├── domain/               # optional for now
├── ui/
│   ├── map/              # HomeFragment + MapViewModel
│   ├── database/         # list of measurements
│   └── analysis/
├── utils/
│   └── NoiseMeter.kt
└── MainActivity.kt
```

**Principles:**
- MVVM
- Single source of truth = Repository + Room
- ViewModels should not know about Android framework details when possible
- Prefer Kotlin Coroutines + Flow

---

## 5. Immediate Next Actions (for any agent)

When starting work on this project, do the following in order:

1. Read this file completely
2. Read `README.md`
3. Inspect current `HomeFragment.kt` and `MainActivity.kt`
4. Start with **Phase 0** (cleanup) unless explicitly told otherwise
5. After each phase — update this document (mark completed items)

---

## 6. Notes for AI Agents

- Prefer small, focused commits
- Do not introduce cloud features until Phase 7
- Keep local-first philosophy
- When adding dependencies — update both `libs.versions.toml` and this plan if needed
- Yandex MapKit key must never be committed in plain text in the future
- Always handle runtime permissions properly

---

**End of Development Plan**
