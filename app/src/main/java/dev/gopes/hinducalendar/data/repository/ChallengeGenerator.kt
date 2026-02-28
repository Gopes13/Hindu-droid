package dev.gopes.hinducalendar.data.repository

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.domain.model.ChallengeType
import dev.gopes.hinducalendar.domain.model.DailyChallenge
import java.util.Calendar
import kotlin.random.Random

internal class ChallengeGenerator(private val context: Context) {

    private var overrideLanguageCode: String? = null

    fun generateDailyChallenge(languageCode: String? = null): DailyChallenge {
        this.overrideLanguageCode = languageCode
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val type = ChallengeType.entries[dayOfYear % ChallengeType.entries.size]
        val rng = Random(dayOfYear.toLong())

        return when (type) {
            ChallengeType.PANCHANG_EXPLORER -> generatePanchangChallenge(dayOfYear, rng)
            ChallengeType.FESTIVAL_KNOWLEDGE -> generateFestivalChallenge(dayOfYear, rng)
            ChallengeType.VERSE_REFLECTION -> generateVerseChallenge(dayOfYear, rng)
            ChallengeType.MANTRA_MATCH -> generateMantraChallenge(dayOfYear, rng)
        }
    }

    private val tithiStringIds = intArrayOf(
        R.string.tithi_pratipada, R.string.tithi_dwitiya, R.string.tithi_tritiya,
        R.string.tithi_chaturthi, R.string.tithi_panchami, R.string.tithi_shashthi,
        R.string.tithi_saptami, R.string.tithi_ashtami, R.string.tithi_navami,
        R.string.tithi_dashami, R.string.tithi_ekadashi, R.string.tithi_dwadashi,
        R.string.tithi_trayodashi, R.string.tithi_chaturdashi, R.string.tithi_purnima,
        R.string.tithi_amavasya
    )

    private val nakshatraStringIds = intArrayOf(
        R.string.nakshatra_ashwini, R.string.nakshatra_bharani, R.string.nakshatra_krittika,
        R.string.nakshatra_rohini, R.string.nakshatra_mrigashira, R.string.nakshatra_ardra,
        R.string.nakshatra_punarvasu, R.string.nakshatra_pushya, R.string.nakshatra_ashlesha,
        R.string.nakshatra_magha, R.string.nakshatra_purva_phalguni, R.string.nakshatra_uttara_phalguni,
        R.string.nakshatra_hasta, R.string.nakshatra_chitra, R.string.nakshatra_swati,
        R.string.nakshatra_vishakha, R.string.nakshatra_anuradha, R.string.nakshatra_jyeshtha,
        R.string.nakshatra_mula, R.string.nakshatra_purvashadha, R.string.nakshatra_uttarashadha,
        R.string.nakshatra_shravana, R.string.nakshatra_dhanishta, R.string.nakshatra_shatabhisha,
        R.string.nakshatra_purva_bhadrapada, R.string.nakshatra_uttara_bhadrapada, R.string.nakshatra_revati
    )

    private val ordinalStringIds = intArrayOf(
        R.string.ordinal_1, R.string.ordinal_2, R.string.ordinal_3,
        R.string.ordinal_4, R.string.ordinal_5, R.string.ordinal_6,
        R.string.ordinal_7, R.string.ordinal_8, R.string.ordinal_9,
        R.string.ordinal_10, R.string.ordinal_11, R.string.ordinal_12,
        R.string.ordinal_13, R.string.ordinal_14, R.string.ordinal_15,
        R.string.ordinal_16, R.string.ordinal_17, R.string.ordinal_18,
        R.string.ordinal_19, R.string.ordinal_20, R.string.ordinal_21,
        R.string.ordinal_22, R.string.ordinal_23, R.string.ordinal_24,
        R.string.ordinal_25, R.string.ordinal_26, R.string.ordinal_27
    )

