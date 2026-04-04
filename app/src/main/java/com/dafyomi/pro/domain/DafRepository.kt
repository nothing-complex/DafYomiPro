package com.dafyomi.pro.domain

import java.time.LocalDate

/**
 * Repository for fetching Daf Yomi data and generating summaries.
 *
 * Summary generation strategy:
 * - For masechtot with detailed summaries (4 currently), uses curated content
 * - For other masechtot, generates a placeholder based on known tractate themes
 * - Future: Integrate Sefaria API for real Hebrew text → AI summary generation
 */
class DafRepository {

    private val summaryCache = mutableMapOf<String, String>()

    /**
     * Gets today's Daf Yomi data with summary.
     */
    fun getTodaysDaf(): DafData {
        val today = LocalDate.now()
        val daf = DafCalculator.getDafForDate(today)

        val summary = getSummaryForDaf(daf.masechet, daf.dafNumber)

        return daf.copy(summary = summary)
    }

    /**
     * Gets summary for a specific daf, using cache when available.
     */
    private fun getSummaryForDaf(masechet: Masechet, dafNumber: Int): String {
        val key = "${masechet.english}$dafNumber"

        return summaryCache.getOrPut(key) {
            generateSummary(masechet, dafNumber)
        }
    }

    /**
     * Generates summary for a daf.
     * First checks if we have detailed summaries for this masechet,
     * otherwise falls back to masechet-specific placeholder.
     */
    private fun generateSummary(masechet: Masechet, dafNumber: Int): String {
        // Check if we have detailed summaries for this masechet
        val detailedSummaries = summaries[masechet.id]
        if (detailedSummaries != null && detailedSummaries.isNotEmpty()) {
            // Map daf to one of 4 quarter sections (0-3)
            val quarterIndex = (dafNumber - 1) / maxOf(1, masechet.dafCount / 4)
            val index = quarterIndex.coerceIn(0, detailedSummaries.lastIndex)
            return detailedSummaries[index]
        }

        // Fall back to masechet-specific placeholder
        return defaultSummary(masechet)
    }

    /**
     * Generates a masechet-specific placeholder summary based on
     * known themes and topics of each tractate.
     */
    private fun defaultSummary(masechet: Masechet): String {
        val theme = masechetThemes[masechet.id]
            ?: "Jewish law and rabbinic tradition"

        val topic = masechetTopics[masechet.id]
            ?: "the intricate details of ${masechet.english}"

        return "Today's daf in ${masechet.english} explores $topic. $theme."
    }

