package dev.gopes.hinducalendar.domain.model

enum class Nakshatra(
    val number: Int,
    val displayName: String,
    val hindiName: String,
    val deity: String
) {
    ASHWINI(1, "Ashwini", "अश्विनी", "Ashwini Kumaras"),
    BHARANI(2, "Bharani", "भरणी", "Yama"),
    KRITTIKA(3, "Krittika", "कृत्तिका", "Agni"),
    ROHINI(4, "Rohini", "रोहिणी", "Brahma"),
    MRIGASHIRA(5, "Mrigashira", "मृगशिरा", "Soma"),
    ARDRA(6, "Ardra", "आर्द्रा", "Rudra"),
    PUNARVASU(7, "Punarvasu", "पुनर्वसु", "Aditi"),
    PUSHYA(8, "Pushya", "पुष्य", "Brihaspati"),
    ASHLESHA(9, "Ashlesha", "आश्लेषा", "Sarpa"),
    MAGHA(10, "Magha", "मघा", "Pitris"),
    PURVA_PHALGUNI(11, "Purva Phalguni", "पूर्वा फाल्गुनी", "Bhaga"),
    UTTARA_PHALGUNI(12, "Uttara Phalguni", "उत्तरा फाल्गुनी", "Aryaman"),
    HASTA(13, "Hasta", "हस्त", "Savitar"),
    CHITRA(14, "Chitra", "चित्रा", "Tvashtar"),
    SWATI(15, "Swati", "स्वाति", "Vayu"),
    VISHAKHA(16, "Vishakha", "विशाखा", "Indra-Agni"),
    ANURADHA(17, "Anuradha", "अनुराधा", "Mitra"),
    JYESHTHA(18, "Jyeshtha", "ज्येष्ठा", "Indra"),
    MULA(19, "Mula", "मूल", "Nirrti"),
    PURVASHADHA(20, "Purvashadha", "पूर्वाषाढ़ा", "Apas"),
    UTTARASHADHA(21, "Uttarashadha", "उत्तराषाढ़ा", "Vishvadevas"),
    SHRAVANA(22, "Shravana", "श्रवण", "Vishnu"),
    DHANISHTA(23, "Dhanishta", "धनिष्ठा", "Vasus"),
    SHATABHISHA(24, "Shatabhisha", "शतभिषा", "Varuna"),
    PURVA_BHADRAPADA(25, "Purva Bhadrapada", "पूर्वा भाद्रपद", "Aja Ekapada"),
    UTTARA_BHADRAPADA(26, "Uttara Bhadrapada", "उत्तरा भाद्रपद", "Ahir Budhnya"),
    REVATI(27, "Revati", "रेवती", "Pushan");

    companion object {
        const val SPAN_DEGREES = 360.0 / 27.0

        fun fromSiderealLongitude(longitude: Double): Nakshatra {
            val normalized = ((longitude % 360.0) + 360.0) % 360.0
            val index = (normalized / SPAN_DEGREES).toInt()
            return entries.getOrElse(index.coerceIn(0, 26)) { ASHWINI }
        }
    }
}
