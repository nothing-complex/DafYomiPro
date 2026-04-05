package com.dafyomi.pro.domain

data class DafData(
    val dafIndex: Int,
    val masechet: Masechet,
    val dafNumber: Int,
    val cycleDay: Int,
    val cyclePercent: Float,
    val hebrewText: String?,     // Actual Hebrew text from Sefaria
    val englishText: String?,    // English translation from Sefaria (if available)
    val summary: String           // Fallback summary
)
