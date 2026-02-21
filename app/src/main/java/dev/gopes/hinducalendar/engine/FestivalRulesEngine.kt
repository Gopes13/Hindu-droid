package dev.gopes.hinducalendar.engine

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import dev.gopes.hinducalendar.data.model.*
import timber.log.Timber
import java.time.LocalDate

/**
 * Evaluates festival rules from festivals.json against computed Panchang data.
 */
class FestivalRulesEngine(private val context: Context) {

    private val festivals: List<Festival> by lazy { loadFestivals() }

    private fun loadFestivals(): List<Festival> {
        return try {
            val json = context.assets.open("festivals.json").bufferedReader().readText()
            val container = Gson().fromJson(json, FestivalContainer::class.java)
            container.festivals.map { it.toDomain() }
        } catch (e: Exception) {
            Timber.e(e, "Failed to load festivals.json from assets")
            emptyList()
        }
    }

    fun festivalsFor(
        date: LocalDate,
        hinduDate: HinduDate,
        eveningHinduDate: HinduDate,
        tradition: CalendarTradition,
        jdTT: Double
    ): List<FestivalOccurrence> {
        val results = mutableListOf<FestivalOccurrence>()

        for (festival in festivals) {
            val traditionOk = festival.category == FestivalCategory.MAJOR || tradition.key in festival.traditions
            if (!traditionOk) continue
            // Evening/night festivals check the tithi prevailing later in the day
            val matchDate = if (festival.id in EVENING_FESTIVALS) eveningHinduDate else hinduDate
            if (matchesFestival(festival, matchDate, date, jdTT, tradition)) {
                results.add(FestivalOccurrence(festival, date))
            }
        }

        // Add recurring observances
        results.addAll(recurringObservances(hinduDate, date))

        // Deduplicate: suppress generic recurring Ekadashi when a named one matched
        val hasNamedEkadashi = results.any { it.festival.rule.tithi == 11 && it.festival.category != FestivalCategory.RECURRING }
        if (hasNamedEkadashi) {
            results.removeAll { it.festival.id.startsWith("ekadashi_") && it.festival.category == FestivalCategory.RECURRING }
        }

        return results
    }

    private fun matchesFestival(festival: Festival, hinduDate: HinduDate, date: LocalDate, jdTT: Double, tradition: CalendarTradition): Boolean {
        val rule = festival.rule
        return when (rule.type) {
            "tithi" -> matchesTithiRule(rule, hinduDate, tradition)
            "solar" -> matchesSolarRule(rule, jdTT)
            "tithi_offset" -> matchesTithiOffsetRule(rule, hinduDate, tradition)
            "fixed_solar" -> matchesFixedSolarRule(rule, date)
            else -> false
        }
    }

    private fun matchesFixedSolarRule(rule: FestivalRule, date: LocalDate): Boolean {
        val solarMonth = rule.solarMonth ?: return false
        val solarDay = rule.solarDay ?: return false
        return date.monthValue == solarMonth && date.dayOfMonth == solarDay
    }

    private fun matchesTithiRule(rule: FestivalRule, hinduDate: HinduDate, tradition: CalendarTradition): Boolean {
        val ruleMonth = rule.month ?: return false
        val rulePaksha = rule.paksha ?: return false
        val ruleTithi = rule.tithi ?: return false

        var monthMatches = ruleMonth == hinduDate.month.name.lowercase()
                || ruleMonth == hinduDate.month.displayName.lowercase()

        // Festival rules use Purnimant month naming convention.
        // In Purnimant, Krishna paksha belongs to the NEXT month vs Amant.
        // e.g. Diwali is "Kartik Krishna 30" (Purnimant) but "Ashwin Krishna 30" (Amant).
        // For Amant-based traditions, also check if the next month matches the rule.
        if (!monthMatches && tradition.monthSystem == MonthSystem.AMANT
            && (rulePaksha == "krishna" || ruleTithi == 30)) {
            val nextMonth = hinduDate.month.next()
            monthMatches = ruleMonth == nextMonth.name.lowercase()
                    || ruleMonth == nextMonth.displayName.lowercase()
        }

        val pakshaMatches = rulePaksha == hinduDate.paksha.key
        val tithiMatches = when {
            ruleTithi == 15 && hinduDate.paksha == Paksha.SHUKLA -> hinduDate.tithi == Tithi.PURNIMA
            ruleTithi == 30 -> hinduDate.tithi == Tithi.AMAVASYA
            else -> hinduDate.tithi.number == ruleTithi
        }

        return monthMatches && pakshaMatches && tithiMatches
    }

