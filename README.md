# Lock Cracker

Lock Cracker is a Wear OS mechanical dial puzzle built with Kotlin and Jetpack Compose for Wear OS. The player rotates a lock dial, confirms combination steps, and opens the lock by following the direction and clearing rules for the selected difficulty.

## Features

- Rotary crown and touch-drag dial input.
- Easy, Medium, Hard, and Expert difficulty configurations.
- Classic Silver, Matte Black, Retro Brass, and Minimal White lock themes.
- Sound and haptic feedback settings.
- Persisted game settings with Android DataStore.
- Unit-tested lock engine and rotary input mapping.

## Tech Stack

- Kotlin
- Jetpack Compose for Wear OS
- AndroidX Navigation Compose
- Hilt
- Android DataStore
- JUnit, Truth, and kotlinx-coroutines-test

## Project Structure

```text
app/src/main/java/com/example/lockergame/
|-- data/settings        # Persisted user settings
|-- di                   # Dependency injection
|-- domain               # Lock models, engine, and difficulty config
|-- feature/game         # Main lock gameplay UI and state
|-- feature/home         # Home screen
|-- feature/settings     # Difficulty and customization screens
|-- navigation           # Compose navigation graph
`-- ui/theme             # App theme
```

## Requirements

- Android Studio with Android Gradle Plugin 9.2.0 support.
- JDK 17.
- Android SDK 37.
- A Wear OS emulator or device running API 30 or newer.

## Getting Started

Clone the repository and build the app:

```bash
./gradlew assembleDebug
```

Run unit tests:

```bash
./gradlew test
```

Install the debug build on a connected emulator or device:

```bash
./gradlew installDebug
```

The application ID is `com.example.lockergame`.

## Gameplay

Start a game from the home screen, rotate the dial with the Wear OS rotary crown or by dragging on the screen, then tap the center knob to confirm a selected value. Higher difficulties require stricter direction changes and clearing turns before the combination can be entered.
