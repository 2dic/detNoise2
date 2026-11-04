# noiseDet2

**Urban Noise Mapping Android App**

Measure, record and visualize noise levels around you. Build a personal noise map of the city and (in the future) contribute to a shared public noise map.

---

## Vision

noiseDet2 helps people understand and avoid noise pollution in urban environments.

**For regular users:**
- Measure noise levels in real time
- See noisy and quiet places on the map
- Plan walks and routes that avoid loud areas
- Keep a personal history of measurements

**For authorities, researchers and city planners:**
- Crowdsourced noise data from citizens
- Dynamic city-wide noise map
- Data-driven decisions on noise control and urban planning

> **Current stage**: All measurements are stored **locally** on the device. Cloud sync and a public shared map are planned for later versions.

---

## Current Status

Early development / MVP in progress.

### Already implemented
- Yandex MapKit integration
- Current location detection
- Bottom navigation with three tabs (Map / Database / Analysis)
- Basic project structure ready for further development

### In active development
- Real-time noise level measurement (dB) using the microphone
- Local storage of measurements (Room)
- Color-coded noise markers on the map
- List of saved measurements

---

## Roadmap

### Near-term (Local version)
- [ ] Noise measurement (on-demand / continuous)
- [ ] Save measurement: coordinates + dB + timestamp
- [ ] Color-coded markers on the map based on noise level
- [ ] List of all local measurements
- [ ] Basic statistics (average / max noise level)

### Mid-term
- [ ] Route planning that avoids noisy zones
- [ ] Filters by time and noise level
- [ ] Data export (CSV / JSON)
- [ ] Relative microphone calibration

### Long-term
- [ ] Shared cloud database + synchronization
- [ ] Public dynamically updated noise map
- [ ] Data moderation and verification
- [ ] Analytics dashboard for authorities and researchers
- [ ] Ability to attach notes / photos to measurements

---

## Tech Stack

| Component              | Technology                     |
|------------------------|--------------------------------|
| Language               | Kotlin                         |
| UI                     | ViewBinding + Material Design  |
| Maps                   | Yandex MapKit (Lite)           |
| Architecture           | MVVM                           |
| Local storage          | Room (planned)                 |
| Noise measurement      | AudioRecord (planned)          |
| Min SDK                | 24                             |
| Target SDK             | 35                             |

---

## App Structure

| Tab          | Purpose                                              |
|--------------|------------------------------------------------------|
| **Map**      | Interactive map + noise measurement + markers        |
| **Database** | List of local measurements, filters, deletion        |
| **Analysis** | Statistics, charts, analysis by time / area          |

---

## Getting Started

### Requirements
- Android Studio Hedgehog or newer
- JDK 11+
- Yandex MapKit API key ([get one here](https://developer.tech.yandex.ru/))

### Setup

1. Clone the repository:
```bash
git clone https://github.com/2dic/detNoise2.git
cd detNoise2
```

2. Add your Yandex MapKit API key:
   - Currently placed in `MainActivity.kt`
   - Recommended: move it to `local.properties` or BuildConfig for security

3. Open the project in Android Studio and wait for Gradle sync.

4. Run on a real device (recommended) or emulator.

> **Note**: Noise measurement requires `RECORD_AUDIO` and location permissions. Emulators have limited microphone quality.

---

## Planned Architecture

```
app/
├── data/
│   ├── local/          # Room (Entity, Dao, Database)
│   ├── model/          # NoiseMeasurement and related models
│   └── repository/     # NoiseRepository
├── domain/             # (optional) UseCases
├── ui/
│   ├── map/            # HomeFragment + ViewModel
│   ├── database/       # Measurements list
│   └── analysis/       # Statistics and charts
├── utils/
│   └── NoiseMeter.kt   # dB measurement logic
└── MainActivity.kt
```

---

## Noise Level Color Scale (planned)

| Level (dB) | Color     | Typical environment              |
|------------|-----------|----------------------------------|
| < 50       | Green     | Quiet courtyard, park            |
| 50–70      | Yellow    | Regular street                   |
| 70–85      | Orange    | Busy road                        |
| > 85       | Red       | Construction, highway, nightclub |

---

## Contributing

This project is in early development.  
Issues, suggestions and pull requests are welcome.

Repository: [https://github.com/2dic/detNoise2](https://github.com/2dic/detNoise2)

---

## License

License is not yet defined.  
The project is under active development.
