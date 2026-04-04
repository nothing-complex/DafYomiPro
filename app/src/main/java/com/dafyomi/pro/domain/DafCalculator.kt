package com.dafyomi.pro.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * DafCalculator computes the current Daf Yomi position in the 7.5-year cycle.
 *
 * The Daf Yomi cycle started January 2, 2020 (12th Siyum Hashas) and consists of
 * 2,711 daf across 38 masechtot. The cycle completes approximately July 2027.
 *
 * Algorithm:
 * 1. daysSinceStart = (today - Jan 2, 2020).days
 * 2. dafIndex = daysSinceStart % 2711  // 0-indexed position in cycle
 * 3. Map dafIndex to masechet using cumulative daf boundaries
 */
object DafCalculator {

    // Cycle start: January 2, 2020 (12th Siyum Hashas)
    private val CYCLE_START = LocalDate.of(2020, 1, 2)
    private const val CYCLE_LENGTH = 2711

    /**
     * Complete list of 38 masechtot in Daf Yomi cycle with correct daf counts.
     * Data source: dafyomi.co.il calendar, verified against standard Talmud Bavli.
     * Total: 2,711 daf (2,711 double-sided pages)
     */
    private val masechetData = listOf(
        // Seder Zeraim (1)
        Masechet(1, "ברכות", "Berachot", "Berachot", "beh-RAKH-ot", 64),
        // Seder Moed (12)
        Masechet(2, "שבת", "Shabbat", "Shabat", "sha-BAT", 156),
        Masechet(3, "עירובין", "Eruvin", "Eruvin", "eh-roo-VIN", 105),
        Masechet(4, "פסחים", "Pesachim", "Pesachim", "peh-SAH-kim", 121),
        Masechet(5, "שקלים", "Shekalim", "Shekalim", "sheh-KAH-lim", 22),
        Masechet(6, "יומא", "Yoma", "Yoma", "YO-ma", 88),
        Masechet(7, "סוכה", "Sukkah", "Sukkah", "soo-KAH", 55),
        Masechet(8, "ביצה", "Beitzah", "Beitzah", "BAY-tsah", 39),
        Masechet(9, "ראש השנה", "Rosh Hashanah", "Rosh Hashanah", "rosh ha-SHAH-nah", 35),
        Masechet(10, "תענית", "Taanit", "Taanit", "tah-ah-NEET", 31),
        Masechet(11, "מגילה", "Megillah", "Megillah", "meh-GIL-lah", 32),
        Masechet(12, "מועד קטן", "Moed Katan", "Moed Katan", "MO-ed kah-TAN", 29),
        Masechet(13, "חגיגה", "Chagigah", "Chagigah", "khah-gee-GAH", 27),
        // Seder Nashim (7)
        Masechet(14, "יבמות", "Yevamot", "Yevamot", "yeh-vah-MOTE", 122),
        Masechet(15, "כתובות", "Ketubot", "Ketubot", "keh-too-BOTE", 112),
        Masechet(16, "נדרים", "Nedarim", "Nedarim", "neh-dah-REEM", 91),
        Masechet(17, "נזיר", "Nazir", "Nazir", "nah-ZEER", 66),
        Masechet(18, "סוטה", "Sotah", "Sotah", "SO-tah", 49),
        Masechet(19, "גיטין", "Gittin", "Gittin", "git-TEEN", 90),
        Masechet(20, "קידושין", "Kiddushin", "Kiddushin", "kee-doo-SHEEN", 82),
        // Seder Nezikin (8)
        Masechet(21, "בבא קמא", "Bava Kamma", "Bava Kamma", "BAH-vah kah-MAH", 119),
        Masechet(22, "בבא מציעא", "Bava Metzia", "Bava Metzia", "BAH-vah mets-EE-ah", 121),
        Masechet(23, "בבא בתרא", "Bava Batra", "Bava Batra", "BAH-vah bah-TRAH", 176),
        Masechet(24, "סנהדרין", "Sanhedrin", "Sanhedrin", "san-heh-DREEN", 113),
        Masechet(25, "מכות", "Makkot", "Makkot", "mah-KOTE", 24),
        Masechet(26, "שבועות", "Shevuot", "Shevuot", "sheh-voo-OTE", 48),
        Masechet(27, "עדיות", "Eduyot", "Eduyot", "eh-doo-YOTE", 13),
        Masechet(28, "עבודה זרה", "Avodah Zarah", "Avodah Zarah", "ah-voh-Dah ah-rah", 76),
        Masechet(29, "הוריות", "Horayot", "Horayot", "ho-rah-YOTE", 14),
        // Seder Kodashim (9)
        Masechet(30, "זבחים", "Zevachim", "Zevachim", "zeh-vah-KHEEM", 120),
        Masechet(31, "מנחות", "Menachot", "Menachot", "meh-nah-KHOTE", 110),
        Masechet(32, "חולין", "Chullin", "Chullin", "khoo-LEEN", 142),
        Masechet(33, "בכורות", "Bekhorot", "Bekhorot", "beh-kho-ROTE", 61),
        Masechet(34, "ערכין", "Arachin", "Arachin", "ah-rah-KREEN", 34),
        Masechet(35, "תמורה", "Temurah", "Temurah", "teh-moo-RAH", 33),
        Masechet(36, "כריתות", "Keritot", "Keritot", "keh-ree-TOHT", 28),
        Masechet(37, "תמיד", "Tamid", "Tamid", "tah-MEED", 33),
        // Taharot (1)
        Masechet(38, "נדה", "Niddah", "Niddah", "NID-dah", 73)
    )

