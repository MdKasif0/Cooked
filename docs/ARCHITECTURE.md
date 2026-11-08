# Architecture Overview

1. **Pattern**: Offline-first, single-activity MVVM with unidirectional data flow (UDF).
2. **`core/`**: Cross-cutting foundation—design system, `CookedClock`, `DispatcherProvider`, `CookedResult`, and OEM battery defense helpers.
3. **`domain/`**: Pure Kotlin business models (`Reminder`, `CookedScore`, `PressureLevel`) and use cases. No Android framework dependencies.
4. **`data/`**: Room database, DAO, entity persistence, DataStore preferences, and repository implementation.
5. **`scheduling/`**: Exact `AlarmManager.setAlarmClock()` triggers, heads-up notifications, and broadcast receivers resilient to reboot and OEM task killers.
6. **`feature/`**: Feature slices (`home/today`, `deadlines`, `insights`, `add`, `detail`, `settings`, `onboarding`, `paywall`).
7. **UI Separation**: Every feature contains `XScreen` (stateless), `XRoute` (stateful), `XViewModel`, and `XUiState`.
8. **Navigation**: Compose Navigation with type-safe `@Serializable` routes and preserved bottom-bar tab state.
9. **Widgets**: Jetpack Glance app widgets and Android Quick Settings tile service for <3-second quick adds.
10. **State Management**: `StateFlow<UiState>` collected with lifecycle awareness (`collectAsStateWithLifecycle`).
11. **Modularity Path**: Packaged so each layer can seamlessly migrate into multi-project Gradle modules in subsequent milestones.

