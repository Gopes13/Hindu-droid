package dev.gopes.hinducalendar.domain.model

enum class Yoga(val number: Int, val displayName: String) {
    VISHKAMBHA(1, "Vishkambha"),
    PRITI(2, "Priti"),
    AYUSHMAN(3, "Ayushman"),
    SAUBHAGYA(4, "Saubhagya"),
    SHOBHANA(5, "Shobhana"),
    ATIGANDA(6, "Atiganda"),
    SUKARMA(7, "Sukarma"),
    DHRITI(8, "Dhriti"),
    SHULA(9, "Shula"),
    GANDA(10, "Ganda"),
    VRIDDHI(11, "Vriddhi"),
    DHRUVA(12, "Dhruva"),
    VYAGHATA(13, "Vyaghata"),
    HARSHANA(14, "Harshana"),
    VAJRA(15, "Vajra"),
    SIDDHI(16, "Siddhi"),
    VYATIPATA(17, "Vyatipata"),
    VARIYAN(18, "Variyan"),
    PARIGHA(19, "Parigha"),
    SHIVA(20, "Shiva"),
    SIDDHA(21, "Siddha"),
    SADHYA(22, "Sadhya"),
    SHUBHA(23, "Shubha"),
    SHUKLA(24, "Shukla"),
    BRAHMA(25, "Brahma"),
    INDRA(26, "Indra"),
    VAIDHRITI(27, "Vaidhriti");

    companion object {
        const val SPAN_DEGREES = 360.0 / 27.0

        fun from(sunSidereal: Double, moonSidereal: Double): Yoga {
            val sum = ((sunSidereal + moonSidereal) % 360.0 + 360.0) % 360.0
            val index = (sum / SPAN_DEGREES).toInt()
            return entries.getOrElse(index.coerceIn(0, 26)) { VISHKAMBHA }
        }
    }
}