    companion object {
        /**
         * Detailed summaries for select masechtot.
         * Each masechet has 4 summaries covering different quarters of the tractate.
         * Data source: Curated content based on Talmudic topics.
         */
        private val summaries = mapOf(
            // Berachot - Laws of blessings and prayers
            1 to listOf(
                "Berachot introduces the laws of blessings, examining when and how we acknowledge God's role in our daily lives through prayer and gratitude.",
                "The Talmud explores the morning blessings and the proper mindset required for prayer, establishing the framework for all subsequent mitzvot.",
                "This section discusses the obligation to recite blessings before performing commandments and enjoying worldly pleasures.",
                "The sages debate the precise moments when blessings must be recited and who is exempt from this obligation."
            ),
            // Shabbat - Sabbath laws and the 39 melachot
            2 to listOf(
                "Shabbat examines the thirty-nine prohibited activities on the Sabbath, tracing their origins to the Mishkan construction.",
                "The discussion centers on defining creative work and the boundaries of permissible activity during Shabbat.",
                "This daf addresses carrying in public domains and the complexities of the eruv Chatzeirot.",
                "The Talmud explores the intersection of Shabbat observance and life-saving measures."
            ),
            // Zevachim - Sacrificial offerings
            30 to listOf(
                "Zevachim begins the Order of Kodashim, examining the laws of sacrificial offerings and their spiritual significance.",
                "This section details the procedures for handling sacrifices that have become impure or have been left beyond their designated time.",
                "The Talmud discusses the nuanced laws governing the placement of sacrificial portions on the altar.",
                "This daf explores the complex rules surrounding piggul, meat sacrificed with improper intention."
            ),
            // Menachot - Flour offerings
            31 to listOf(
                "Menachot examines the flour offerings brought by Kohanim and individuals, detailing their preparation and presentation.",
                "The Talmud discusses the showbread and the menorah, exploring how sacred objects fulfill their spiritual purpose.",
                "This section addresses the offering of flour by those unable to bring animal sacrifices.",
                "The sages debate the proper mixing of oil and the requirements for waved offerings."
            ),
            // Yevamot - Levirate marriage
            14 to listOf(
                "Yevamot deals with the laws of levirate marriage, exploring when a brother must marry his deceased brother's widow.",
                "This section examines the role of the yavam (brother-in-law) and the process of chalitzah (release).",
                "The Talmud discusses various scenarios including cases of doubt, converts, and disabled individuals.",
                "This daf explores the spiritual connection between the living and the deceased through this mitzvah."
            ),
            // Ketubot - Marriage contracts
            15 to listOf(
                "Ketubot outlines the marriage contract and the financial obligations between husband and wife.",
                "This section discusses the various components of the ketubah and their significance.",
                "The Talmud examines cases involving wife's refusal to work and husband's duties.",
                "This daf explores the balance of rights and responsibilities in marital relationships."
            ),
            // Nedarim - Vows
            16 to listOf(
                "Nedarim explores the laws of vows, examining how spoken commitments become binding obligations.",
                "This section discusses the power of speech to create religious duties.",
                "The Talmud analyzes the process of nullifying vows and the role of the Beit Din.",
                "This daf examines various types of vows and their dissolution."
            ),
            // Gittin - Divorce
            19 to listOf(
                "Gittin establishes the requirements for a valid bill of divorce (get) and the process of dissolution of marriage.",
                "This section discusses the proper wording and witnesses required for a get.",
                "The Talmud examines difficult situations including conditional gett and hostage gett.",
                "This daf explores the gravity of marital dissolution in Jewish law."
            )
        )

        /**
         * Known themes for each masechet, used to generate placeholder summaries.
         */
        private val masechetThemes = mapOf(
            // Seder Zeraim
            1 to "It establishes the foundation for all prayer and mitzvah observance in Jewish life.",
            2 to "It defines the boundary between sacred and ordinary time, teaching us to sanctify our week.",
            3 to "It explores the legal intricacies of merging courtyards and establishing Shabbat boundaries.",
            4 to "It examines the Passover sacrifice and the laws of chametz during the festival.",
            5 to "It discusses the half-shekel temple tax and its significance.",
            6 to "It addresses the Day of Atonement and the high priest's service.",
            7 to "It teaches about the festival of booths and the temporary dwelling we construct.",
            8 to "It explores the laws of the festival egg, examining the intersection of Yom Tov and eggs.",
            9 to "It discusses the New Year festival and its unique prayers and customs.",
            10 to "It examines the Day of Fasting and its laws of afflicting the soul.",
            11 to "It explores the Scroll of Esther and the Purim narrative.",
            12 to "It addresses the intermediate days of festivals.",
            13 to "It discusses the pilgrimage offerings of the three festivals.",

            // Seder Nashim
            14 to "It explores the unique command to preserve the name of the deceased brother through levirate marriage.",
            15 to "It establishes the financial framework protecting both spouses in marriage.",
            16 to "It demonstrates how our words create sacred obligations that shape our spiritual life.",
            17 to "It examines the nazir's vow of separation and the implications of such dedication.",
            18 to "It explores the suspected adulteress and the ritual designed to reveal truth.",
            19 to "It provides the legal framework for ending a marriage while maintaining dignity.",
            20 to "It discusses the sanctification of marriage through kiddushin.",

            // Seder Nezikin
            21 to "It addresses damages to person and property, establishing principles of civil law.",
            22 to "It examines cases involving lost objects and the duties of the finder.",
            23 to "It discusses property disputes and the division of inherited land.",
            24 to "It explores the high court, criminal law, and the king.",
            25 to "It examines lashes as punishment and the boundaries of judicial discretion.",
            26 to "It discusses oath-making and the consequences of false testimony.",
            27 to "It explores the teachings of the early sages preserved for posterity.",
            28 to "It addresses dealings with idolaters and the boundaries of interaction.",
            29 to "It examines the laws of rulers and mistaken decisions.",

            // Seder Kodashim
            30 to "It establishes the proper procedures for sacrificial offerings in the Temple.",
            31 to "It teaches about flour offerings and the service of the Kohanim.",
            32 to "It explores the laws of ritual slaughter and prepared foods.",
            33 to "It discusses the firstborn animals and their sacred status.",
            34 to "It examines valuation vows and the dedication of property to the Temple.",
            35 to "It explores the laws of substituting animal sacrifices.",
            36 to "It discusses the penalty of excision for severe transgressions.",
            37 to "It describes the daily tamid offering and the regular Temple service.",

            // Taharot
            38 to "It addresses the laws of ritual impurity, particularly for women in childbirth."
        )

        /**
         * Known topics for each masechet, used to generate more specific placeholders.
         */
        private val masechetTopics = mapOf(
            1 to "when and how we bless God's name in our daily lives",
            2 to "the thirty-nine melachot and their applications in modern life",
            3 to "the legal fiction of eruv that allows carrying on Shabbat",
            4 to "the intricate laws of chametz and the Passover Seder",
            5 to "the communal responsibility for the Temple service",
            6 to "the holiest day of the year and its profound significance",
            7 to "the temporary booth and our relationship with nature",
            8 to "the egg that was laid on a festival and its complex status",
            9 to "the day when God remembers all creation",
            10 to "a day of reflection and self-examination before God",
            11 to "the story of Esther and the providence in history",
            12 to "the middle ground between sacred and ordinary time",
            13 to "the joy of pilgrimage and the special offerings",

            14 to "the family continuation imperative and its spiritual dimensions",
            15 to "the contractual love that protects both partners",
            16 to "the gravity and beauty of keeping one's word",
            17 to "the nazir who chooses holy separation",
            18 to "the bitter waters that test a wife's fidelity",
            19 to "the dissolution of marriage with care and respect",
            20 to "how a woman becomes sanctified to her husband",

            21 to "the principles of restitution and personal responsibility",
            22 to "the honest treatment of lost property",
            23 to "the boundaries of property and inheritance",
            24 to "the justice system and the supervision of the community",
            25 to "the proportionality of punishment",
            26 to "the sacred nature of the oath and its power",
            27 to "the chain of tradition from Sinai",
            28 to "the boundaries of engagement with the surrounding world",
            29 to "the leadership that serves the people",

            30 to "the complex procedures of the sacrificial system",
            31 to "the elevation of mundane objects to sacred use",
            32 to "the kosher diet and the boundaries of permissible consumption",
            33 to "the sanctity of the firstborn",
            34 to "the evaluation and dedication of property to sacred purposes",
            35 to "the substitution that transfers sanctity",
            36 to "the punishment that cuts off the soul from its people",
            37 to "the constant fire that represented eternal devotion",

            38 to "the lifecycle of purity and impurity"
        )
    }
}