    private val solarEventBoundaries = mapOf(
        "aries_ingress" to 0.0,
        "taurus_ingress" to 30.0,
        "gemini_ingress" to 60.0,
        "cancer_ingress" to 90.0,
        "leo_ingress" to 120.0,
        "virgo_ingress" to 150.0,
        "libra_ingress" to 180.0,
        "scorpio_ingress" to 210.0,
        "sagittarius_ingress" to 240.0,
        "capricorn_ingress" to 270.0,
        "aquarius_ingress" to 300.0,
        "pisces_ingress" to 330.0
    )

    private fun matchesSolarRule(rule: FestivalRule, jdTT: Double): Boolean {
        val boundary = solarEventBoundaries[rule.solarEvent] ?: return false
        val sunSidereal = AstronomyEngine.tropicalToSidereal(
            AstronomyEngine.sunTropicalLongitude(jdTT), jdTT
        )
        val diff = (sunSidereal - boundary + 360.0) % 360.0
        return diff in 0.0..1.0
    }

    private fun matchesTithiOffsetRule(rule: FestivalRule, hinduDate: HinduDate, tradition: CalendarTradition): Boolean {
        val daysAfter = rule.daysAfter ?: return false
        if (daysAfter <= 0) return false
        val ruleMonth = rule.month ?: return false

        if (rule.tithi == 15) {
            // After Purnima: daysAfter=1 → Krishna Pratipada, daysAfter=2 → Krishna Dwitiya, etc.
            val expectedTithi = Tithi.fromNumber(daysAfter)
            if (hinduDate.tithi == expectedTithi && hinduDate.paksha == Paksha.KRISHNA) {
                // The rule's month names the Purnima's month (Purnimant convention).
                // In Purnimant, Purnima is the LAST day of the month — after it, the
                // month changes. So the day after X Purnima is in month X+1.
                // We must check previousMonth to identify which Purnima preceded us.
                // In Amant, Purnima is mid-month — the following Krishna paksha stays
                // in the same month, so we check the current month directly.
                return if (tradition.monthSystem == MonthSystem.PURNIMANT) {
                    val prevMonth = hinduDate.month.previous()
                    ruleMonth == prevMonth.name.lowercase() || ruleMonth == prevMonth.displayName.lowercase()
                } else {
                    ruleMonth == hinduDate.month.name.lowercase() || ruleMonth == hinduDate.month.displayName.lowercase()
                }
            }
        }
        return false
    }

