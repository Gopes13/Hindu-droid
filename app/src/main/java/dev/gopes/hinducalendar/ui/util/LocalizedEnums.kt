package dev.gopes.hinducalendar.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.data.model.*

/** Localized display name for [DharmaPath] using Android string resources. */
@Composable
fun DharmaPath.localizedName(): String = when (this) {
    DharmaPath.GENERAL -> stringResource(R.string.dharma_general)
    DharmaPath.VAISHNAV -> stringResource(R.string.dharma_vaishnav)
    DharmaPath.SHAIV -> stringResource(R.string.dharma_shaiv)
    DharmaPath.SHAKTA -> stringResource(R.string.dharma_shakta)
    DharmaPath.SMARTA -> stringResource(R.string.dharma_smarta)
    DharmaPath.ISKCON -> stringResource(R.string.dharma_iskcon)
    DharmaPath.SWAMINARAYAN -> stringResource(R.string.dharma_swaminarayan)
    DharmaPath.SIKH -> stringResource(R.string.dharma_sikh)
    DharmaPath.JAIN -> stringResource(R.string.dharma_jain)
}

/** Localized description for [DharmaPath]. */
@Composable
fun DharmaPath.localizedDescription(): String = when (this) {
    DharmaPath.GENERAL -> stringResource(R.string.dharma_general_desc)
    DharmaPath.VAISHNAV -> stringResource(R.string.dharma_vaishnav_desc)
    DharmaPath.SHAIV -> stringResource(R.string.dharma_shaiv_desc)
    DharmaPath.SHAKTA -> stringResource(R.string.dharma_shakta_desc)
    DharmaPath.SMARTA -> stringResource(R.string.dharma_smarta_desc)
    DharmaPath.ISKCON -> stringResource(R.string.dharma_iskcon_desc)
    DharmaPath.SWAMINARAYAN -> stringResource(R.string.dharma_swaminarayan_desc)
    DharmaPath.SIKH -> stringResource(R.string.dharma_sikh_desc)
    DharmaPath.JAIN -> stringResource(R.string.dharma_jain_desc)
}

/** Localized display name for [SacredTextType]. */
@Composable
fun SacredTextType.localizedName(): String = when (this) {
    SacredTextType.GITA -> stringResource(R.string.text_name_gita)
    SacredTextType.HANUMAN_CHALISA -> stringResource(R.string.text_name_hanuman_chalisa)
    SacredTextType.JAPJI_SAHIB -> stringResource(R.string.text_name_japji_sahib)
    SacredTextType.BHAGAVATA -> stringResource(R.string.text_name_bhagavata)
    SacredTextType.VISHNU_SAHASRANAMA -> stringResource(R.string.text_name_vishnu_sahasranama)
    SacredTextType.SHIVA_PURANA -> stringResource(R.string.text_name_shiva_purana)
    SacredTextType.RUDRAM -> stringResource(R.string.text_name_rudram)
    SacredTextType.DEVI_MAHATMYA -> stringResource(R.string.text_name_devi_mahatmya)
    SacredTextType.SOUNDARYA_LAHARI -> stringResource(R.string.text_name_soundarya_lahari)
    SacredTextType.SHIKSHAPATRI -> stringResource(R.string.text_name_shikshapatri)
    SacredTextType.VACHANAMRUT -> stringResource(R.string.text_name_vachanamrut)
    SacredTextType.SUKHMANI -> stringResource(R.string.text_name_sukhmani)
    SacredTextType.GURBANI -> stringResource(R.string.text_name_gurbani)
    SacredTextType.TATTVARTHA_SUTRA -> stringResource(R.string.text_name_tattvartha_sutra)
    SacredTextType.JAIN_PRAYERS -> stringResource(R.string.text_name_jain_prayers)
}

