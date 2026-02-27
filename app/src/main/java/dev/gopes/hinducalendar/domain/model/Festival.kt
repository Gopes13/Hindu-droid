package dev.gopes.hinducalendar.domain.model

import java.time.LocalDate

data class Festival(
    val id: String,
    val names: Map<String, String>,
    val description: Map<String, String>,
    val rule: FestivalRule,
    val traditions: List<String>,
    val category: FestivalCategory,
    val durationDays: Int = 1,
    val dharmaPath: List<String>? = null,
    val stories: Map<String, String>? = null,
    val significances: Map<String, String>? = null
) {
    /** Backward-compatible English name. */
    val displayName: String get() = names["en"] ?: ""

    fun displayName(language: AppLanguage): String = names.localized(language)
    fun descriptionText(language: AppLanguage): String = description.localized(language)
    fun story(language: AppLanguage): String = stories?.localized(language) ?: ""
    fun significance(language: AppLanguage): String = significances?.localized(language) ?: ""
}

data class FestivalRule(
    val type: String,           // "tithi", "solar", "tithi_offset", "fixed_solar"
    val month: String? = null,
    val paksha: String? = null,
    val tithi: Int? = null,
    val solarEvent: String? = null,
    val daysAfter: Int? = null,
    val daysBefore: Int? = null,
    val solarMonth: Int? = null,
    val solarDay: Int? = null
)

enum class FestivalCategory(val displayName: String) {
    MAJOR("Major Festival"),
    MODERATE("Festival"),
    RECURRING("Monthly Observance"),
    REGIONAL("Regional Festival"),
    VRAT("Vrat (Fast)")
}

data class FestivalOccurrence(
    val festival: Festival,
    val date: LocalDate,
    val endDate: LocalDate? = null
) {
    val id: String get() = "${festival.id}_$date"
}