    private fun recurringObservances(hinduDate: HinduDate, date: LocalDate): List<FestivalOccurrence> {
        val results = mutableListOf<FestivalOccurrence>()

        if (hinduDate.tithi == Tithi.EKADASHI) {
            val name = "${hinduDate.paksha.displayName} Ekadashi"
            results.add(FestivalOccurrence(
                festival = Festival(
                    id = "ekadashi_${hinduDate.month.number}_${hinduDate.paksha.key}",
                    names = mapOf("en" to name, "hi" to "एकादशी"),
                    description = mapOf("en" to "Ekadashi fasting day dedicated to Lord Vishnu.", "hi" to "भगवान विष्णु को समर्पित एकादशी व्रत।"),
                    rule = FestivalRule(type = "tithi"),
                    traditions = listOf("purnimant", "amant", "gujarati", "bengali", "tamil", "malayalam"),
                    category = FestivalCategory.RECURRING
                ),
                date = date
            ))
        }

        if (hinduDate.tithi == Tithi.PURNIMA) {
            results.add(FestivalOccurrence(
                festival = Festival(
                    id = "purnima_${hinduDate.month.number}",
                    names = mapOf("en" to "${hinduDate.month.displayName} Purnima", "hi" to "${hinduDate.month.hindiName} पूर्णिमा"),
                    description = mapOf("en" to "Full moon day of ${hinduDate.month.displayName}.", "hi" to "${hinduDate.month.hindiName} माह की पूर्णिमा।"),
                    rule = FestivalRule(type = "tithi"),
                    traditions = listOf("purnimant", "amant"),
                    category = FestivalCategory.RECURRING
                ),
                date = date
            ))
        }

        if (hinduDate.tithi == Tithi.AMAVASYA) {
            results.add(FestivalOccurrence(
                festival = Festival(
                    id = "amavasya_${hinduDate.month.number}",
                    names = mapOf("en" to "${hinduDate.month.displayName} Amavasya", "hi" to "${hinduDate.month.hindiName} अमावस्या"),
                    description = mapOf("en" to "New moon day of ${hinduDate.month.displayName}.", "hi" to "${hinduDate.month.hindiName} माह की अमावस्या।"),
                    rule = FestivalRule(type = "tithi"),
                    traditions = listOf("purnimant", "amant"),
                    category = FestivalCategory.RECURRING
                ),
                date = date
            ))
        }

        if (hinduDate.tithi == Tithi.TRAYODASHI) {
            results.add(FestivalOccurrence(
                festival = Festival(
                    id = "pradosh_${hinduDate.month.number}_${hinduDate.paksha.key}",
                    names = mapOf("en" to "Pradosh Vrat", "hi" to "प्रदोष व्रत"),
                    description = mapOf("en" to "Twilight fast dedicated to Lord Shiva.", "hi" to "भगवान शिव को समर्पित प्रदोष काल का व्रत।"),
                    rule = FestivalRule(type = "tithi"),
                    traditions = listOf("purnimant", "amant"),
                    category = FestivalCategory.VRAT
                ),
                date = date
            ))
        }

        return results
    }

    companion object {
        /**
         * Festivals celebrated in the afternoon, evening, or night.
         * These use the tithi prevailing ~12 hours after sunrise rather than at sunrise.
         * This matches the traditional convention:
         *  - Diwali: Pradosh Kaal (evening)
         *  - Maha Shivaratri: Nishita Kaal (midnight)
         *  - Dussehra: Aparahna Kaal (afternoon)
         *  - Ganesh Chaturthi: Madhyahna Kaal (midday)
         */
        private val EVENING_FESTIVALS = setOf(
            "diwali",
            "diwali_jain",
            "narak_chaturdashi",
            "maha_shivaratri",
            "dussehra",
            "ganesh_chaturthi",
            "karwa_chauth"
        )
    }
}

// ==================== JSON Parsing ====================

private data class FestivalContainer(val festivals: List<FestivalJson>)

private data class FestivalJson(
    val id: String,
    val names: Map<String, String>,
    val description: Map<String, String>,
    val rule: FestivalRuleJson,
    val traditions: List<String>,
    val category: String,
    @SerializedName("durationDays") val durationDays: Int = 1,
    val dharmaPath: List<String>? = null,
    val stories: Map<String, String>? = null,
    val significances: Map<String, String>? = null
) {
    fun toDomain() = Festival(
        id = id,
        names = names,
        description = description,
        rule = FestivalRule(
            type = rule.type,
            month = (rule.month as? String),
            paksha = rule.paksha,
            tithi = rule.tithi,
            solarEvent = rule.solarEvent,
            daysAfter = rule.daysAfter,
            daysBefore = rule.daysBefore,
            solarMonth = (rule.month as? Number)?.toInt(),
            solarDay = rule.day
        ),
        traditions = traditions,
        category = when (category) {
            "major" -> FestivalCategory.MAJOR
            "moderate" -> FestivalCategory.MODERATE
            "recurring" -> FestivalCategory.RECURRING
            "regional" -> FestivalCategory.REGIONAL
            "vrat" -> FestivalCategory.VRAT
            else -> FestivalCategory.MODERATE
        },
        durationDays = durationDays,
        dharmaPath = dharmaPath,
        stories = stories,
        significances = significances
    )
}

private data class FestivalRuleJson(
    val type: String,
    val month: Any? = null,     // String for tithi rules, Number for fixed_solar
    val paksha: String? = null,
    val tithi: Int? = null,
    val solarEvent: String? = null,
    val daysAfter: Int? = null,
    val daysBefore: Int? = null,
    val day: Int? = null         // used by fixed_solar rules
)
