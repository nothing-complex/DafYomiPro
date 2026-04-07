# Daf Yomi Pro

A beautiful Android app for daily Daf Yomi study, featuring:

- **Elegant typography** with Hebrew focus, transliteration, and pronunciation guides
- **Hebrew date display** showing the current Jewish calendar date
- **Time-based dark mode** (auto dark after 6pm, light after 6am)
- **Share functionality** to spread Torah learning
- **Cycle progress tracking** (day X of 2,711)
- **Actual Hebrew text** from Sefaria API with English toggle
- **Settings persistence** (theme and font size saved between sessions)
- **Offline indicator** when network is unavailable

## Architecture

```
DafYomiPro/
├── app/
│   └── src/main/java/com/dafyomi/pro/
│       ├── DafYomiApp.kt            # Application class (entry point)
│       ├── domain/                  # Domain layer
│       │   ├── DafCalculator.kt    # Core cycle calculation algorithm
│       │   ├── DafData.kt          # Data model for Daf Yomi
│       │   ├── DafRepository.kt    # Data fetching with caching
│       │   ├── HebrewDateFormatter.kt # Hebrew date formatting
│       │   ├── Masechet.kt         # Data model for tractates
│       │   ├── SefariaApiService.kt # Sefaria API client
│       │   └── SettingsManager.kt   # DataStore-based settings persistence
│       └── ui/                     # UI layer
│           ├── DafScreen.kt        # Main Compose UI
│           ├── DafViewModel.kt     # ViewModel for state management
│           ├── MainActivity.kt     # Main activity
│           └── theme/              # Theme system
│               ├── Color.kt        # Color palette
│               ├── Theme.kt        # Theme provider with auto dark mode
│               └── Typography.kt   # Swiss/International style typography
```

### Tech Stack

- **Jetpack Compose** for UI
- **MVVM** architecture with Clean Architecture principles
- **Kotlin Coroutines** for async operations
- **DataStore Preferences** for persistent settings
- **CompositionLocal** for theme-aware colors

## Daf Yomi Cycle

The 14th cycle began January 5, 2020 (Berachot 2) and runs for 2,711 daf through 38 masechtot, completing approximately July 2027.

**Current cycle data (as of April 2026):**
- Day ~2,283 of 2,711
- Currently in Niddah

## Theme System

Three theme modes:
- **Off** - Always light mode
- **On** - Always dark mode
- **Auto** - Time-based (6am-6pm light, 6pm-6am dark)

Colors use a sand/stone palette for light mode and charcoal/cream for dark mode, with animated sand dune backgrounds in light mode and star fields in dark mode.

## Data Sources

- **Daf calculation**: Self-contained algorithm (no API needed)
- **Hebrew text**: Sefaria.org API (CC-BY licensed)
- **Hebrew date**: Calculated locally from system date

## Features

### Font Size Control
The font size settings (A/A/A buttons in settings) control only the **body text** (Hebrew/English daf content). The header text (Hebrew name, daf number, transliteration, progress) remains fixed at its elegant default size.

### Day Change Detection
The app automatically detects when the day changes while the app is open (e.g., past midnight) and reloads the new daf. No need to force-close and reopen.

### Settings Persistence
Theme mode and font size preferences are saved using DataStore and restored on app restart.

### Offline Support
When the Sefaria API is unavailable, the app shows an "Offline - showing cached text" indicator and displays the built-in summary text.

## Building

```bash
# Set up environment
export JAVA_HOME="/path/to/jdk-17"
export ANDROID_HOME="/path/to/android-sdk"

# Build debug APK
./gradlew assembleDebug

# Install to connected device
./gradlew installDebug
```

## Testing

Maestro tests are configured in the `.maestro/` directory. Run with:

```bash
maestro test .maestro/
```

## Version History

### v1.1 (Current)
- Add Hebrew date display
- Add settings persistence (DataStore)
- Add offline indicator
- Add day-change auto-detection
- Fix font size controls to only affect body text
- Lock portrait orientation
- Improve loading message

### v1.0
- Initial release with Daf Yomi display
- Sefaria API integration for Hebrew text
- Time-based dark mode
- Share functionality
- Animated backgrounds
