package dev.gopes.hinducalendar.domain.model

import java.time.LocalDateTime

enum class HoraAuspiciousness {
    BENEFIC,    // Jupiter, Venus, Mercury, Moon
    MALEFIC,    // Mars, Saturn
    ROYAL       // Sun (situational)
}

enum class HoraPlanet(
    val displayName: String,
    val hindiName: String,
    val symbol: String,
    val auspiciousness: HoraAuspiciousness
) {
    SUN("Sun", "\u0938\u0942\u0930\u094D\u092F", "\u2609", HoraAuspiciousness.ROYAL),
    MOON("Moon", "\u091A\u0928\u094D\u0926\u094D\u0930", "\u263D", HoraAuspiciousness.BENEFIC),
    MARS("Mars", "\u092E\u0902\u0917\u0932", "\u2642", HoraAuspiciousness.MALEFIC),
    MERCURY("Mercury", "\u092C\u0941\u0927", "\u263F", HoraAuspiciousness.BENEFIC),
    JUPITER("Jupiter", "\u092C\u0943\u0939\u0938\u094D\u092A\u0924\u093F", "\u2643", HoraAuspiciousness.BENEFIC),
    VENUS("Venus", "\u0936\u0941\u0915\u094D\u0930", "\u2640", HoraAuspiciousness.BENEFIC),
    SATURN("Saturn", "\u0936\u0928\u093F", "\u2644", HoraAuspiciousness.MALEFIC);

    companion object {
        // Chaldean order (descending orbital period)
        val CHALDEAN_ORDER = listOf(SATURN, JUPITER, MARS, SUN, VENUS, MERCURY, MOON)

        // Day ruler by weekday (1=Sunday...7=Saturday)
        val DAY_RULER = mapOf(
            1 to SUN,
            2 to MOON,
            3 to MARS,
            4 to MERCURY,
            5 to JUPITER,
            6 to VENUS,
            7 to SATURN
        )

        /** Get the hora planet for a given hora index (0-23) on a given weekday. */
        fun atHoraIndex(weekday: Int, horaIndex: Int): HoraPlanet {
            val ruler = DAY_RULER[weekday]!!
            val startIdx = CHALDEAN_ORDER.indexOf(ruler)
            return CHALDEAN_ORDER[(startIdx + horaIndex) % 7]
        }
    }
}

data class HoraPeriod(
    val planet: HoraPlanet,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val horaNumber: Int,
    val isDay: Boolean,
    val isCurrent: Boolean = false
)
