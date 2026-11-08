# Cooked 🔥

> **"so you're not cooked."**  
> A reminder + deadline companion designed for students, freelancers, and ADHD adults.

---

## What Cooked Solves
- **Nag-Until-Done Reminders**: Swiping a reminder notification away does **NOT** complete it. Cooked persistently re-alerts you until you physically tap **Done**.
- **Flagship Deadline Mode**: Tasks with hard deadlines get start-by dates, effort estimates, ramped urgency pressure, live countdowns, and a weekly **Cooked Score** (0–100) that shows your overload level.
- **Fast Capture (< 3s)**: Ultra-quick input with smart time chips (`+15m`, `+1h`, `Tonight`, `Tomorrow`) from the in-app bottom sheet, Glance home widget, or Quick Settings tile.
- **Aggressive OEM Defense**: Survives task killers (Xiaomi MIUI/HyperOS, Samsung OneUI, Oppo, Vivo, OnePlus) via `AlarmManager.setAlarmClock()`, boot receivers, and in-app whitelist guidance.

---

## Architecture & Tech Stack

Cooked follows clean architecture and unidirectional data flow (UDF) packaged for multi-module extraction:

```
com.cooked.app
├── core/        (designsystem, time/CookedClock, util/DispatcherProvider, di)
├── data/        (db, dao, entity, repository, datastore)
├── domain/      (model, repository interfaces, usecases)
├── scheduling/  (alarm scheduler, receivers, notification builder)
├── feature/     (home/today, deadlines, insights, add, detail, settings, onboarding, paywall)
└── widget/      (Glance home widget, Quick Settings tile)
```

- **Language & Framework**: Kotlin 2.2, Jetpack Compose + Material 3, Single Activity.
- **Navigation**: Navigation Compose with type-safe `@Serializable` routes.
- **Dependency Injection**: Dagger Hilt.
- **Local Persistence**: Room 2.7+ (KSP) + DataStore Preferences.
- **Alarms**: `AlarmManager.setAlarmClock()` (bypasses Doze & OEM power saves).
- **Widgets**: Jetpack Glance + `TileService`.
- **Quality**: Detekt + JUnit + Truth + Turbine + MockK.

---

## How to Build

### Prerequisites
- JDK 17+
- Android SDK 35 (`compileSdk 35`, `minSdk 26`)

### Commands
```bash
# Run Unit Tests
./gradlew testDebugUnitTest

# Run Detekt Static Analysis
./gradlew detekt

# Build Debug APK
./gradlew assembleDebug

# Build Release APK (R8 Minified)
./gradlew assembleRelease
```

---

## Documentation
- Detailed architectural guidelines: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)
