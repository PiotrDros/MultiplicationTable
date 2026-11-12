# Multiplication Table Game

An interactive Android application designed to help users (especially children) practice multiplication tables through a gamified experience with visual and audio feedback.

## Features
- **Customizable Ranges**: Practice multiplication using different digit sets (2-9, 2-5, or 6-9).
- **Gamified Rounds**: 10 rounds per session with real-time scoring.
- **Voice Guidance**: Every question and feedback is voiced using high-quality audio samples.
- **Modern UI**: Built with Jetpack Compose and Material 3 for a fluid, responsive experience.
- **Interactive Feedback**: Visual cues (Green/Red cards) and audio explanations for incorrect answers.

## Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Design System**: Material 3
- **Media**: Android MediaPlayer API
- **Architecture**: Separated Domain Logic (GameLogic) and UI (MainActivity).

## Project Structure
- `app/src/main/java/.../multiplicationtable/`
    - `MainActivity.kt`: Entry point, manages UI state (Start, Game, Summary screens).
    - `GameLogic.kt`: Core business logic for sessions, rounds, and scoring.
    - `AudioPlayer.kt`: Infrastructure for playing sequential audio samples.
- `app/src/main/res/`
    - `raw/`: Audio assets (`.m4a`) for digits, products, and instructional phrases.
    - `values/strings.xml`: Localized strings and formatting.

## Game Flow
1. **Start Screen**: User selects a multiplication range.
2. **Game Screen**:
    - The app "speaks" the question (e.g., "What is 5 times 6").
    - User enters an answer and submits.
    - App provides immediate visual and audio feedback.
    - Feedback persists until the audio explanation is finished.
3. **Summary Screen**: Displays final score and allows restarting or changing settings.

## How to Build
1. Open the project in **Android Studio (Ladybug or newer)**.
2. Sync with Gradle.
3. Run on a physical device or emulator (Android 8.0+ recommended for audio performance).

## Recent Improvements
- Synchronized UI clearing with audio completion.
- Logic to ensure the final round's feedback is displayed before the summary screen.
- Simplified `.gitignore` for better repository hygiene.