    private fun getLocalizedContext(): Context {
        val locales = AppCompatDelegate.getApplicationLocales()
        val locale = if (!locales.isEmpty) {
            locales[0]
        } else {
            // Fallback: use the language code passed from preferences
            overrideLanguageCode?.takeIf { it != "en" && it != "hl" }
                ?.let { java.util.Locale(it) }
        } ?: return context
        val config = Configuration(context.resources.configuration).apply {
            setLocale(locale)
        }
        return context.createConfigurationContext(config)
    }

    private fun s(id: Int): String = getLocalizedContext().getString(id)
    private fun s(id: Int, vararg args: Any): String = getLocalizedContext().getString(id, *args)

    private fun generatePanchangChallenge(day: Int, rng: Random): DailyChallenge {
        val useTithi = day % 2 == 0
        if (useTithi) {
            val tithis = tithiStringIds.map { s(it) }
            val correctIdx = day % tithis.size
            val correct = tithis[correctIdx]
            val ordinal = s(ordinalStringIds[correctIdx])
            val question = s(R.string.quiz_tithi_question, ordinal)
            val options = generateOptions(correct, tithis, rng)
            return DailyChallenge(
                id = "panchang_$day",
                type = ChallengeType.PANCHANG_EXPLORER,
                question = question,
                options = options.first,
                correctOptionIndex = options.second
            )
        } else {
            val nakshatras = nakshatraStringIds.map { s(it) }
            val correctIdx = day % nakshatras.size
            val correct = nakshatras[correctIdx]
            val ordinal = s(ordinalStringIds[correctIdx])
            val question = s(R.string.quiz_nakshatra_question, ordinal)
            val options = generateOptions(correct, nakshatras, rng)
            return DailyChallenge(
                id = "panchang_$day",
                type = ChallengeType.PANCHANG_EXPLORER,
                question = question,
                options = options.first,
                correctOptionIndex = options.second
            )
        }
    }

    private data class QuizQ(val question: String, val answer: String, val wrong: List<String>)

    private fun generateFestivalChallenge(day: Int, rng: Random): DailyChallenge {
        val festivals = listOf(
            QuizQ(s(R.string.quiz_festival_diwali_q), s(R.string.quiz_festival_diwali_a), listOf(s(R.string.quiz_festival_diwali_w1), s(R.string.quiz_festival_diwali_w2), s(R.string.quiz_festival_diwali_w3))),
            QuizQ(s(R.string.quiz_festival_holi_q), s(R.string.quiz_festival_holi_a), listOf(s(R.string.quiz_festival_holi_w1), s(R.string.quiz_festival_holi_w2), s(R.string.quiz_festival_holi_w3))),
            QuizQ(s(R.string.quiz_festival_navratri_q), s(R.string.quiz_festival_navratri_a), listOf(s(R.string.quiz_festival_navratri_w1), s(R.string.quiz_festival_navratri_w2), s(R.string.quiz_festival_navratri_w3))),
            QuizQ(s(R.string.quiz_festival_raksha_q), s(R.string.quiz_festival_raksha_a), listOf(s(R.string.quiz_festival_raksha_w1), s(R.string.quiz_festival_raksha_w2), s(R.string.quiz_festival_raksha_w3))),
            QuizQ(s(R.string.quiz_festival_janmashtami_q), s(R.string.quiz_festival_janmashtami_a), listOf(s(R.string.quiz_festival_janmashtami_w1), s(R.string.quiz_festival_janmashtami_w2), s(R.string.quiz_festival_janmashtami_w3))),
            QuizQ(s(R.string.quiz_festival_ganesh_q), s(R.string.quiz_festival_ganesh_a), listOf(s(R.string.quiz_festival_ganesh_w1), s(R.string.quiz_festival_ganesh_w2), s(R.string.quiz_festival_ganesh_w3))),
            QuizQ(s(R.string.quiz_festival_shivaratri_q), s(R.string.quiz_festival_shivaratri_a), listOf(s(R.string.quiz_festival_shivaratri_w1), s(R.string.quiz_festival_shivaratri_w2), s(R.string.quiz_festival_shivaratri_w3))),
            QuizQ(s(R.string.quiz_festival_sankranti_q), s(R.string.quiz_festival_sankranti_a), listOf(s(R.string.quiz_festival_sankranti_w1), s(R.string.quiz_festival_sankranti_w2), s(R.string.quiz_festival_sankranti_w3))),
            QuizQ(s(R.string.quiz_festival_baisakhi_q), s(R.string.quiz_festival_baisakhi_a), listOf(s(R.string.quiz_festival_baisakhi_w1), s(R.string.quiz_festival_baisakhi_w2), s(R.string.quiz_festival_baisakhi_w3))),
            QuizQ(s(R.string.quiz_festival_ramnavami_q), s(R.string.quiz_festival_ramnavami_a), listOf(s(R.string.quiz_festival_ramnavami_w1), s(R.string.quiz_festival_ramnavami_w2), s(R.string.quiz_festival_ramnavami_w3))),
            QuizQ(s(R.string.quiz_festival_hanuman_q), s(R.string.quiz_festival_hanuman_a), listOf(s(R.string.quiz_festival_hanuman_w1), s(R.string.quiz_festival_hanuman_w2), s(R.string.quiz_festival_hanuman_w3))),
            QuizQ(s(R.string.quiz_festival_gurupurnima_q), s(R.string.quiz_festival_gurupurnima_a), listOf(s(R.string.quiz_festival_gurupurnima_w1), s(R.string.quiz_festival_gurupurnima_w2), s(R.string.quiz_festival_gurupurnima_w3)))
        )
        val q = festivals[day % festivals.size]
        val allOptions = listOf(q.answer) + q.wrong
        val shuffled = allOptions.shuffled(rng)
        return DailyChallenge(
            id = "festival_$day",
            type = ChallengeType.FESTIVAL_KNOWLEDGE,
            question = q.question,
            options = shuffled,
            correctOptionIndex = shuffled.indexOf(q.answer)
        )
    }

