package dev.gopes.hinducalendar.domain.model

data class HinduDate(
    val month: HinduMonth,
    val paksha: Paksha,
    val tithi: Tithi,
    val samvatYear: Int,
    val shakaYear: Int,
    val isAdhikMaas: Boolean = false,
    val bangabdaYear: Int = 0,
    val thiruvalluvarYear: Int = 0,
    val kollavarshamYear: Int = 0,
    val nanakshahiYear: Int = 0,
    val virNirvanaSamvatYear: Int = 0
) {
    val displayString: String
        get() {
            val adhik = if (isAdhikMaas) "Adhik " else ""
            return "$adhik${month.displayName} ${paksha.displayName} ${tithi.displayName}"
        }

    val fullDisplayString: String
        get() = "$displayString, Vikram Samvat $samvatYear"

    /** Year string appropriate for the given tradition. */
    fun yearDisplayForTradition(tradition: CalendarTradition): String = when (tradition) {
        CalendarTradition.PURNIMANT, CalendarTradition.AMANT, CalendarTradition.GUJARATI ->
            "Vikram Samvat $samvatYear"
        CalendarTradition.TAMIL -> "Thiruvalluvar $thiruvalluvarYear"
        CalendarTradition.MALAYALAM -> "Kollavarsham $kollavarshamYear"
        CalendarTradition.BENGALI -> "Bangabda $bangabdaYear"
    }
}

enum class HinduMonth(val number: Int, val displayName: String, val hindiName: String) {
    CHAITRA(1, "Chaitra", "चैत्र"),
    VAISHAKHA(2, "Vaishakh", "वैशाख"),
    JYESHTHA(3, "Jyeshtha", "ज्येष्ठ"),
    ASHADHA(4, "Ashadh", "आषाढ़"),
    SHRAVANA(5, "Shravan", "श्रावण"),
    BHADRAPADA(6, "Bhadrapad", "भाद्रपद"),
    ASHWIN(7, "Ashwin", "आश्विन"),
    KARTIK(8, "Kartik", "कार्तिक"),
    MARGASHIRSHA(9, "Margashirsha", "मार्गशीर्ष"),
    PAUSHA(10, "Paush", "पौष"),
    MAGHA(11, "Magh", "माघ"),
    PHALGUNA(12, "Phalgun", "फाल्गुन");

    fun previous(): HinduMonth = when (this) {
        CHAITRA -> PHALGUNA
        VAISHAKHA -> CHAITRA
        JYESHTHA -> VAISHAKHA
        ASHADHA -> JYESHTHA
        SHRAVANA -> ASHADHA
        BHADRAPADA -> SHRAVANA
        ASHWIN -> BHADRAPADA
        KARTIK -> ASHWIN
        MARGASHIRSHA -> KARTIK
        PAUSHA -> MARGASHIRSHA
        MAGHA -> PAUSHA
        PHALGUNA -> MAGHA
    }

    fun next(): HinduMonth = when (this) {
        CHAITRA -> VAISHAKHA
        VAISHAKHA -> JYESHTHA
        JYESHTHA -> ASHADHA
        ASHADHA -> SHRAVANA
        SHRAVANA -> BHADRAPADA
        BHADRAPADA -> ASHWIN
        ASHWIN -> KARTIK
        KARTIK -> MARGASHIRSHA
        MARGASHIRSHA -> PAUSHA
        PAUSHA -> MAGHA
        MAGHA -> PHALGUNA
        PHALGUNA -> CHAITRA
    }

    companion object {
        fun fromSolarMonth(solarMonth: Int): HinduMonth {
            return entries.firstOrNull { it.number == solarMonth + 1 } ?: CHAITRA
        }
    }
}

enum class Paksha(val displayName: String, val hindiName: String) {
    SHUKLA("Shukla", "शुक्ल"),
    KRISHNA("Krishna", "कृष्ण");

    val key: String get() = name.lowercase()
}

enum class Tithi(val number: Int, val displayName: String, val hindiName: String) {
    PRATIPADA(1, "Pratipada", "प्रतिपदा"),
    DWITIYA(2, "Dwitiya", "द्वितीया"),
    TRITIYA(3, "Tritiya", "तृतीया"),
    CHATURTHI(4, "Chaturthi", "चतुर्थी"),
    PANCHAMI(5, "Panchami", "पंचमी"),
    SHASHTHI(6, "Shashthi", "षष्ठी"),
    SAPTAMI(7, "Saptami", "सप्तमी"),
    ASHTAMI(8, "Ashtami", "अष्टमी"),
    NAVAMI(9, "Navami", "नवमी"),
    DASHAMI(10, "Dashami", "दशमी"),
    EKADASHI(11, "Ekadashi", "एकादशी"),
    DWADASHI(12, "Dwadashi", "द्वादशी"),
    TRAYODASHI(13, "Trayodashi", "त्रयोदशी"),
    CHATURDASHI(14, "Chaturdashi", "चतुर्दशी"),
    PURNIMA(15, "Purnima", "पूर्णिमा"),
    AMAVASYA(30, "Amavasya", "अमावस्या");

    val continuousNumber: Int get() = number

    companion object {
        fun fromNumber(n: Int): Tithi {
            return entries.firstOrNull { it.number == n } ?: PRATIPADA
        }
    }
}
