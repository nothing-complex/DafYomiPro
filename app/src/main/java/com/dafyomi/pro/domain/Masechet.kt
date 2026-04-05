package com.dafyomi.pro.domain

/**
 * Represents a single masechet (tractate) in the Daf Yomi cycle.
 *
 * @property id Unique identifier (1-38) for the masechet in cycle order
 * @property hebrew Hebrew name in Unicode (e.g., "ברכות")
 * @property english English name (e.g., "Berachot")
 * @property transliteration Phonetic transliteration (e.g., "Berachot")
 * @property pronunciation Pronunciation guide (e.g., "beh-RAKH-ot")
 * @property dafCount Total number of daf (pages) in this masechet
 */
data class Masechet(
    val id: Int,
    val hebrew: String,
    val english: String,
    val transliteration: String,
    val pronunciation: String,
    val dafCount: Int
)
