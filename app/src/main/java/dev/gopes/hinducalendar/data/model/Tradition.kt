package dev.gopes.hinducalendar.data.model

enum class CalendarTradition(
    val displayName: String,
    val description: String,
    val monthSystem: MonthSystem
) {
    PURNIMANT(
        "North Indian (Purnimant)",
        "Month ends on Purnima (full moon). Used in most of North India, including UP, MP, Rajasthan, Bihar.",
        MonthSystem.PURNIMANT
    ),
    AMANT(
        "South Indian (Amant)",
        "Month ends on Amavasya (new moon). Used in Maharashtra, Karnataka, Andhra Pradesh, Telangana.",
        MonthSystem.AMANT
    ),
    GUJARATI(
        "Gujarati",
        "Based on Amant system with Gujarati month names and regional festivals like Uttarayan.",
        MonthSystem.AMANT
    ),
    BENGALI(
        "Bengali",
        "Bengali calendar (Bangabda) with regional festivals like Durga Puja and Poila Boishakh.",
        MonthSystem.PURNIMANT
    ),
    TAMIL(
        "Tamil",
        "Tamil calendar (Thiruvalluvar) with festivals like Pongal and Tamil New Year.",
        MonthSystem.AMANT
    ),
    MALAYALAM(
        "Malayalam",
        "Malayalam calendar (Kollavarsham) with festivals like Onam and Vishu.",
        MonthSystem.AMANT
    );

    val key: String get() = name.lowercase()
}

enum class MonthSystem {
    PURNIMANT,  // month ends on Purnima
    AMANT       // month ends on Amavasya
}