/** Localized theme tag for [SacredTextType]. */
@Composable
fun SacredTextType.localizedThemeTag(): String = when (this.themeTag) {
    "Philosophy" -> stringResource(R.string.theme_philosophy)
    "Devotional Hymn" -> stringResource(R.string.theme_devotional_hymn)
    "Morning Prayer" -> stringResource(R.string.theme_morning_prayer)
    "Epic Stories" -> stringResource(R.string.theme_epic_stories)
    "1000 Names" -> stringResource(R.string.theme_1000_names)
    "Vedic Chant" -> stringResource(R.string.theme_vedic_chant)
    "Goddess Glory" -> stringResource(R.string.theme_goddess_glory)
    "Devotional Poetry" -> stringResource(R.string.theme_devotional_poetry)
    "Ethical Guide" -> stringResource(R.string.theme_ethical_guide)
    "Discourses" -> stringResource(R.string.theme_discourses)
    "Meditation" -> stringResource(R.string.theme_meditation)
    "Daily Shabads" -> stringResource(R.string.theme_daily_shabads)
    "Jain Philosophy" -> stringResource(R.string.theme_jain_philosophy)
    "Prayers & Teachings" -> stringResource(R.string.theme_prayers_teachings)
    else -> this.themeTag
}

/** Localized unit label for [SacredTextType]. */
@Composable
fun SacredTextType.localizedUnitLabel(): String = when (this.unitLabel) {
    "verses" -> stringResource(R.string.unit_verses)
    "pauris" -> stringResource(R.string.unit_pauris)
    "episodes" -> stringResource(R.string.unit_episodes)
    "shlokas" -> stringResource(R.string.unit_shlokas)
    "anuvakas" -> stringResource(R.string.unit_anuvakas)
    "chapters" -> stringResource(R.string.unit_chapters)
    "discourses" -> stringResource(R.string.unit_discourses)
    "ashtpadis" -> stringResource(R.string.unit_ashtpadis)
    "shabads" -> stringResource(R.string.unit_shabads)
    "sutras" -> stringResource(R.string.unit_sutras)
    "teachings" -> stringResource(R.string.unit_teachings)
    else -> this.unitLabel
}

/** Localized display name for [CalendarTradition]. */
@Composable
fun CalendarTradition.localizedName(): String = when (this) {
    CalendarTradition.PURNIMANT -> stringResource(R.string.tradition_purnimant)
    CalendarTradition.AMANT -> stringResource(R.string.tradition_amant)
    CalendarTradition.GUJARATI -> stringResource(R.string.tradition_gujarati)
    CalendarTradition.BENGALI -> stringResource(R.string.tradition_bengali)
    CalendarTradition.TAMIL -> stringResource(R.string.tradition_tamil)
    CalendarTradition.MALAYALAM -> stringResource(R.string.tradition_malayalam)
}

/** Localized description for [CalendarTradition]. */
@Composable
fun CalendarTradition.localizedDescription(): String = when (this) {
    CalendarTradition.PURNIMANT -> stringResource(R.string.tradition_purnimant_desc)
    CalendarTradition.AMANT -> stringResource(R.string.tradition_amant_desc)
    CalendarTradition.GUJARATI -> stringResource(R.string.tradition_gujarati_desc)
    CalendarTradition.BENGALI -> stringResource(R.string.tradition_bengali_desc)
    CalendarTradition.TAMIL -> stringResource(R.string.tradition_tamil_desc)
    CalendarTradition.MALAYALAM -> stringResource(R.string.tradition_malayalam_desc)
}

/** Localized display name for [FestivalCategory]. */
@Composable
fun FestivalCategory.localizedName(): String = when (this) {
    FestivalCategory.MAJOR -> stringResource(R.string.category_major)
    FestivalCategory.MODERATE -> stringResource(R.string.category_moderate)
    FestivalCategory.RECURRING -> stringResource(R.string.category_recurring)
    FestivalCategory.REGIONAL -> stringResource(R.string.category_regional)
    FestivalCategory.VRAT -> stringResource(R.string.category_vrat)
}

/** Localized display name for [CalendarSyncOption]. */
@Composable
fun CalendarSyncOption.localizedName(): String = when (this) {
    CalendarSyncOption.FESTIVALS_ONLY -> stringResource(R.string.sync_festivals_only)
    CalendarSyncOption.FESTIVALS_AND_TITHIS -> stringResource(R.string.sync_festivals_and_tithis)
    CalendarSyncOption.FULL_PANCHANG -> stringResource(R.string.sync_full_panchang)
}

