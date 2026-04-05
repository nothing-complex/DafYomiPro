# Daf Yomi Pro

A beautiful Android app for daily Daf Yomi study, featuring:

- **Elegant typography** with Hebrew focus and transliteration guides
- **Time-based dark mode** (auto dark after 6pm, light after 6am)
- **Share functionality** to spread Torah learning
- **Cycle progress tracking** (day X of 2,711)
- **Actual Hebrew text** from Sefaria API

## Architecture

```
DafYomiPro/
├── ui/
│   ├── DafScreen.kt      # Main Compose UI
│   ├── DafViewModel.kt   # State management
│   └── theme/            # Color, typography, theming
└── domain/
    ├── DafCalculator.kt   # Core cycle calculation
    ├── DafRepository.kt  # Data fetching
    ├── SefariaApiService.kt  # Sefaria API integration
    └── *.kt             # Data models
```

### Tech Stack

- **Jetpack Compose** for UI
- **MVVM** architecture
- **Kotlin Coroutines** for async
- **CompositionLocal** for theme-aware colors

## Daf Yomi Cycle

The 14th cycle began January 5, 2020 (Berachot 2) and runs for 2,711 daf through 38 masechtot, completing ~July 2027.

**Current cycle data (as of April 2026):**
- ~Day 2,283 of 2,711
- Currently in Niddah

## Theme System

Three theme modes:
- **Off** - Always light mode
- **On** - Always dark mode
- **Auto** - Time-based (6am-6pm light, 6pm-6am dark)

Colors use a sand/stone palette for light mode and charcoal/cream for dark mode, with animated sand dune backgrounds.

## Data Sources

- **Daf calculation**: Self-contained algorithm (no API needed)
- **Hebrew text**: Sefaria.org API (CC-BY licensed)
- **Cycle verification**: HebCal iCal feed

## Building

```bash
./gradlew assembleDebug
```

APK installs to connected device:
```bash
./gradlew installDebug
```

## Marketing

App icon: Open book with Star of David, designed to be immediately recognizable as a Jewish study app without relying on potentially incorrect Hebrew text.

See `marketing/Logo.jpg` for the approved logo design.
