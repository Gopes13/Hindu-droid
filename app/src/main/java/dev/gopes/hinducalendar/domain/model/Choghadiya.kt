package dev.gopes.hinducalendar.domain.model

import java.time.LocalDateTime

enum class ChoghadiyaAuspiciousness {
    BEST,      // Amrit
    GOOD,      // Shubh
    GAIN,      // Labh
    NEUTRAL,   // Char (OK for travel)
    BAD        // Rog, Kaal, Udvegh
}

enum class ChoghadiyaType(
    val displayName: String,
    val hindiName: String,
    val auspiciousness: ChoghadiyaAuspiciousness
) {
    UDVEGH("Udvegh", "\u0909\u0926\u094D\u0935\u0947\u0917", ChoghadiyaAuspiciousness.BAD),
    CHAR("Char", "\u091A\u0930", ChoghadiyaAuspiciousness.NEUTRAL),
    LABH("Labh", "\u0932\u093E\u092D", ChoghadiyaAuspiciousness.GAIN),
    AMRIT("Amrit", "\u0905\u092E\u0943\u0924", ChoghadiyaAuspiciousness.BEST),
    KAAL("Kaal", "\u0915\u093E\u0932", ChoghadiyaAuspiciousness.BAD),
    SHUBH("Shubh", "\u0936\u0941\u092D", ChoghadiyaAuspiciousness.GOOD),
    ROG("Rog", "\u0930\u094B\u0917", ChoghadiyaAuspiciousness.BAD);

    companion object {
        // Cycling order for reference: Udvegh(0), Char(1), Labh(2), Amrit(3), Kaal(4), Shubh(5), Rog(6)
        private val CYCLE = entries.toList()

        // Day starting indices by weekday (1=Sunday...7=Saturday)
        val DAY_START = mapOf(
            1 to 0,  // Sun = Udvegh
            2 to 3,  // Mon = Amrit
            3 to 6,  // Tue = Rog
            4 to 2,  // Wed = Labh
            5 to 5,  // Thu = Shubh
            6 to 1,  // Fri = Char
            7 to 4   // Sat = Kaal
        )

        // Night starting indices by weekday (1=Sunday...7=Saturday)
        val NIGHT_START = mapOf(
            1 to 5,  // Sun = Shubh
            2 to 1,  // Mon = Char
            3 to 4,  // Tue = Kaal
            4 to 0,  // Wed = Udvegh
            5 to 3,  // Thu = Amrit
            6 to 6,  // Fri = Rog
            7 to 2   // Sat = Labh
        )

        /** Get the choghadiya type at a given position (0-7) for day/night of a weekday. */
        fun atPosition(weekday: Int, position: Int, isDay: Boolean): ChoghadiyaType {
            val startIndex = if (isDay) DAY_START[weekday]!! else NIGHT_START[weekday]!!
            return CYCLE[(startIndex + position) % 7]
        }
    }
}

data class ChoghadiyaPeriod(
    val type: ChoghadiyaType,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val isDay: Boolean,
    val isCurrent: Boolean = false
)