/** Localized display name for [ReminderTiming]. */
@Composable
fun ReminderTiming.localizedName(): String = when (this) {
    ReminderTiming.MORNING_OF -> stringResource(R.string.reminder_morning_of)
    ReminderTiming.EVENING_BEFORE -> stringResource(R.string.reminder_evening_before)
    ReminderTiming.DAY_BEFORE -> stringResource(R.string.reminder_day_before)
    ReminderTiming.TWO_DAYS_BEFORE -> stringResource(R.string.reminder_two_days_before)
}

/** Localized display name for [Tithi]. */
@Composable
fun Tithi.localizedName(): String = when (this) {
    Tithi.PRATIPADA -> stringResource(R.string.tithi_pratipada)
    Tithi.DWITIYA -> stringResource(R.string.tithi_dwitiya)
    Tithi.TRITIYA -> stringResource(R.string.tithi_tritiya)
    Tithi.CHATURTHI -> stringResource(R.string.tithi_chaturthi)
    Tithi.PANCHAMI -> stringResource(R.string.tithi_panchami)
    Tithi.SHASHTHI -> stringResource(R.string.tithi_shashthi)
    Tithi.SAPTAMI -> stringResource(R.string.tithi_saptami)
    Tithi.ASHTAMI -> stringResource(R.string.tithi_ashtami)
    Tithi.NAVAMI -> stringResource(R.string.tithi_navami)
    Tithi.DASHAMI -> stringResource(R.string.tithi_dashami)
    Tithi.EKADASHI -> stringResource(R.string.tithi_ekadashi)
    Tithi.DWADASHI -> stringResource(R.string.tithi_dwadashi)
    Tithi.TRAYODASHI -> stringResource(R.string.tithi_trayodashi)
    Tithi.CHATURDASHI -> stringResource(R.string.tithi_chaturdashi)
    Tithi.PURNIMA -> stringResource(R.string.tithi_purnima)
    Tithi.AMAVASYA -> stringResource(R.string.tithi_amavasya)
}

/** Localized display name for [Nakshatra]. */
@Composable
fun Nakshatra.localizedName(): String = when (this) {
    Nakshatra.ASHWINI -> stringResource(R.string.nakshatra_ashwini)
    Nakshatra.BHARANI -> stringResource(R.string.nakshatra_bharani)
    Nakshatra.KRITTIKA -> stringResource(R.string.nakshatra_krittika)
    Nakshatra.ROHINI -> stringResource(R.string.nakshatra_rohini)
    Nakshatra.MRIGASHIRA -> stringResource(R.string.nakshatra_mrigashira)
    Nakshatra.ARDRA -> stringResource(R.string.nakshatra_ardra)
    Nakshatra.PUNARVASU -> stringResource(R.string.nakshatra_punarvasu)
    Nakshatra.PUSHYA -> stringResource(R.string.nakshatra_pushya)
    Nakshatra.ASHLESHA -> stringResource(R.string.nakshatra_ashlesha)
    Nakshatra.MAGHA -> stringResource(R.string.nakshatra_magha)
    Nakshatra.PURVA_PHALGUNI -> stringResource(R.string.nakshatra_purva_phalguni)
    Nakshatra.UTTARA_PHALGUNI -> stringResource(R.string.nakshatra_uttara_phalguni)
    Nakshatra.HASTA -> stringResource(R.string.nakshatra_hasta)
    Nakshatra.CHITRA -> stringResource(R.string.nakshatra_chitra)
    Nakshatra.SWATI -> stringResource(R.string.nakshatra_swati)
    Nakshatra.VISHAKHA -> stringResource(R.string.nakshatra_vishakha)
    Nakshatra.ANURADHA -> stringResource(R.string.nakshatra_anuradha)
    Nakshatra.JYESHTHA -> stringResource(R.string.nakshatra_jyeshtha)
    Nakshatra.MULA -> stringResource(R.string.nakshatra_mula)
    Nakshatra.PURVASHADHA -> stringResource(R.string.nakshatra_purvashadha)
    Nakshatra.UTTARASHADHA -> stringResource(R.string.nakshatra_uttarashadha)
    Nakshatra.SHRAVANA -> stringResource(R.string.nakshatra_shravana)
    Nakshatra.DHANISHTA -> stringResource(R.string.nakshatra_dhanishta)
    Nakshatra.SHATABHISHA -> stringResource(R.string.nakshatra_shatabhisha)
    Nakshatra.PURVA_BHADRAPADA -> stringResource(R.string.nakshatra_purva_bhadrapada)
    Nakshatra.UTTARA_BHADRAPADA -> stringResource(R.string.nakshatra_uttara_bhadrapada)
    Nakshatra.REVATI -> stringResource(R.string.nakshatra_revati)
}

