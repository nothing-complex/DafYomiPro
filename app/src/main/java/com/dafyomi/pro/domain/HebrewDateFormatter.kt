package com.dafyomi.pro.domain

import android.icu.util.HebrewCalendar
import java.time.LocalDate

/**
 * Utility for formatting Hebrew (Jewish) calendar dates.
 * Uses android.icu.util.HebrewCalendar (API 26+) for accurate Hebrew date calculations.
 * No external dependencies required.
 */
object HebrewDateFormatter {

    // Hebrew month names — HebrewCalendar months are 0=Nissan, 1=Iyar, ..., 12=Adar II
    private val hebrewMonthNames = listOf(
        "ניסן",      // 0
        "אייר",     // 1
        "סיון",     // 2
        "תמוז",     // 3
        "אב",       // 4
        "אלול",     // 5
        "תשרי",     // 6
        "מרחשוון",  // 7
        "כסלו",     // 8
        "טבת",      // 9
        "שבט",      // 10
        "אדר א׳",   // 11 (plain Adar in non-leap, Adar I in leap)
        "אדר ב׳"    // 12 (Adar II in leap years only)
    )

    /**
     * Formats a LocalDate as a Hebrew date string.
     * Returns format like "כ״א ניסן תשפ״ו"
     *
     * April 19, 2026 → "כ״א ניסן תשפ״ו"
     */
    fun formatHebrewDate(date: LocalDate): String {
        return try {
            val cal = HebrewCalendar().apply {
                set(date.year, date.monthValue - 1, date.dayOfMonth)
            }
            val day   = cal.get(HebrewCalendar.DAY_OF_MONTH)
            val month = cal.get(HebrewCalendar.MONTH)        // 0-based
            val year  = cal.get(HebrewCalendar.YEAR)

            "${toHebrewNumerals(day)} ${hebrewMonthNames.getOrElse(month) { "" }} ${toHebrewYear(year)}"
        } catch (e: Exception) {
            ""
        }
    }

    // -------------------------------------------------------------------------
    // Hebrew numeral converters (preserved from original implementation)
    // -------------------------------------------------------------------------

    /**
     * Converts a day number to Hebrew numerals using kriut format.
     * 1–9: alef–tet with geresh; 10–19: yud + unit; 20–29: kaf/yud + unit.
     */
    private fun toHebrewNumerals(day: Int): String {
        if (day <= 0) return ""

        return when {
            day == 10                       -> "י׳"
            day <  10                       -> listOf("א׳","ב׳","ג׳","ד׳","ה׳","ו׳","ז׳","ח׳","ט׳")[day - 1]
            day <  20                       -> listOf("י׳","כ׳","ל׳","מ׳","נ׳","ס׳","ע׳","פ׳","צ׳")[day - 11]
            day == 20                       -> "כ׳"
            day <  30                       -> listOf("כ׳","ל׳","מ׳","נ׳","ס׳","ע׳","פ׳","צ׳","ק׳")[day - 21]
            day <  40                       -> listOf("ל׳","מ׳","נ׳","ס׳","ע׳","פ׳","צ׳","ק׳","ר׳")[day - 31]
            day <= 50 && day % 10 != 0       -> {
                val ones = day % 10
                val tens = (day / 10) * 10
                val tensStr = when (tens) {
                    40 -> "מ׳"; 50 -> "נ׳"; 60 -> "ס׳"; 70 -> "ע׳"; 80 -> "פ׳"; 90 -> "צ׳"
                    else -> ""
                }
                tensStr + listOf("א׳","ב׳","ג׳","ד׳","ה׳","ו׳","ז׳","ח׳","ט׳")[ones - 1]
            }
            else -> {
                val ones = day % 10
                val tens = (day / 10) * 10
                val tensStr = when (tens) {
                    50 -> "נ׳"; 60 -> "ס׳"; 70 -> "ע׳"; 80 -> "פ׳"; 90 -> "צ׳"
                    else -> ""
                }
                if (ones in 1..9) {
                    tensStr + listOf("א׳","ב׳","ג׳","ד׳","ה׳","ו׳","ז׳","ח׳","ט׳")[ones - 1]
                } else tensStr
            }
        }
    }

    /**
     * Converts a Hebrew year to Hebrew numerals.
     * e.g. 5786 → "תשפ״ו"
     */
    private fun toHebrewYear(year: Int): String {
        val thousands = year / 1000
        val remainder = year % 1000

        var result = when (thousands) {
            5 -> "ה׳"; 4 -> "ד׳"; 3 -> "ג׳"; 2 -> "ב׳"; 1 -> "א׳"
            else -> ""
        }

        // Hundreds (100–900), using tav forms for 400+
        result += when (remainder / 100) {
            9 -> "ת״ת"; 8 -> "ת״ש"; 7 -> "ת״ז"; 6 -> "ת״ו"; 5 -> "ת״ה"
            4 -> "ת״ד"; 3 -> "ת״ג"; 2 -> "ת״ב"; 1 -> "ת״"
            else -> ""
        }

        val tensAndOnes = remainder % 100
        // 15 and 16 have special书写 (not י״ה / ו״ה)
        if (tensAndOnes == 15) return result + "ט״ו"
        if (tensAndOnes == 16) return result + "ט״ז"

        val tens = tensAndOnes / 10
        val ones = tensAndOnes % 10

        result += when (tens) {
            9 -> "צ״ט"; 8 -> "פ״ט"; 7 -> "ע״ט"; 6 -> "ס״ט"; 5 -> "נ״ה"
            4 -> "מ״ה"; 3 -> "ל״ה"; 2 -> "כ״ה"; 1 -> "י״ה"
            else -> ""
        }

        if (ones != 0) {
            result += listOf("א׳","ב׳","ג׳","ד׳","ה׳","ו׳","ז׳","ח׳","ט׳")[ones - 1]
        }
        return result
    }
}
