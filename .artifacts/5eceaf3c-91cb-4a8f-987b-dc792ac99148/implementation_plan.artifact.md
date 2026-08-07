# Internationalization and Score Persistence

This plan addresses two main requests: supporting all Play Store countries through extensive translations and making the "Recorde" (High Score) persistent so it's not lost when the app closes.

## User Review Required

> [!IMPORTANT]
> - I will implement translations for over 50 major languages, which covers the vast majority of the 177+ countries on the Play Store.
> - I will use `SharedPreferences` to save your high score permanently on the device's storage.

## Proposed Changes

### Game Logic

#### [MODIFY] [GameView.kt](file:///C:/Users/luizb/Documents/Codex/2026-07-25/q/outputs/AndroidStudioGame/app/src/main/java/com/example/dodger/GameView.kt)
- **Persistence:** Add logic to load the high score from `SharedPreferences` on startup and save it whenever a new record is achieved.
- **i18n:** Update drawing logic to use string resources with placeholders (e.g., `getString(R.string.score, score)`).

### Resources

#### [MODIFY] [strings.xml](file:///C:/Users/luizb/Documents/Codex/2026-07-25/q/outputs/AndroidStudioGame/app/src/main/res/values/strings.xml)
- Define base string keys: `score_text`, `best_score_text`, `game_over_title`, `restart_hint`.

#### [NEW] Localized Strings
- I will generate a comprehensive set of `strings.xml` files for major languages (en, es, fr, de, it, ja, ko, zh, ru, hi, ar, etc.) to ensure global coverage.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to ensure all new resource files are valid.

### Manual Verification
- **Score:** Play the game, set a record, close the app completely, and reopen to verify the "Recorde" is still there.
- **Language:** Change device language settings to verify the UI updates correctly.