/** Localized display name for [Yoga]. */
@Composable
fun Yoga.localizedName(): String = when (this) {
    Yoga.VISHKAMBHA -> stringResource(R.string.yoga_vishkambha)
    Yoga.PRITI -> stringResource(R.string.yoga_priti)
    Yoga.AYUSHMAN -> stringResource(R.string.yoga_ayushman)
    Yoga.SAUBHAGYA -> stringResource(R.string.yoga_saubhagya)
    Yoga.SHOBHANA -> stringResource(R.string.yoga_shobhana)
    Yoga.ATIGANDA -> stringResource(R.string.yoga_atiganda)
    Yoga.SUKARMA -> stringResource(R.string.yoga_sukarma)
    Yoga.DHRITI -> stringResource(R.string.yoga_dhriti)
    Yoga.SHULA -> stringResource(R.string.yoga_shula)
    Yoga.GANDA -> stringResource(R.string.yoga_ganda)
    Yoga.VRIDDHI -> stringResource(R.string.yoga_vriddhi)
    Yoga.DHRUVA -> stringResource(R.string.yoga_dhruva)
    Yoga.VYAGHATA -> stringResource(R.string.yoga_vyaghata)
    Yoga.HARSHANA -> stringResource(R.string.yoga_harshana)
    Yoga.VAJRA -> stringResource(R.string.yoga_vajra)
    Yoga.SIDDHI -> stringResource(R.string.yoga_siddhi)
    Yoga.VYATIPATA -> stringResource(R.string.yoga_vyatipata)
    Yoga.VARIYAN -> stringResource(R.string.yoga_variyan)
    Yoga.PARIGHA -> stringResource(R.string.yoga_parigha)
    Yoga.SHIVA -> stringResource(R.string.yoga_shiva)
    Yoga.SIDDHA -> stringResource(R.string.yoga_siddha)
    Yoga.SADHYA -> stringResource(R.string.yoga_sadhya)
    Yoga.SHUBHA -> stringResource(R.string.yoga_shubha)
    Yoga.SHUKLA -> stringResource(R.string.yoga_shukla)
    Yoga.BRAHMA -> stringResource(R.string.yoga_brahma)
    Yoga.INDRA -> stringResource(R.string.yoga_indra)
    Yoga.VAIDHRITI -> stringResource(R.string.yoga_vaidhriti)
}

/** Localized display name for [Karana]. */
@Composable
fun Karana.localizedName(): String = when (this) {
    Karana.KIMSTUGHNA -> stringResource(R.string.karana_kimstughna)
    Karana.SHAKUNI -> stringResource(R.string.karana_shakuni)
    Karana.CHATUSHPADA -> stringResource(R.string.karana_chatushpada)
    Karana.NAGAVA -> stringResource(R.string.karana_nagava)
    Karana.BAVA -> stringResource(R.string.karana_bava)
    Karana.BALAVA -> stringResource(R.string.karana_balava)
    Karana.KAULAVA -> stringResource(R.string.karana_kaulava)
    Karana.TAITILA -> stringResource(R.string.karana_taitila)
    Karana.GARAJA -> stringResource(R.string.karana_garaja)
    Karana.VANIJA -> stringResource(R.string.karana_vanija)
    Karana.VISHTI -> stringResource(R.string.karana_vishti)
}

/** Localized display name for [Paksha]. */
@Composable
fun Paksha.localizedName(): String = when (this) {
    Paksha.SHUKLA -> stringResource(R.string.paksha_shukla)
    Paksha.KRISHNA -> stringResource(R.string.paksha_krishna)
}

