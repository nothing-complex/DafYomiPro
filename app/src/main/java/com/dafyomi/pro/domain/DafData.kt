package com.dafyomi.pro.domain

/**
 * Represents the complete data for a specific Daf Yomi date.
 *
 * @property dafIndex 0-based index in the entire 2,711-daf cycle
 * @property masechet The masechet (tractate) this daf belongs to
 * @property dafNumber Page number within the masechet (1-indexed)
 * @property cycleDay Day number in the current cycle (1 to ~2,711)
 * @property cyclePercent Progress through cycle (0.0 to 1.0)
 * @property hebrewText Actual Hebrew text from Sefaria API (null if unavailable)
 * @property englishText English translation from Sefaria (null if unavailable)
 * @property summary Fallback summary text when API text is unavailable
 */
data class DafData(
    val dafIndex: Int,
    val masechet: Masechet,
    val dafNumber: Int,
    val cycleDay: Int,
    val cyclePercent: Float,
    val hebrewText: String?,
    val englishText: String?,
    val summary: String
)