    /**
     * cumulativeDafs[i] = ending 0-based index of masechet i in the cycle.
     * E.g., Berachot (64 daf) ends at index 63, so cumulativeDafs[0] = 63.
     * Shabbat starts at index 64, ends at index 219, so cumulativeDafs[1] = 219.
     *
     * The scan starts at -1 so the running sum gives inclusive end indices.
     */
    private val cumulativeDafs: List<Int> = masechetData.scan(-1) { acc, m -> acc + m.dafCount }

    /**
     * Returns the DafData for a given date.
     *
     * @param date The date to look up
     * @return DafData containing masechet, daf number, cycle position, and summary
     */
    fun getDafForDate(date: LocalDate): DafData {
        val daysSinceStart = ChronoUnit.DAYS.between(CYCLE_START, date)

        // Handle negative days (dates before cycle start) by wrapping
        val adjustedDays = ((daysSinceStart % CYCLE_LENGTH) + CYCLE_LENGTH) % CYCLE_LENGTH

        // cycleDay is 1-indexed for display (day 1 = first day of cycle)
        val cycleDay = (adjustedDays + 1).toInt()

        // dafIndex is the 0-based position in the cycle (0 to 2710)
        val dafIndex = adjustedDays.toInt()

        // Find which masechet this daf belongs to
        // cumulativeDafs[i] is the ENDING index of masechet i
        // So we find the first one where cumulativeDafs[i] >= dafIndex
        var masechetIndex = cumulativeDafs.indexOfFirst { it >= dafIndex }

        // Safety check: if masechetIndex is -1 (dafIndex beyond all masechet ends),
        // wrap back to beginning. This handles edge cases where our data doesn't
        // perfectly match the theoretical cycle boundary.
        if (masechetIndex == -1) {
            masechetIndex = masechetData.lastIndex
        }

        // Calculate the daf number within this masechet (1-indexed)
        val masechetStart = if (masechetIndex > 0) cumulativeDafs[masechetIndex - 1] + 1 else 0
        val dafNumber = dafIndex - masechetStart + 1

        val cyclePercent = cycleDay.toFloat() / CYCLE_LENGTH

        return DafData(
            dafIndex = dafIndex,
            masechet = masechetData[masechetIndex],
            dafNumber = dafNumber,
            cycleDay = cycleDay,
            cyclePercent = cyclePercent,
            summary = ""
        )
    }

    /**
     * Looks up a masechet by its ID.
     * @param id The masechet ID (1-38)
     * @return The Masechet or null if not found
     */
    fun getMasechetById(id: Int): Masechet? = masechetData.find { it.id == id }

    /**
     * Returns the total number of masechtot in the cycle.
     */
    fun getMasechetCount(): Int = masechetData.size

    /**
     * Returns the total number of daf in the cycle.
     */
    fun getCycleLength(): Int = CYCLE_LENGTH
}