/** Localized display name for [HinduMonth]. */
@Composable
fun HinduMonth.localizedName(): String = when (this) {
    HinduMonth.CHAITRA -> stringResource(R.string.month_chaitra)
    HinduMonth.VAISHAKHA -> stringResource(R.string.month_vaishakha)
    HinduMonth.JYESHTHA -> stringResource(R.string.month_jyeshtha)
    HinduMonth.ASHADHA -> stringResource(R.string.month_ashadha)
    HinduMonth.SHRAVANA -> stringResource(R.string.month_shravana)
    HinduMonth.BHADRAPADA -> stringResource(R.string.month_bhadrapada)
    HinduMonth.ASHWIN -> stringResource(R.string.month_ashwin)
    HinduMonth.KARTIK -> stringResource(R.string.month_kartik)
    HinduMonth.MARGASHIRSHA -> stringResource(R.string.month_margashirsha)
    HinduMonth.PAUSHA -> stringResource(R.string.month_pausha)
    HinduMonth.MAGHA -> stringResource(R.string.month_magha)
    HinduMonth.PHALGUNA -> stringResource(R.string.month_phalguna)
}

/** Localized display name for [SadhanaBadge.BadgeCategory]. */
@Composable
fun SadhanaBadge.BadgeCategory.localizedName(): String = when (this) {
    SadhanaBadge.BadgeCategory.STREAK -> stringResource(R.string.badge_cat_streak)
    SadhanaBadge.BadgeCategory.TEXT_COMPLETION -> stringResource(R.string.badge_cat_text)
    SadhanaBadge.BadgeCategory.EXPLORER -> stringResource(R.string.badge_cat_explorer)
    SadhanaBadge.BadgeCategory.FESTIVAL -> stringResource(R.string.badge_cat_festival)
    SadhanaBadge.BadgeCategory.LANGUAGE -> stringResource(R.string.badge_cat_language)
    SadhanaBadge.BadgeCategory.PANCHANG -> stringResource(R.string.badge_cat_panchang)
    SadhanaBadge.BadgeCategory.CHALLENGE -> stringResource(R.string.badge_cat_challenge)
    SadhanaBadge.BadgeCategory.ENGAGEMENT -> stringResource(R.string.badge_cat_engagement)
    SadhanaBadge.BadgeCategory.SANSKRIT -> stringResource(R.string.badge_cat_sanskrit)
    SadhanaBadge.BadgeCategory.JAPA -> stringResource(R.string.badge_cat_japa)
    SadhanaBadge.BadgeCategory.DIYA -> stringResource(R.string.badge_cat_diya)
}

/** Localized display name for a [SadhanaBadge]. */
@Composable
fun SadhanaBadge.localizedName(): String = stringResource(
    when (this.titleKey) {
        "badge_streak_7" -> R.string.badge_streak_7
        "badge_streak_30" -> R.string.badge_streak_30
        "badge_streak_100" -> R.string.badge_streak_100
        "badge_streak_365" -> R.string.badge_streak_365
        "badge_explorer_2" -> R.string.badge_explorer_2
        "badge_explorer_5" -> R.string.badge_explorer_5
        "badge_festival_5" -> R.string.badge_festival_5
        "badge_festival_20" -> R.string.badge_festival_20
        "badge_language_3" -> R.string.badge_language_3
        "badge_language_6" -> R.string.badge_language_6
        "badge_panchang_7" -> R.string.badge_panchang_7
        "badge_panchang_30" -> R.string.badge_panchang_30
        "badge_panchang_100" -> R.string.badge_panchang_100
        "badge_challenge_7" -> R.string.badge_challenge_7
        "badge_challenge_30" -> R.string.badge_challenge_30
        "badge_explained_10" -> R.string.badge_explained_10
        "badge_explained_100" -> R.string.badge_explained_100
        "badge_reflections_5" -> R.string.badge_reflections_5
        "badge_reflections_25" -> R.string.badge_reflections_25
        "badge_deep_study_7" -> R.string.badge_deep_study_7
        "badge_deep_study_30" -> R.string.badge_deep_study_30
        "badge_sanskrit_first_letters" -> R.string.badge_sanskrit_first_letters
        "badge_sanskrit_student" -> R.string.badge_sanskrit_student
        "badge_sanskrit_scholar" -> R.string.badge_sanskrit_scholar
        "badge_sanskrit_mantra_reader" -> R.string.badge_sanskrit_mantra_reader
        "badge_japa_10" -> R.string.badge_japa_10
        "badge_japa_108" -> R.string.badge_japa_108
        "badge_japa_1008" -> R.string.badge_japa_1008
        "badge_japa_streak_7" -> R.string.badge_japa_streak_7
        "badge_japa_streak_30" -> R.string.badge_japa_streak_30
        "badge_diya_7" -> R.string.badge_diya_7
        "badge_diya_30" -> R.string.badge_diya_30
        "badge_diya_108" -> R.string.badge_diya_108
        "badge_diya_streak_7" -> R.string.badge_diya_streak_7
        else -> R.string.punya_points
    }
)

