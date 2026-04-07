package com.dafyomi.pro.domain

import java.time.LocalDate

/**
 * Utility for formatting Hebrew (Jewish) calendar dates.
 * Uses Hebrew Unicode characters for month names and numerals.
 */
object HebrewDateFormatter {

    // Hebrew month names in Hebrew Unicode
    private val hebrewMonths = mapOf(
        1 to "ניסן",     // Nisan
        2 to "אייר",     // Iyar
        3 to "סיון",     // Sivan
        4 to "תמוז",     // Tammuz
        5 to "אב",       // Av
        6 to "אלול",     // Elul
        7 to "תשרי",     // Tishrei
        8 to "מרחשוון",  // Cheshvan
        9 to "כסלו",     // Kislev
        10 to "טבת",     // Tevet
        11 to "שבט",     // Shevat
        12 to "אדר א׳",  // Adar I (leap year)
        13 to "אדר ב׳"   // Adar II (leap year)
    )

    // Fallback month names for non-leap years
    private val hebrewMonthsNormal = mapOf(
        1 to "ניסן",
        2 to "אייר",
        3 to "סיון",
        4 to "תמוז",
        5 to "אב",
        6 to "אלול",
        7 to "תשרי",
        8 to "מרחשוון",
        9 to "כסלו",
        10 to "טבת",
        11 to "שבט",
        12 to "אדר"
    )

