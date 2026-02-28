package dev.gopes.hinducalendar.domain.model

import java.time.LocalDateTime

enum class MuhurtaAuspiciousness {
    SHUBH,       // Auspicious
    ASHUBH,      // Inauspicious
    MADHYAM,     // Neutral/Mixed
    ATHI_SHUBH   // Highly auspicious (Abhijit)
}

enum class Muhurta(
    val number: Int,
    val displayName: String,
    val hindiName: String,
    val isDay: Boolean,
    val auspiciousness: MuhurtaAuspiciousness
) {
    // Day muhurtas (sunrise to sunset, 1-15)
    RUDRA(1, "Rudra", "\u0930\u0941\u0926\u094D\u0930", true, MuhurtaAuspiciousness.ASHUBH),
    AHI(2, "Ahi", "\u0905\u0939\u093F", true, MuhurtaAuspiciousness.ASHUBH),
    MITRA(3, "Mitra", "\u092E\u093F\u0924\u094D\u0930", true, MuhurtaAuspiciousness.SHUBH),
    PITRU(4, "Pitru", "\u092A\u093F\u0924\u0943", true, MuhurtaAuspiciousness.ASHUBH),
    VASU(5, "Vasu", "\u0935\u0938\u0941", true, MuhurtaAuspiciousness.SHUBH),
    VARA(6, "Vara", "\u0935\u093E\u0930", true, MuhurtaAuspiciousness.SHUBH),
    VISHWADEVA(7, "Vishwadeva", "\u0935\u093F\u0936\u094D\u0935\u0926\u0947\u0935", true, MuhurtaAuspiciousness.SHUBH),
    VIDHI(8, "Vidhi (Abhijit)", "\u0935\u093F\u0927\u093F (\u0905\u092D\u093F\u091C\u093F\u0924)", true, MuhurtaAuspiciousness.ATHI_SHUBH),
    SATAMUKHI(9, "Satamukhi", "\u0938\u0924\u092E\u0941\u0916\u0940", true, MuhurtaAuspiciousness.SHUBH),
    PURUHUTA(10, "Puruhuta", "\u092A\u0941\u0930\u0941\u0939\u0942\u0924", true, MuhurtaAuspiciousness.MADHYAM),
    VAHINI(11, "Vahini", "\u0935\u093E\u0939\u093F\u0928\u0940", true, MuhurtaAuspiciousness.ASHUBH),
    NAKTANCHARA(12, "Naktanchara", "\u0928\u0915\u094D\u0924\u0902\u091A\u0930", true, MuhurtaAuspiciousness.ASHUBH),
    VARUNA(13, "Varuna", "\u0935\u0930\u0941\u0923", true, MuhurtaAuspiciousness.SHUBH),
    ARYAMAN(14, "Aryaman", "\u0905\u0930\u094D\u092F\u092E\u0928", true, MuhurtaAuspiciousness.MADHYAM),
    BHAGA(15, "Bhaga", "\u092D\u0917", true, MuhurtaAuspiciousness.ASHUBH),

    // Night muhurtas (sunset to next sunrise, 16-30)
    GIRISHA(16, "Girisha", "\u0917\u093F\u0930\u0940\u0936", false, MuhurtaAuspiciousness.ASHUBH),
    AJIPADA(17, "Ajipada", "\u0905\u091C\u093F\u092A\u0926", false, MuhurtaAuspiciousness.MADHYAM),
    AHIRBUDHNYA(18, "Ahirbudhnya", "\u0905\u0939\u093F\u0930\u094D\u092C\u0941\u0927\u094D\u0928\u094D\u092F", false, MuhurtaAuspiciousness.SHUBH),
    PUSHA(19, "Pusha", "\u092A\u0942\u0937\u093E", false, MuhurtaAuspiciousness.SHUBH),
    ASHWINI(20, "Ashwini", "\u0905\u0936\u094D\u0935\u093F\u0928\u0940", false, MuhurtaAuspiciousness.SHUBH),
    YAMA(21, "Yama", "\u092F\u092E", false, MuhurtaAuspiciousness.ASHUBH),
    AGNI(22, "Agni", "\u0905\u0917\u094D\u0928\u093F", false, MuhurtaAuspiciousness.SHUBH),
    VIDHATA(23, "Vidhata", "\u0935\u093F\u0927\u093E\u0924\u093E", false, MuhurtaAuspiciousness.SHUBH),
    CHANDA(24, "Chanda", "\u091A\u0923\u094D\u0921", false, MuhurtaAuspiciousness.ASHUBH),
    ADITI(25, "Aditi", "\u0905\u0926\u093F\u0924\u093F", false, MuhurtaAuspiciousness.SHUBH),
    JIVA(26, "Jiva", "\u091C\u0940\u0935", false, MuhurtaAuspiciousness.SHUBH),
    VISHNU(27, "Vishnu", "\u0935\u093F\u0937\u094D\u0923\u0941", false, MuhurtaAuspiciousness.SHUBH),
    YUMIGADYUTI(28, "Yumigadyuti", "\u092F\u0941\u092E\u093F\u0917\u0926\u094D\u092F\u0941\u0924\u093F", false, MuhurtaAuspiciousness.MADHYAM),
    BRAHMA(29, "Brahma", "\u092C\u094D\u0930\u0939\u094D\u092E\u093E", false, MuhurtaAuspiciousness.SHUBH),
    SAMUDRAM(30, "Samudram", "\u0938\u092E\u0941\u0926\u094D\u0930\u092E", false, MuhurtaAuspiciousness.MADHYAM);

    companion object {
        val dayMuhurtas get() = entries.filter { it.isDay }
        val nightMuhurtas get() = entries.filter { !it.isDay }
        fun fromNumber(number: Int): Muhurta = entries.first { it.number == number }
    }
}

data class MuhurtaPeriod(
    val muhurta: Muhurta,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val isCurrent: Boolean = false
)