    private fun generateVerseChallenge(day: Int, rng: Random): DailyChallenge {
        val verses = listOf(
            QuizQ(s(R.string.quiz_verse_gita_q), s(R.string.quiz_verse_gita_a), listOf(s(R.string.quiz_verse_gita_w1), s(R.string.quiz_verse_gita_w2), s(R.string.quiz_verse_gita_w3))),
            QuizQ(s(R.string.quiz_verse_chalisa_q), s(R.string.quiz_verse_chalisa_a), listOf(s(R.string.quiz_verse_chalisa_w1), s(R.string.quiz_verse_chalisa_w2), s(R.string.quiz_verse_chalisa_w3))),
            QuizQ(s(R.string.quiz_verse_japji_q), s(R.string.quiz_verse_japji_a), listOf(s(R.string.quiz_verse_japji_w1), s(R.string.quiz_verse_japji_w2), s(R.string.quiz_verse_japji_w3))),
            QuizQ(s(R.string.quiz_verse_sahasranama_q), s(R.string.quiz_verse_sahasranama_a), listOf(s(R.string.quiz_verse_sahasranama_w1), s(R.string.quiz_verse_sahasranama_w2), s(R.string.quiz_verse_sahasranama_w3))),
            QuizQ(s(R.string.quiz_verse_rudram_q), s(R.string.quiz_verse_rudram_a), listOf(s(R.string.quiz_verse_rudram_w1), s(R.string.quiz_verse_rudram_w2), s(R.string.quiz_verse_rudram_w3))),
            QuizQ(s(R.string.quiz_verse_devi_q), s(R.string.quiz_verse_devi_a), listOf(s(R.string.quiz_verse_devi_w1), s(R.string.quiz_verse_devi_w2), s(R.string.quiz_verse_devi_w3))),
            QuizQ(s(R.string.quiz_verse_soundarya_q), s(R.string.quiz_verse_soundarya_a), listOf(s(R.string.quiz_verse_soundarya_w1), s(R.string.quiz_verse_soundarya_w2), s(R.string.quiz_verse_soundarya_w3))),
            QuizQ(s(R.string.quiz_verse_sukhmani_q), s(R.string.quiz_verse_sukhmani_a), listOf(s(R.string.quiz_verse_sukhmani_w1), s(R.string.quiz_verse_sukhmani_w2), s(R.string.quiz_verse_sukhmani_w3)))
        )
        val q = verses[day % verses.size]
        val allOptions = listOf(q.answer) + q.wrong
        val shuffled = allOptions.shuffled(rng)
        return DailyChallenge(
            id = "verse_$day",
            type = ChallengeType.VERSE_REFLECTION,
            question = q.question,
            options = shuffled,
            correctOptionIndex = shuffled.indexOf(q.answer)
        )
    }

