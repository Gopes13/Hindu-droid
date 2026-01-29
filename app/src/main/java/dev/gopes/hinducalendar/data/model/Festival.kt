package dev.gopes.hinducalendar.data.model

import java.time.LocalDate

data class Festival(
    val id: String,
    val names: LocalizedName,
    val description: LocalizedName,
    val rule: FestivalRule,
    val traditions: List<String>,
    val category: FestivalCategory,
    val durationDays: Int = 1
) {
    val displayName: String get() = names.en
    val hindiDisplayName: String get() = names.hi ?: names.en
}

data class LocalizedName(
    val en: String,
    val hi: String? = null,
    val sa: String? = null
)

data class FestivalRule(
    val type: String,           // "tithi", "solar", "tithi_offset"
    val month: String? = null,
    val paksha: String? = null,
    val tithi: Int? = null,
    val solarEvent: String? = null,
    val daysAfter: Int? = null,
    val daysBefore: Int? = null
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