/** Localized origin language name for kirtans (e.g. "Hindi" → "हिन्दी"). */
@Composable
fun localizedOriginLanguage(originLanguage: String): String = when (originLanguage) {
    "Hindi" -> stringResource(R.string.origin_lang_hindi)
    "Awadhi" -> stringResource(R.string.origin_lang_awadhi)
    "Sanskrit" -> stringResource(R.string.origin_lang_sanskrit)
    "Gujarati" -> stringResource(R.string.origin_lang_gujarati)
    "Marathi" -> stringResource(R.string.origin_lang_marathi)
    "Bengali" -> stringResource(R.string.origin_lang_bengali)
    "Telugu" -> stringResource(R.string.origin_lang_telugu)
    "Kannada" -> stringResource(R.string.origin_lang_kannada)
    "Tamil" -> stringResource(R.string.origin_lang_tamil)
    "Malayalam" -> stringResource(R.string.origin_lang_malayalam)
    "Odia" -> stringResource(R.string.origin_lang_odia)
    "Assamese" -> stringResource(R.string.origin_lang_assamese)
    "Punjabi" -> stringResource(R.string.origin_lang_punjabi)
    else -> originLanguage
}

/** Localized Hindu date string, e.g. "फाल्गुन शुक्ल नवमी". */
@Composable
fun HinduDate.localizedDisplayString(): String {
    val adhik = if (isAdhikMaas) stringResource(R.string.adhik_prefix) + " " else ""
    val monthStr = month.localizedName()
    val pakshaStr = paksha.localizedName()
    val tithiStr = tithi.localizedName()
    return "$adhik$monthStr $pakshaStr $tithiStr"
}

/** Localized year display, e.g. "विक्रम संवत 2082". */
@Composable
fun HinduDate.localizedYearDisplay(tradition: CalendarTradition): String = when (tradition) {
    CalendarTradition.PURNIMANT, CalendarTradition.AMANT, CalendarTradition.GUJARATI ->
        stringResource(R.string.vikram_samvat_format, samvatYear)
    CalendarTradition.TAMIL ->
        stringResource(R.string.thiruvalluvar_format, thiruvalluvarYear)
    CalendarTradition.MALAYALAM ->
        stringResource(R.string.kollavarsham_format, kollavarshamYear)
    CalendarTradition.BENGALI ->
        stringResource(R.string.bangabda_format, bangabdaYear)
}

/** Helper to resolve a [PanchangElement] number to a localized Tithi name. */
@Composable
fun localizedTithiName(number: Int): String =
    Tithi.fromNumber(number).localizedName()

/** Helper to resolve a [PanchangElement] number to a localized Nakshatra name. */
@Composable
fun localizedNakshatraName(number: Int): String =
    Nakshatra.entries.getOrNull(number - 1)?.localizedName() ?: ""

/** Helper to resolve a [PanchangElement] number to a localized Yoga name. */
@Composable
fun localizedYogaName(number: Int): String =
    Yoga.entries.getOrNull(number - 1)?.localizedName() ?: ""

/** Helper to resolve a [PanchangElement] number to a localized Karana name. */
@Composable
fun localizedKaranaName(ordinal: Int): String =
    Karana.entries.getOrNull(ordinal)?.localizedName() ?: ""