    private fun generateMantraChallenge(day: Int, rng: Random): DailyChallenge {
        val mantras = listOf(
            QuizQ(s(R.string.quiz_mantra_shiva_q), s(R.string.quiz_mantra_shiva_a), listOf(s(R.string.quiz_mantra_shiva_w1), s(R.string.quiz_mantra_shiva_w2), s(R.string.quiz_mantra_shiva_w3))),
            QuizQ(s(R.string.quiz_mantra_narayana_q), s(R.string.quiz_mantra_narayana_a), listOf(s(R.string.quiz_mantra_narayana_w1), s(R.string.quiz_mantra_narayana_w2), s(R.string.quiz_mantra_narayana_w3))),
            QuizQ(s(R.string.quiz_mantra_ganesha_q), s(R.string.quiz_mantra_ganesha_a), listOf(s(R.string.quiz_mantra_ganesha_w1), s(R.string.quiz_mantra_ganesha_w2), s(R.string.quiz_mantra_ganesha_w3))),
            QuizQ(s(R.string.quiz_mantra_gayatri_q), s(R.string.quiz_mantra_gayatri_a), listOf(s(R.string.quiz_mantra_gayatri_w1), s(R.string.quiz_mantra_gayatri_w2), s(R.string.quiz_mantra_gayatri_w3))),
            QuizQ(s(R.string.quiz_mantra_mrityunjaya_q), s(R.string.quiz_mantra_mrityunjaya_a), listOf(s(R.string.quiz_mantra_mrityunjaya_w1), s(R.string.quiz_mantra_mrityunjaya_w2), s(R.string.quiz_mantra_mrityunjaya_w3))),
            QuizQ(s(R.string.quiz_mantra_bija_q), s(R.string.quiz_mantra_bija_a), listOf(s(R.string.quiz_mantra_bija_w1), s(R.string.quiz_mantra_bija_w2), s(R.string.quiz_mantra_bija_w3))),
            QuizQ(s(R.string.quiz_mantra_harekrishna_q), s(R.string.quiz_mantra_harekrishna_a), listOf(s(R.string.quiz_mantra_harekrishna_w1), s(R.string.quiz_mantra_harekrishna_w2), s(R.string.quiz_mantra_harekrishna_w3))),
            QuizQ(s(R.string.quiz_mantra_ikonkar_q), s(R.string.quiz_mantra_ikonkar_a), listOf(s(R.string.quiz_mantra_ikonkar_w1), s(R.string.quiz_mantra_ikonkar_w2), s(R.string.quiz_mantra_ikonkar_w3)))
        )
        val q = mantras[day % mantras.size]
        val allOptions = listOf(q.answer) + q.wrong
        val shuffled = allOptions.shuffled(rng)
        return DailyChallenge(
            id = "mantra_$day",
            type = ChallengeType.MANTRA_MATCH,
            question = q.question,
            options = shuffled,
            correctOptionIndex = shuffled.indexOf(q.answer)
        )
    }

    private fun generateOptions(correct: String, pool: List<String>, rng: Random): Pair<List<String>, Int> {
        val wrong = pool.filter { it != correct }.shuffled(rng).take(3)
        val all = (listOf(correct) + wrong).shuffled(rng)
        return all to all.indexOf(correct)
    }
}
