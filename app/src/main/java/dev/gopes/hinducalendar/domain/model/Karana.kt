package dev.gopes.hinducalendar.domain.model

enum class Karana(val displayName: String, val isAuspicious: Boolean) {
    KIMSTUGHNA("Kimstughna", true),
    SHAKUNI("Shakuni", false),
    CHATUSHPADA("Chatushpada", false),
    NAGAVA("Nagava", false),
    BAVA("Bava", true),
    BALAVA("Balava", true),
    KAULAVA("Kaulava", true),
    TAITILA("Taitila", true),
    GARAJA("Garaja", true),
    VANIJA("Vanija", true),
    VISHTI("Vishti (Bhadra)", false);

    companion object {
        private val RECURRING = listOf(BAVA, BALAVA, KAULAVA, TAITILA, GARAJA, VANIJA, VISHTI)

        fun from(tithiNumber: Int, isFirstHalf: Boolean): Karana {
            val karanaIndex = (tithiNumber - 1) * 2 + if (isFirstHalf) 0 else 1

            return when (karanaIndex) {
                0 -> KIMSTUGHNA
                57 -> SHAKUNI
                58 -> CHATUSHPADA
                59 -> NAGAVA
                else -> RECURRING[(karanaIndex - 1) % 7]
            }
        }
    }
}
