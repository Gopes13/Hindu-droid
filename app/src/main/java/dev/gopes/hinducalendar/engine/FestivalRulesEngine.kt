package dev.gopes.hinducalendar.engine

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import dev.gopes.hinducalendar.data.model.*
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
            emptyList()
        }
    }

    fun festivalsFor(
        date: LocalDate,
        hinduDate: HinduDate,
        tradition: CalendarTradition,
        jdTT: Double
    ): List<FestivalOccurrence> {
        val results = mutableListOf<FestivalOccurrence>()

        for (festival in festivals) {
            if (tradition.key !in festival.traditions) continue
            if (matchesFestival(festival, hinduDate, jdTT)) {
                results.add(FestivalOccurrence(festival, date))
            }
        }

        // Add recurring observances
        results.addAll(recurringObservances(hinduDate, date))

        return results
    }

    private fun matchesFestival(festival: Festival, hinduDate: HinduDate, jdTT: Double): Boolean {
        val rule = festival.rule
        return when (rule.type) {
            "tithi" -> matchesTithiRule(rule, hinduDate)
            "solar" -> matchesSolarRule(rule, jdTT)
            "tithi_offset" -> matchesTithiOffsetRule(rule, hinduDate)
            else -> false
        }
    }

    private fun matchesTithiRule(rule: FestivalRule, hinduDate: HinduDate): Boolean {
        val ruleMonth = rule.month ?: return false
        val rulePaksha = rule.paksha ?: return false
        val ruleTithi = rule.tithi ?: return false

        val monthMatches = ruleMonth == hinduDate.month.name.lowercase()
                || ruleMonth == hinduDate.month.displayName.lowercase()
        val pakshaMatches = rulePaksha == hinduDate.paksha.key
        val tithiMatches = when {
            ruleTithi == 15 && hinduDate.paksha == Paksha.SHUKLA -> hinduDate.tithi == Tithi.PURNIMA
            ruleTithi == 30 -> hinduDate.tithi == Tithi.AMAVASYA
            else -> hinduDate.tithi.number == ruleTithi
        }

        return monthMatches && pakshaMatches && tithiMatches
    }

    private fun matchesSolarRule(rule: FestivalRule, jdTT: Double): Boolean {
        if (rule.solarEvent != "capricorn_ingress") return false
        val sunSidereal = AstronomyEngine.tropicalToSidereal(
            AstronomyEngine.sunTropicalLongitude(jdTT), jdTT
        )
        return sunSidereal in 270.0..271.0
    }

    private fun matchesTithiOffsetRule(rule: FestivalRule, hinduDate: HinduDate): Boolean {
        if ((rule.daysAfter ?: 0) <= 0) return false
        // Holi: day after Phalgun Purnima → Krishna Pratipada of the same month
        if (rule.tithi == 15 && hinduDate.tithi == Tithi.PRATIPADA && hinduDate.paksha == Paksha.KRISHNA) {
            val ruleMonth = rule.month ?: return false
            return ruleMonth == hinduDate.month.name.lowercase()
                    || ruleMonth == hinduDate.month.displayName.lowercase()
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
        rule = FestivalRule(rule.type, rule.month, rule.paksha, rule.tithi, rule.solarEvent, rule.daysAfter, rule.daysBefore),
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
    val month: String? = null,
    val paksha: String? = null,
    val tithi: Int? = null,
    val solarEvent: String? = null,
    val daysAfter: Int? = null,
    val daysBefore: Int? = null
)
