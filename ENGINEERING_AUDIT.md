# DafYomiPro Engineering Audit

**Version:** 1 (targetSdk 34, minSdk 26)
**Review scope:** `app/src/main/java/com/dafyomi/pro/domain/` and `app/src/main/java/com/dafyomi/pro/ui/`

---

## Architecture

- **MVVM boundaries held**: Domain (DafCalculator, DafRepository, SefariaApiService, HebrewDateFormatter, SettingsManager) is cleanly separated from UI (DafScreen, DafViewModel). Repository pattern used correctly.
- **Single-activity architecture** is appropriate for this app's scope.
- Manual DI via `DafYomiApp.Application` is acceptable at this scale — no Hilt/Koin needed.
- ViewModel factory via companion object is pre-Hilt but consistent and not wrong.

---

## Kotlin / Coroutine Anti-Patterns

- **`startDateChangeDetector()` — infinite `while (true)` without Job tracking.** The coroutine launched in `viewModelScope` has no stored `Job` reference and no explicit `cancel()` path. Structured concurrency will cancel it when the ViewModel is cleared, but this is implicit. A `Job` reference should be stored and cancelled in `onCleared()` for clarity and safety.
- **`.first()` on Flow in `loadSavedSettings()`** is blocking but correctly wrapped in `viewModelScope.launch { ... }`, so it will not deadlock on cold start. However, it is semantically misuse — `.first()` subscribes and completes immediately on a hot flow, which works here but is a pattern easily misused in other contexts.
- **No structured concurrency cancellation token** for the date detector — if the ViewModel is cleared and re-created rapidly, multiple detector coroutines could overlap (though `viewModelScope` itself handles cleanup on ViewModel destruction).

---

## State Management

- **`DafState` is a data class, not a sealed class.** It is possible (though currently not triggered in the codebase) to have `isLoading=false`, `daf!=null`, AND `error!=null` simultaneously — an "ambient error" state. A sealed class with states like `Loading`, `Success(DafData)`, `Error(String, DafData?)` would enforce mutual exclusivity.
- **`textCache` and `summaryCache` in `DafRepository` are unbounded `mutableMapOf`.** Over a long session with many different masechtot viewed, this could grow unboundedly and cause OOM. Should be a `LruCache` or size-bounded cache.
- **`lastLoadedDate` is a plain `var`** — not thread-safe if `loadTodaysDaf()` and `startDateChangeDetector()` race. Both run in `viewModelScope` so sequential by default, but the mutable var pattern is fragile.

---

## Network Layer Robustness

- **No retry logic.** `getDafText()` catches all exceptions and returns `null` silently. If the Sefaria API is temporarily unavailable, the user sees an error or empty text with no automatic retry.
- **No connection pooling.** Each `HttpURLConnection` is created fresh. For a poll-every-minute app this is acceptable but inefficient.
- **Manual JSON parsing is fragile.** `parseV2Response()`, `extractTextFromField()`, and `parseNestedArray()` use `indexOf`, substring, and manual bracket-depth counting. If the Sefaria API response format changes even slightly (new field ordering, escaping changes), parsing silently returns null instead of a descriptive error.
- **Timeouts set to 15000ms** — reasonable for mobile networks but no exponential backoff on retry.
- **Response code handling:** HTTP non-200 returns `null` with no logging or user-facing feedback beyond the null bubble-up.

---

## Memory Leaks / Lifecycle

- **Unbounded caches** in `DafRepository` (noted above).
- **`viewModelScope` correctly scopes coroutines** — all `launch` calls use `viewModelScope`, so they are tied to ViewModel lifecycle and cancelled on `onCleared()`.
- **No `onCleared()` override** in `DafViewModel` — the date detector coroutine is implicitly cancelled via `viewModelScope` cancellation, but explicit `cancel()` of a stored Job would be safer.
- **No lifecycle-aware component usage** beyond standard ViewModel — `startDateChangeDetector()` polls regardless of app foreground/background state, which is a minor battery concern.

---

## ProGuard / Build

- **`proguard-rules.pro` is minimal boilerplate** — no custom keep rules for:
  - `ThemeMode.valueOf()` reflection call in `SettingsManager`
  - `DafState` data class serialization (if ever serialized)
  - Any domain model classes that might be obfuscated unexpectedly
- **No `minifyEnabled` configuration shown** — unclear if obfuscation is enabled or tested.

---

## Missing Tests

- **No unit tests** for `DafCalculator` (the core algorithmic logic — cycle calculation, masechet boundaries).
- **No unit tests** for `HebrewDateFormatter`.
- **No instrumented tests** for `SefariaApiService` (mocking the network layer).
- **No UI tests** for `DafScreen` state transitions (loading → success → error → offline).
- **No DataStore preference tests** for `SettingsManager`.

---

## Critical Bugs

1. **Manual JSON parsing will silently fail on API format changes.** `extractTextFromField` and `parseNestedArray` use string manipulation rather than a JSON library. A Sefaria API update could cause text to stop loading entirely with no error logged and no user notification beyond "error occurred".

2. **Unbounded caches in `DafRepository` can grow indefinitely.** `textCache` and `summaryCache` are `mutableMapOf` with no eviction. Over many sessions (or a single long session), this can cause memory pressure.

3. **No retry on network failure.** A single API blip shows an error to the user with no automatic retry, degrading UX on unreliable connections.

4. **`DafState` allows conflicting state.** The data class model permits `{isLoading=false, daf!=null, error!=null}` simultaneously, which could cause ambiguous UI rendering if the state machine ever reaches that combination.

---

## Summary of Risk Levels

| Issue | Severity |
|---|---|
| Manual JSON parsing fragility | Medium |
| Unbounded cache memory leak | Medium |
| No network retry | Medium |
| `DafState` ambient error state | Low |
| Infinite while-loop without Job reference | Low |
| ProGuard insufficient keep rules | Low |
| No tests | Medium (long-term maintenance) |