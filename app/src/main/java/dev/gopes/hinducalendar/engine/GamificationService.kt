package dev.gopes.hinducalendar.engine

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.data.model.*
import java.time.LocalDate
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class GamificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // ── Point Values ────────────────────────────────────────────────────────
    companion object {
        const val POINTS_APP_OPEN = 5
        const val POINTS_VERSE_VIEW = 5
        const val POINTS_VERSE_READ = 10
        const val POINTS_CHALLENGE = 15
        const val POINTS_FESTIVAL_STORY = 5
        const val POINTS_TEXT_COMPLETION = 100
        const val POINTS_REFLECTION = 5
        const val POINTS_DEEP_STUDY = 3
        const val MAX_DEEP_STUDY_POINTS_PER_DAY = 50
        const val POINTS_JAPA_ROUND = 10
        const val MAX_JAPA_ROUNDS_REWARDED = 10
        const val POINTS_DIYA_LIGHTING = 5
        const val POINTS_DIYA_STREAK_BONUS = 15
        const val POINTS_SANSKRIT_LETTER = 5
        const val POINTS_SANSKRIT_MODULE = 50
        const val POINTS_SANSKRIT_VERSE = 5
    }

    // ── Daily Rewards ───────────────────────────────────────────────────────

    fun rewardAppOpen(data: GamificationData, streak: StreakData): GamificationData {
        if (!data.isEnabled) return data
        val today = LocalDate.now().toString()
        if (data.lastAppOpenRewardDate == today) return data
        var updated = data
            .addPoints(POINTS_APP_OPEN)
            .copy(lastAppOpenRewardDate = today)
        // Streak bonus
        if (data.lastStreakBonusDate != today) {
            updated = updated
                .addPoints(streak.streakBonus)
                .copy(lastStreakBonusDate = today)
        }
        return updated
    }

    fun rewardVerseView(data: GamificationData): GamificationData {
        if (!data.isEnabled) return data
        val today = LocalDate.now().toString()
        if (data.lastVerseViewRewardDate == today) return data
        return data
            .addPoints(POINTS_VERSE_VIEW)
            .copy(lastVerseViewRewardDate = today)
    }

    fun rewardVerseRead(data: GamificationData): GamificationData {
        if (!data.isEnabled) return data
        val today = LocalDate.now().toString()
        if (data.lastVerseReadRewardDate == today) return data
        return data
            .addPoints(POINTS_VERSE_READ)
            .copy(lastVerseReadRewardDate = today)
    }

    fun rewardChallenge(data: GamificationData): GamificationData {
        if (!data.isEnabled) return data
        val today = LocalDate.now().toString()
        if (data.lastChallengeDate == today && data.lastChallengeCompleted) return data
        return data
            .addPoints(POINTS_CHALLENGE)
            .copy(
                lastChallengeDate = today,
                lastChallengeCompleted = true,
                challengesSolved = data.challengesSolved + 1
            )
    }

    fun rewardFestivalStory(data: GamificationData, festivalId: String): GamificationData {
        if (!data.isEnabled) return data
        if (festivalId in data.festivalStoriesRead) return data
        return data
            .addPoints(POINTS_FESTIVAL_STORY)
            .copy(festivalStoriesRead = data.festivalStoriesRead + festivalId)
    }

    fun rewardTextCompletion(data: GamificationData, textType: SacredTextType): GamificationData {
        if (!data.isEnabled) return data
        if (textType.name in data.textsCompleted) return data
        return data
            .addPoints(POINTS_TEXT_COMPLETION)
            .copy(textsCompleted = data.textsCompleted + textType.name)
    }

    fun rewardReflection(data: GamificationData): GamificationData {
        if (!data.isEnabled) return data
        return data
            .addPoints(POINTS_REFLECTION)
            .copy(reflectionsWritten = data.reflectionsWritten + 1)
    }

    fun rewardDeepStudy(data: GamificationData): GamificationData {
        if (!data.isEnabled) return data
        val today = LocalDate.now().toString()
        val todayPoints = if (data.lastDeepStudyRewardDate == today) data.deepStudyPointsToday else 0
        if (todayPoints >= MAX_DEEP_STUDY_POINTS_PER_DAY) return data
        val reward = POINTS_DEEP_STUDY.coerceAtMost(MAX_DEEP_STUDY_POINTS_PER_DAY - todayPoints)
        return data
            .addPoints(reward)
            .copy(
                deepStudySessions = data.deepStudySessions + 1,
                lastDeepStudyRewardDate = today,
                deepStudyPointsToday = todayPoints + reward
            )
    }

    fun trackExplanationView(data: GamificationData): GamificationData {
        if (!data.isEnabled) return data
        return data.copy(versesExplained = data.versesExplained + 1)
    }

    fun trackPanchangCheck(data: GamificationData): GamificationData {
        if (!data.isEnabled) return data
        val today = LocalDate.now().toString()
        if (data.lastPanchangCheckDate == today) return data
        return data.copy(
            panchangDaysChecked = data.panchangDaysChecked + 1,
            lastPanchangCheckDate = today
        )
    }

    fun trackDharmaPath(data: GamificationData, pathId: String): GamificationData {
        if (!data.isEnabled) return data
        return data.copy(dharmaPathsExplored = data.dharmaPathsExplored + pathId)
    }

    fun trackLanguage(data: GamificationData, language: String): GamificationData {
        if (!data.isEnabled) return data
        return data.copy(languagesUsed = data.languagesUsed + language)
    }

    // ── Badge Checking ──────────────────────────────────────────────────────

    fun checkAndAwardBadges(data: GamificationData, streak: StreakData): Pair<GamificationData, List<String>> {
        if (!data.isEnabled) return data to emptyList()
        val newBadges = mutableListOf<String>()
        var updated = data
        for (badge in SadhanaBadge.allBadges) {
            if (!updated.hasBadge(badge.id) && badge.requirement(updated, streak)) {
                updated = updated.awardBadge(badge.id)
                newBadges.add(badge.id)
            }
        }
        return updated to newBadges
    }

    // ── Japa & Diya Rewards ────────────────────────────────────────────────

    fun rewardJapaRound(data: GamificationData, japaState: dev.gopes.hinducalendar.data.model.JapaState): GamificationData {
        if (!data.isEnabled) return data
        if (japaState.roundsRewardedToday >= MAX_JAPA_ROUNDS_REWARDED) return data
        return data.addPoints(POINTS_JAPA_ROUND)
    }

    fun rewardDiyaLighting(data: GamificationData, diyaState: dev.gopes.hinducalendar.data.model.DiyaState): GamificationData {
        if (!data.isEnabled) return data
        val today = LocalDate.now().toString()
        if (diyaState.lastDiyaRewardDate == today) return data
        var updated = data.addPoints(POINTS_DIYA_LIGHTING)
        // 7-day streak bonus
        if (diyaState.lightingStreak > 0 && diyaState.lightingStreak % 7 == 0) {
            updated = updated.addPoints(POINTS_DIYA_STREAK_BONUS)
        }
        return updated
    }

    // ── Japa & Diya Badge Checks ───────────────────────────────────────────

    fun checkJapaBadges(data: GamificationData, japaState: dev.gopes.hinducalendar.data.model.JapaState): GamificationData {
        var updated = data
        val rounds = japaState.totalRoundsLifetime
        if (rounds >= 10 && !updated.hasBadge("badge_japa_10")) updated = updated.awardBadge("badge_japa_10")
        if (rounds >= 108 && !updated.hasBadge("badge_japa_108")) updated = updated.awardBadge("badge_japa_108")
        if (rounds >= 1008 && !updated.hasBadge("badge_japa_1008")) updated = updated.awardBadge("badge_japa_1008")
        val streak = japaState.japaStreak
        if (streak >= 7 && !updated.hasBadge("badge_japa_streak_7")) updated = updated.awardBadge("badge_japa_streak_7")
        if (streak >= 30 && !updated.hasBadge("badge_japa_streak_30")) updated = updated.awardBadge("badge_japa_streak_30")
        return updated
    }

    fun checkDiyaBadges(data: GamificationData, diyaState: dev.gopes.hinducalendar.data.model.DiyaState): GamificationData {
        var updated = data
        val days = diyaState.totalDaysLit
        if (days >= 7 && !updated.hasBadge("badge_diya_7")) updated = updated.awardBadge("badge_diya_7")
        if (days >= 30 && !updated.hasBadge("badge_diya_30")) updated = updated.awardBadge("badge_diya_30")
        if (days >= 108 && !updated.hasBadge("badge_diya_108")) updated = updated.awardBadge("badge_diya_108")
        val streak = diyaState.lightingStreak
        if (streak >= 7 && !updated.hasBadge("badge_diya_streak_7")) updated = updated.awardBadge("badge_diya_streak_7")
        return updated
    }

    // ── Sanskrit Rewards ───────────────────────────────────────────────────

    fun rewardSanskritLesson(data: GamificationData, points: Int): GamificationData {
        if (!data.isEnabled) return data
        return data.addPoints(points)
    }

    fun rewardSanskritLetter(data: GamificationData): GamificationData {
        if (!data.isEnabled) return data
        return data.addPoints(POINTS_SANSKRIT_LETTER)
    }

    fun rewardSanskritModule(data: GamificationData): GamificationData {
        if (!data.isEnabled) return data
        return data.addPoints(POINTS_SANSKRIT_MODULE)
    }

    fun rewardSanskritVerse(data: GamificationData): GamificationData {
        if (!data.isEnabled) return data
        return data.addPoints(POINTS_SANSKRIT_VERSE)
    }

    fun checkSanskritBadges(data: GamificationData, progress: SanskritProgress): GamificationData {
        var updated = data
        if (progress.isModuleComplete("module1") && !updated.hasBadge("badge_sanskrit_first_letters")) {
            updated = updated.awardBadge("badge_sanskrit_first_letters")
        }
        if (progress.lessonsCount >= 10 && !updated.hasBadge("badge_sanskrit_student")) {
            updated = updated.awardBadge("badge_sanskrit_student")
        }
        if (progress.lettersCount >= 20 && !updated.hasBadge("badge_sanskrit_scholar")) {
            updated = updated.awardBadge("badge_sanskrit_scholar")
        }
        if (progress.isModuleComplete("module5") && !updated.hasBadge("badge_sanskrit_mantra_reader")) {
            updated = updated.awardBadge("badge_sanskrit_mantra_reader")
        }
        return updated
    }

    // ── Daily Challenge Generation ──────────────────────────────────────────

    fun generateDailyChallenge(): DailyChallenge {
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

    private fun s(id: Int): String = context.getString(id)
    private fun s(id: Int, vararg args: Any): String = context.getString(id, *args)

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

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun generateOptions(correct: String, pool: List<String>, rng: Random): Pair<List<String>, Int> {
        val wrong = pool.filter { it != correct }.shuffled(rng).take(3)
        val all = (listOf(correct) + wrong).shuffled(rng)
        return all to all.indexOf(correct)
    }
}