    /**
     * Formats a LocalDate as a Hebrew date string.
     * Returns format like "כ״ה ניסן ה׳תשפ״ה"
     */
    fun formatHebrewDate(date: LocalDate): String {
        return try {
            val hebrewDate = calculateHebrewDate(date)
            val day = hebrewDate.dayOfMonth
            val month = hebrewDate.month
            val year = hebrewDate.year
            val isLeapYear = hebrewDate.isLeapYear

            val dayHebrew = toHebrewNumerals(day)
            val monthHebrew = if (isLeapYear && month == 12) {
                hebrewMonths[13] ?: "אדר ב׳"
            } else {
                if (isLeapYear && month > 6) {
                    hebrewMonths[month + 1] ?: hebrewMonthsNormal[month] ?: ""
                } else {
                    hebrewMonthsNormal[month] ?: ""
                }
            }
            val yearHebrew = toHebrewYear(year)

            "$dayHebrew $monthHebrew $yearHebrew"
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Calculates the Hebrew date components for a given Gregorian date.
     */
    private fun calculateHebrewDate(date: LocalDate): HebrewDate {
        // Jewish calendar starts in 1970 at 5730
        val baseYear = 5730
        val baseGregorianYear = 1970

        var year = baseYear + (date.year - baseGregorianYear)
        val isLeapYear = isJewishLeapYear(year)

        // Calculate day of year (1-365/366)
        val dayOfYear = date.dayOfYear

        // Hebrew months have different lengths - approximated
        // Rosh Hashanah starts around September-October
        val approxHebrewMonth = when {
            dayOfYear < 60 -> 7   // Tishrei
            dayOfYear < 120 -> 8   // Cheshvan
            dayOfYear < 150 -> 9   // Kislev
            dayOfYear < 180 -> 10  // Tevet
            dayOfYear < 210 -> 11  // Shevat
            dayOfYear < 240 -> if (isLeapYear) 12 else 1  // Adar or Nisan
            dayOfYear < 270 -> if (isLeapYear) 13 else 2  // Adar II or Iyar
            dayOfYear < 300 -> 3   // Sivan
            dayOfYear < 330 -> 4    // Tammuz
            dayOfYear < 360 -> 5    // Av
            else -> 6              // Elul
        }

        // Simplified day calculation
        var dayOfMonth = (dayOfYear - 30) % 30 + 1
        if (dayOfMonth < 1) dayOfMonth = dayOfYear - 60 + 1
        if (dayOfMonth < 1) dayOfMonth = dayOfYear

        // Adjust for months that start differently
        when (approxHebrewMonth) {
            7 -> dayOfMonth = dayOfYear - 60 + 7  // Tishrei starts around day 60-70
            8 -> dayOfMonth = dayOfYear - 120 + 1
            9 -> dayOfMonth = dayOfYear - 150 + 1
            10 -> dayOfMonth = dayOfYear - 180 + 1
        }

        return HebrewDate(year, approxHebrewMonth, dayOfMonth, isLeapYear)
    }

    /**
     * Determines if the Hebrew year is a leap year.
     * Hebrew leap years occur in years 3, 6, 8, 11, 14, 17, 19 of a 19-year cycle.
     */
    private fun isJewishLeapYear(year: Int): Boolean {
        val cycleYear = year % 19
        return cycleYear in listOf(3, 6, 8, 11, 14, 17, 19)
    }

    /**
     * Converts a day number to Hebrew numerals using kriut format.
     * 1-9 use alef-yud, 10-19 use yud+units, 20-29 use kaf-yud + units, etc.
     */
    private fun toHebrewNumerals(day: Int): String {
        if (day <= 0) return ""

        return when {
            day == 10 -> "י׳"
            day < 10 -> {
                val letters = listOf("א׳", "ב׳", "ג׳", "ד׳", "ה׳", "ו׳", "ז׳", "ח׳", "ט׳")
                letters[day - 1]
            }
            day < 20 -> {
                val ones = day - 10
                val letters = listOf("י׳", "כ׳", "ל׳", "מ׳", "נ׳", "ס׳", "ע׳", "פ׳", "צ׳")
                letters[ones - 1]
            }
            day == 20 -> "כ׳"
            day < 30 -> {
                val ones = day - 20
                val letters = listOf("כ׳", "ל׳", "מ׳", "נ׳", "ס׳", "ע׳", "פ׳", "צ׳", "ק׳")
                letters[ones - 1]
            }
            day < 40 -> {
                val ones = day - 30
                val letters = listOf("ל׳", "מ׳", "נ׳", "ס׳", "ע׳", "פ׳", "צ׳", "ק׳", "ר׳")
                letters[ones - 1]
            }
            day <= 50 -> {
                val ones = day - 40
                val letters = listOf("מ׳", "נ׳", "ס׳", "ע׳", "פ׳", "צ׳", "ק׳", "ר׳", "ש׳")
                if (ones in 1..9) letters[ones - 1] else ""
            }
            else -> {
                val ones = day % 10
                val tens = (day / 10) * 10
                val tensStr = when (tens) {
                    50 -> "נ׳"
                    60 -> "ס׳"
                    70 -> "ע׳"
                    80 -> "פ׳"
                    90 -> "צ׳"
                    else -> ""
                }
                if (ones in 1..9) {
                    val onesList = listOf("א׳", "ב׳", "ג׳", "ד׳", "ה׳", "ו׳", "ז׳", "ח׳", "ט׳")
                    tensStr + onesList[ones - 1]
                } else tensStr
            }
        }
    }

    /**
     * Converts a Hebrew year to Hebrew numerals.
     * 5785 = תשפ״ה
     */
    private fun toHebrewYear(year: Int): String {
        val thousands = year / 1000
        val remainder = year % 1000

        var result = ""
        if (thousands > 0) {
            result += when (thousands) {
                5 -> "ה׳"
                4 -> "ד׳"
                3 -> "ג׳"
                2 -> "ב׳"
                1 -> "א׳"
                else -> ""
            }
        }

        // Hundreds (100-900) - uses special final forms for 400+
        val hundreds = remainder / 100
        result += when (hundreds) {
            9 -> "ת״ת"
            8 -> "ת״ש"
            7 -> "ת״ז"
            6 -> "ת״ו"
            5 -> "ת״ה"
            4 -> "ת״ד"
            3 -> "ת״ג"
            2 -> "ת״ב"
            1 -> "ת״"
            else -> ""
        }

        val tensAndOnes = remainder % 100

        // Special cases for 15 and 16 (not written as י״ה or ו״ה)
        if (tensAndOnes == 15) return result + "ט״ו"
        if (tensAndOnes == 16) return result + "ט״ז"

        val tens = tensAndOnes / 10
        val ones = tensAndOnes % 10

        // Tens
        val tensStr = when (tens) {
            9 -> "צ״ט"
            8 -> "פ״ט"
            7 -> "ע״ט"
            6 -> "ס״ט"
            5 -> "נ״ה"
            4 -> "מ״ה"
            3 -> "ל״ה"
            2 -> "כ״ה"
            1 -> "י״ה"
            else -> ""
        }

        val resultWithTens = if (tens == 0) result else result + tensStr

        if (ones == 0) return resultWithTens

        val onesList = listOf("א׳", "ב׳", "ג׳", "ד׳", "ה׳", "ו׳", "ז׳", "ח׳", "ט׳")
        return resultWithTens + onesList[ones - 1]
    }

    private data class HebrewDate(
        val year: Int,
        val month: Int,
        val dayOfMonth: Int,
        val isLeapYear: Boolean
    )
}
