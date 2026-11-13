# Obsidian Sudoku — UI & motion upgrade

Scope: the Play experience. No game logic, ViewModel, data or Firebase code was changed.

## New
- `ui/components/Fx.kt` — `Modifier.bounceClick` (spring press-scale + haptic tick) and `ConfettiBurst` (Canvas particles).

## Rewritten
- `SudokuGridView.kt`
  - Board intro ripple from the top-left whenever a new puzzle starts
  - Animated selection ring that trails out as you move
  - Digit spring-pop when placed; wrong digit shakes the cell + haptic
  - Completion wave when a row, column or box is finished
  - Crisp Canvas grid lines with glowing 3x3 block lines, gradient frame, ambient glow
  - Digit and note sizes scale to the cell; notes matching the selected number highlight
  - TalkBack: every cell announces row, column, value / notes, given, incorrect
- `SudokuControls.kt` — equal-width action buttons, taller numpad (64dp), press bounce, per-digit progress meter,
  highlight of the selected number, notes-mode tint, fully theme-aware
- `PersistentTimerComponent.kt`, `SudokuPauseOverlay.kt`, `DifficultyDialog.kt` — theme-aware, animated
- `PlayScreen.kt` — mistake pips with shake, animated feedback banner (fade/expand), tablet width cap,
  pause overlay now covers the board exactly, victory screen (confetti, count-up score, animated stars),
  game-over screen (spring-in, wobble)

## Not yet built or run
This was written without an Android SDK, so it has not been compiled. Open in Android Studio and run
`./gradlew assembleDebug`. If a compile error appears, it is most likely a missing import.
