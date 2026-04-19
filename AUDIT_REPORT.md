# DafYomiPro — Complete Studio Audit
**Date:** 2026-04-19  
**Version Audited:** 1.0 (versionCode 1, targetSdk 34)  
**Departments:** Engineering · Security · Performance · Accessibility · UI · Hebrew Domain  

---

## Executive Summary

The app is **well-structured at the architectural level** — clean MVVM, proper ViewModel scoping, good domain/UI separation. However it has **two correctness bugs that affect every user** (wrong Hebrew dates, fragile JSON parsing), **zero accessibility**, and **a year's worth of stale dependencies**. These must be resolved before any Play Store submission.

---

## 🔴 Critical — Fix Before Release

### 1. HebrewDateFormatter Produces Wrong Dates
**File:** `domain/HebrewDateFormatter.kt`  
The Hebrew date display is hand-rolled using fixed Gregorian day-of-year ranges to estimate Hebrew months. Hebrew months shift ~11 days per year relative to Gregorian; this approach produces errors of several days near month boundaries. On April 19, 2026 (כ"א ניסן) the formatter will output a wrong month or day for many users.

**Fix:** Replace with `com.kosherjava:zmanim` or `com.github.yevsei:JewishCalendar`.

---

### 2. No Accessibility (TalkBack Is Completely Broken)
**File:** `ui/DafScreen.kt`  
Every interactive element lacks a `contentDescription`. TalkBack reads nothing or garbage:
- Hamburger/settings: Unicode `☰` — TalkBack reads "comma" or nothing
- Share icon: raw Canvas draw — invisible to screen readers
- Font size buttons (A/A/A): no labels
- Theme option rows: no labels

**WCAG 4.1.2 violation on every control.**

**Fix:** Add `semantics { contentDescription = "..." }` or `Modifier.semantics {}` to all interactive elements.

---

### 3. Manual JSON Parsing Will Silently Break
**File:** `domain/SefariaApiService.kt`  
`parseV2Response()`, `extractTextFromField()`, `parseNestedArray()` use `indexOf` and substring arithmetic to parse the Sefaria API response. Any API format change — new field ordering, escaped characters, new nesting level — silently returns `null`, showing the user a blank screen with no error log.

**Fix:** Add `org.json:json` or `kotlinx.serialization` (already on the Kotlin plugin stack) and parse properly.

---

## 🟠 High — Fix Before Launch

### 4. CYCLE_LENGTH Hardcoded and Mismatched
**File:** `domain/DafCalculator.kt`  
`CYCLE_LENGTH = 2713` but the sum of all masechet page counts in `Masechet.kt` = **2680** (delta of 33). The algorithm works because it's calibrated against specific reference dates, but if any masechet count is corrected, the entire cycle drifts silently.

**Fix:** Derive `CYCLE_LENGTH` from `sum(masechet.dafCount)` at runtime, or add an assertion that fails fast if the constant doesn't match.

---

### 5. Hebrew Text Renders LTR (Wrong Direction)
**File:** `ui/DafScreen.kt`, `SummarySection`  
```kotlin
Text(text = displayText, textAlign = TextAlign.Start)  // Wrong for RTL Hebrew
```
`TextAlign.Start` follows the app's layout direction (LTR by default in Compose). Hebrew text must have `textDirection = TextDirection.Rtl` explicitly set.  
**WCAG 1.3.2 violation.**

---

### 6. Background Animations Ignore reduceMotion
**File:** `ui/DafScreen.kt`, `BackgroundAnimation()`  
`rememberInfiniteTransition` runs unconditionally regardless of system accessibility settings. Users with vestibular disorders who enable **Reduce Motion** get no relief.

**Fix:** Gate on `LocalContext.current.getSystemService(AccessibilityManager)?.isEnabled` or use `androidx.compose.ui.platform.LocalConfiguration` to check `fontScale` and motion preferences.

---

### 7. Dual Material Dependencies (APK Bloat)
**File:** `app/build.gradle.kts`  
```kotlin
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.material:material:1.6.0")   // UNUSED
```
Only `material3` components are used in code. The legacy `material` dependency adds ~1-2MB to APK with no benefit.

**Fix:** Remove `material:1.6.0`.

---

### 8. Background Animation Jank
**File:** `ui/DafScreen.kt` lines 461-531  
The wave animation loops `for (x in 0..width step 10)` computing `sin()` per pixel column, 3 paths, every frame at 60fps = ~432 trig calls per frame just for decoration. On mid-range devices this will cause jank during scroll.

**Fix:** Pre-compute the wave path into a `Path` object, cache it with `remember`, and only recompute when wave phase changes. Or use `graphicsLayer` for offset-based animation instead of redrawing.

---

## 🟡 Medium — Fix Within First Update

### 9. Unbounded Repository Caches
**File:** `domain/DafRepository.kt`  
`textCache` and `summaryCache` are plain `mutableMapOf` with no eviction. Long sessions accumulate entries for every masechet viewed.

**Fix:** Replace with `LruCache(maxSize = 10)`.

---

### 10. No Network Retry
**File:** `domain/SefariaApiService.kt`  
A single API failure shows an error with no retry. Users on unreliable connections will be stuck.

**Fix:** Add exponential backoff retry (3 attempts) in `getDafText()`.

---

### 11. Stale Dependencies (1 Year Behind)
**File:** `app/build.gradle.kts`

| Dependency | Current | Should Be |
|---|---|---|
| compileSdk | 34 | 35 |
| Compose BOM | 2024.02.00 | 2024.12.01+ |
| `activity-compose` | 1.8.2 | 1.9.x |
| `datastore` | 1.0.0 | 1.1.1 |

---

### 12. StoneMuted Fails WCAG AA Contrast
**File:** `ui/theme/Color.kt`  
`StoneMuted (#A69F96)` on `SandLight (#F5F0E8)` = **3.8:1 contrast** — fails WCAG AA minimum of 4.5:1. Used for secondary text in light mode.

**Fix:** Darken `StoneMuted` to at least `#857D74` in light mode.

---

### 13. `state.daf!!` Non-Null Assertion Crash Risk
**File:** `ui/DafScreen.kt` lines 88, 99  
`state.daf!!` and `state.daf!!.hebrewText` are used after `state.daf != null` guard, which is logically safe today but fragile against future state model changes.

**Fix:** Use `state.daf?.let { ... } ?: return` pattern, or convert `DafState` to a sealed class (`Loading`, `Success(DafData)`, `Error(String, DafData?)`).

---

### 14. SettingsDialog Local State Resets on Recomposition
**File:** `ui/DafScreen.kt`  
```kotlin
var selectedTheme by remember { mutableStateOf(currentThemeMode) }
```
`remember` captures the initial value at composition time. If parent recomposes, `selectedTheme` resets to stale value. Use `rememberUpdatedState`.

---

### 15. Settings Panel Not Material3-Compliant
**File:** `ui/DafScreen.kt`  
The settings overlay is a custom `Box` with a black scrim — no slide animation, no drag-to-dismiss, no Material3 bottom sheet semantics. Inconsistent with system UI conventions.

**Fix:** Replace with `ModalBottomSheet` from `material3`.

---

### 16. Touch Targets Below 48dp
**File:** `ui/DafScreen.kt`  
`FontSizeButton` is `height(44.dp)` (below Android's recommended 48dp). `ThemeOption` rows are even smaller. **WCAG 2.5.5.**

---

## 🔵 Low — Cleanup / Polish

### 17. Infinite While-Loop Lacks Job Reference
**File:** `ui/DafViewModel.kt`  
`startDateChangeDetector()` launches an unbounded `while (true)` with no stored `Job`. Works via `viewModelScope` cancellation implicitly, but should store the Job for explicit control.

### 18. Theme.kt Also Has Infinite Loop
**File:** `ui/theme/Theme.kt`  
A `LaunchedEffect` containing `while (true) { delay(60_000); currentHour = ... }` runs on every theme recomposition. Use `withContext(Dispatchers.Default)` and extract to ViewModel instead.

### 19. All UI Strings Hardcoded
Only `app_name` is in `strings.xml`. "Settings", "APPEARANCE", "TEXT SIZE", theme labels, font labels, "Share with friends", "Spread the Torah learning" — all hardcoded. Blocks future localization.

### 20. Canvas-Drawn Icons
Share icon and radio selectors are drawn with raw `Canvas` line/circle calls. `Icons.Rounded.Share` and `RadioButton` composables are drop-in replacements that are more maintainable and accessible.

### 21. Unicode Hamburger `☰`
Fragile across fonts. Use `Icons.Default.Menu` instead.

### 22. No Baseline Profile
No baseline profile configured. First launch after install is slower. Add Baseline Profile plugin.

### 23. No Tests
Zero unit tests for `DafCalculator` (core algorithm), `HebrewDateFormatter`, `SettingsManager`, or `SefariaApiService`. Zero UI tests for state transitions.

---

## Priority Fix Order

| # | Issue | Effort | Impact |
|---|---|---|---|
| 1 | Replace HebrewDateFormatter | Medium | Every user sees wrong dates |
| 2 | Add contentDescriptions everywhere | Low | Full TalkBack support |
| 3 | Add proper JSON parsing library | Low | Resilience to API changes |
| 4 | Fix Hebrew TextDirection.Rtl | Low | Correct RTL rendering |
| 5 | Remove legacy `material` dependency | Trivial | -2MB APK |
| 6 | Fix CYCLE_LENGTH derivation | Low | Correctness insurance |
| 7 | Add network retry | Low | UX on bad connections |
| 8 | Optimize BackgroundAnimation | Medium | 60fps on mid-range |
| 9 | Update dependencies to 2026 current | Low | Security patches |
| 10 | Convert DafState to sealed class | Low | State correctness |
