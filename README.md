# Block Crush (Jetpack Compose)

A polished Android puzzle experience inspired by the classic wooden block crush game. Place three blocks at a time on a 10×10 board, clear full rows or columns, and chase the best score with a single-step undo and configurable visual themes.

## How to run

```bash
./gradlew assembleDebug
```

The app targets Android API 26+ and uses Jetpack Compose for all UI.

## Architecture overview

- **Game logic** lives in `com.example.blockcrush.game` with immutable state, placement validation, line clearing, and scoring helpers.
- **Models** for block shapes and theme mode are under `com.example.blockcrush.model`.
- **Persistence** is handled by `SettingsRepository` (`com.example.blockcrush.data`) using DataStore to save best score, theme preference, and toggles for sound and haptics.
- **Presentation** uses a single `GameViewModel` to bridge logic and UI, exposing state via `StateFlow`.
- **UI layer** is written with Compose in `com.example.blockcrush.ui`, separating the game screen and settings screen. Canvas components render the board, pieces, and drag ghost preview.

## Gameplay parameters

- Board size: 10×10 grid (`BOARD_SIZE` in `GameState.kt`).
- Shape catalog: `ShapesCatalog` in `model/Shapes.kt` defines all available blocks.
- Scoring rules: `Scoring` in `game/GameState.kt` (per-cell placement plus line and combo bonuses).

## Features

- Three draggable blocks with ghost previews for valid placement.
- Automatic line clears with scoring bonuses for combos.
- One-step undo restoring board, score, and upcoming blocks.
- Settings with mono or colored themes plus sound/haptics toggles (persisted via DataStore).
- Best score tracking saved between sessions.
