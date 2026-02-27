package dev.gopes.hinducalendar.engine

import android.content.Context
import dev.gopes.hinducalendar.data.repository.GamificationServiceImpl
import dev.gopes.hinducalendar.domain.model.*
import dev.gopes.hinducalendar.domain.service.GamificationService
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import java.time.LocalDate

class GamificationServiceImplTest {

    private lateinit var service: GamificationServiceImpl
    private val today = LocalDate.now().toString()
    private val yesterday = LocalDate.now().minusDays(1).toString()

    private fun enabledData(
        totalPunyaPoints: Int = 0,
        block: GamificationData.() -> GamificationData = { this }
    ) = GamificationData(isEnabled = true, totalPunyaPoints = totalPunyaPoints).block()

    @Before
    fun setUp() {
        val mockContext: Context = mock()
        service = GamificationServiceImpl(mockContext)
    }

    // ── Disabled Guard ─────────────────────────────────────────────────────

    @Test
    fun `all reward methods return unchanged data when disabled`() {
        val data = GamificationData(isEnabled = false)
        val streak = StreakData(currentStreak = 5)
        val japa = JapaState()
        val diya = DiyaState()

        assertSame(data, service.rewardAppOpen(data, streak))
        assertSame(data, service.rewardVerseView(data))
        assertSame(data, service.rewardVerseRead(data))
        assertSame(data, service.rewardChallenge(data))
        assertSame(data, service.rewardFestivalStory(data, "diwali"))
        assertSame(data, service.rewardTextCompletion(data, SacredTextType.GITA))
        assertSame(data, service.rewardReflection(data))
        assertSame(data, service.rewardDeepStudy(data))
        assertSame(data, service.trackExplanationView(data))
        assertSame(data, service.trackPanchangCheck(data))
        assertSame(data, service.trackDharmaPath(data, "shaiv"))
        assertSame(data, service.trackLanguage(data, "hi"))
        assertSame(data, service.rewardJapaRound(data, japa))
        assertSame(data, service.rewardDiyaLighting(data, diya))
        assertSame(data, service.rewardSanskritLesson(data, 10))
        assertSame(data, service.rewardSanskritLetter(data))
        assertSame(data, service.rewardSanskritModule(data))
        assertSame(data, service.rewardSanskritVerse(data))
    }

    @Test
    fun `checkAndAwardBadges returns unchanged data when disabled`() {
        val data = GamificationData(isEnabled = false)
        val (result, badges) = service.checkAndAwardBadges(data, StreakData())
        assertSame(data, result)
        assertTrue(badges.isEmpty())
    }

    // ── rewardAppOpen ───────────────────────────────────────────────────────

    @Test
    fun `rewardAppOpen grants points on first call today`() {
        val data = enabledData()
        val streak = StreakData(currentStreak = 3)
        val result = service.rewardAppOpen(data, streak)

        // APP_OPEN (5) + streak bonus (3*2 = 6)
        assertEquals(GamificationService.POINTS_APP_OPEN + streak.streakBonus, result.totalPunyaPoints)
        assertEquals(today, result.lastAppOpenRewardDate)
        assertEquals(today, result.lastStreakBonusDate)
    }

    @Test
    fun `rewardAppOpen is idempotent within same day`() {
        val data = enabledData { copy(lastAppOpenRewardDate = today, lastStreakBonusDate = today) }
        val streak = StreakData(currentStreak = 5)
        val result = service.rewardAppOpen(data, streak)
        assertSame(data, result)
    }

    @Test
    fun `rewardAppOpen grants points without streak bonus if streak already applied`() {
        val data = enabledData { copy(lastStreakBonusDate = today) }
        val streak = StreakData(currentStreak = 3)
        val result = service.rewardAppOpen(data, streak)

        // Only APP_OPEN points, no streak bonus (already applied today)
        assertEquals(GamificationService.POINTS_APP_OPEN, result.totalPunyaPoints)
        assertEquals(today, result.lastAppOpenRewardDate)
    }

    // ── rewardVerseView ─────────────────────────────────────────────────────

    @Test
    fun `rewardVerseView grants points`() {
        val data = enabledData()
        val result = service.rewardVerseView(data)
        assertEquals(GamificationService.POINTS_VERSE_VIEW, result.totalPunyaPoints)
        assertEquals(today, result.lastVerseViewRewardDate)
    }

    @Test
    fun `rewardVerseView is idempotent within same day`() {
        val data = enabledData { copy(lastVerseViewRewardDate = today) }
        val result = service.rewardVerseView(data)
        assertSame(data, result)
    }

    // ── rewardVerseRead ─────────────────────────────────────────────────────

    @Test
    fun `rewardVerseRead grants points`() {
        val data = enabledData()
        val result = service.rewardVerseRead(data)
        assertEquals(GamificationService.POINTS_VERSE_READ, result.totalPunyaPoints)
        assertEquals(today, result.lastVerseReadRewardDate)
    }

    @Test
    fun `rewardVerseRead is idempotent within same day`() {
        val data = enabledData { copy(lastVerseReadRewardDate = today) }
        val result = service.rewardVerseRead(data)
        assertSame(data, result)
    }

    // ── rewardChallenge ─────────────────────────────────────────────────────

    @Test
    fun `rewardChallenge grants points and increments counter`() {
        val data = enabledData()
        val result = service.rewardChallenge(data)
        assertEquals(GamificationService.POINTS_CHALLENGE, result.totalPunyaPoints)
        assertEquals(today, result.lastChallengeDate)
        assertTrue(result.lastChallengeCompleted)
        assertEquals(1, result.challengesSolved)
    }

    @Test
    fun `rewardChallenge is idempotent if already completed today`() {
        val data = enabledData {
            copy(lastChallengeDate = today, lastChallengeCompleted = true)
        }
        val result = service.rewardChallenge(data)
        assertSame(data, result)
    }

    @Test
    fun `rewardChallenge rewards again on new day`() {
        val data = enabledData {
            copy(lastChallengeDate = yesterday, lastChallengeCompleted = true, challengesSolved = 5)
        }
        val result = service.rewardChallenge(data)
        assertEquals(GamificationService.POINTS_CHALLENGE, result.totalPunyaPoints)
        assertEquals(6, result.challengesSolved)
    }

    // ── rewardFestivalStory ─────────────────────────────────────────────────

    @Test
    fun `rewardFestivalStory grants points and tracks festival`() {
        val data = enabledData()
        val result = service.rewardFestivalStory(data, "diwali")
        assertEquals(GamificationService.POINTS_FESTIVAL_STORY, result.totalPunyaPoints)
        assertTrue("diwali" in result.festivalStoriesRead)
    }

    @Test
    fun `rewardFestivalStory is idempotent for same festival`() {
        val data = enabledData { copy(festivalStoriesRead = setOf("diwali")) }
        val result = service.rewardFestivalStory(data, "diwali")
        assertSame(data, result)
    }

    @Test
    fun `rewardFestivalStory rewards different festivals independently`() {
        val data = enabledData { copy(festivalStoriesRead = setOf("diwali")) }
        val result = service.rewardFestivalStory(data, "holi")
        assertEquals(GamificationService.POINTS_FESTIVAL_STORY, result.totalPunyaPoints)
        assertTrue("diwali" in result.festivalStoriesRead)
        assertTrue("holi" in result.festivalStoriesRead)
    }

    // ── rewardTextCompletion ────────────────────────────────────────────────

    @Test
    fun `rewardTextCompletion grants points and tracks text`() {
        val data = enabledData()
        val result = service.rewardTextCompletion(data, SacredTextType.GITA)
        assertEquals(GamificationService.POINTS_TEXT_COMPLETION, result.totalPunyaPoints)
        assertTrue(SacredTextType.GITA.name in result.textsCompleted)
    }

    @Test
    fun `rewardTextCompletion is idempotent for same text`() {
        val data = enabledData { copy(textsCompleted = setOf(SacredTextType.GITA.name)) }
        val result = service.rewardTextCompletion(data, SacredTextType.GITA)
        assertSame(data, result)
    }

    // ── rewardReflection ────────────────────────────────────────────────────

    @Test
    fun `rewardReflection grants points and increments counter`() {
        val data = enabledData()
        val result = service.rewardReflection(data)
        assertEquals(GamificationService.POINTS_REFLECTION, result.totalPunyaPoints)
        assertEquals(1, result.reflectionsWritten)
    }

    @Test
    fun `rewardReflection accumulates across calls`() {
        val data = enabledData(totalPunyaPoints = 10) { copy(reflectionsWritten = 3) }
        val result = service.rewardReflection(data)
        assertEquals(10 + GamificationService.POINTS_REFLECTION, result.totalPunyaPoints)
        assertEquals(4, result.reflectionsWritten)
    }

    // ── rewardDeepStudy ─────────────────────────────────────────────────────

    @Test
    fun `rewardDeepStudy grants points and increments sessions`() {
        val data = enabledData()
        val result = service.rewardDeepStudy(data)
        assertEquals(GamificationService.POINTS_DEEP_STUDY, result.totalPunyaPoints)
        assertEquals(1, result.deepStudySessions)
        assertEquals(today, result.lastDeepStudyRewardDate)
        assertEquals(GamificationService.POINTS_DEEP_STUDY, result.deepStudyPointsToday)
    }

    @Test
    fun `rewardDeepStudy respects daily cap`() {
        val data = enabledData {
            copy(
                lastDeepStudyRewardDate = today,
                deepStudyPointsToday = GamificationService.MAX_DEEP_STUDY_POINTS_PER_DAY
            )
        }
        val result = service.rewardDeepStudy(data)
        assertSame(data, result)
    }

    @Test
    fun `rewardDeepStudy partial reward near daily cap`() {
        val nearCap = GamificationService.MAX_DEEP_STUDY_POINTS_PER_DAY - 1
        val data = enabledData(totalPunyaPoints = 100) {
            copy(
                lastDeepStudyRewardDate = today,
                deepStudyPointsToday = nearCap,
                deepStudySessions = 5
            )
        }
        val result = service.rewardDeepStudy(data)
        // Only 1 point awarded (cap - current = 50-49 = 1)
        assertEquals(101, result.totalPunyaPoints)
        assertEquals(GamificationService.MAX_DEEP_STUDY_POINTS_PER_DAY, result.deepStudyPointsToday)
        assertEquals(6, result.deepStudySessions)
    }

    @Test
    fun `rewardDeepStudy resets daily counter on new day`() {
        val data = enabledData(totalPunyaPoints = 100) {
            copy(
                lastDeepStudyRewardDate = yesterday,
                deepStudyPointsToday = GamificationService.MAX_DEEP_STUDY_POINTS_PER_DAY
            )
        }
        val result = service.rewardDeepStudy(data)
        // New day, so todayPoints starts at 0
        assertEquals(100 + GamificationService.POINTS_DEEP_STUDY, result.totalPunyaPoints)
        assertEquals(GamificationService.POINTS_DEEP_STUDY, result.deepStudyPointsToday)
    }

    // ── Tracking Methods ────────────────────────────────────────────────────

    @Test
    fun `trackExplanationView increments versesExplained`() {
        val data = enabledData { copy(versesExplained = 5) }
        val result = service.trackExplanationView(data)
        assertEquals(6, result.versesExplained)
    }

    @Test
    fun `trackPanchangCheck increments counter once per day`() {
        val data = enabledData { copy(panchangDaysChecked = 3) }
        val result = service.trackPanchangCheck(data)
        assertEquals(4, result.panchangDaysChecked)
        assertEquals(today, result.lastPanchangCheckDate)
    }

    @Test
    fun `trackPanchangCheck is idempotent within same day`() {
        val data = enabledData { copy(lastPanchangCheckDate = today, panchangDaysChecked = 3) }
        val result = service.trackPanchangCheck(data)
        assertSame(data, result)
    }

    @Test
    fun `trackDharmaPath adds path to explored set`() {
        val data = enabledData()
        val result = service.trackDharmaPath(data, "shaiv")
        assertTrue("shaiv" in result.dharmaPathsExplored)
    }

    @Test
    fun `trackLanguage adds language to used set`() {
        val data = enabledData { copy(languagesUsed = setOf("en")) }
        val result = service.trackLanguage(data, "hi")
        assertEquals(setOf("en", "hi"), result.languagesUsed)
    }

    // ── Badge Checking ──────────────────────────────────────────────────────

    @Test
    fun `checkAndAwardBadges awards streak_7 for 7-day streak`() {
        val data = enabledData()
        val streak = StreakData(longestStreak = 7)
        val (result, newBadges) = service.checkAndAwardBadges(data, streak)
        assertTrue("streak_7" in newBadges)
        assertTrue(result.hasBadge("streak_7"))
    }

    @Test
    fun `checkAndAwardBadges does not re-award existing badges`() {
        val data = enabledData { copy(earnedBadges = listOf("streak_7")) }
        val streak = StreakData(longestStreak = 7)
        val (_, newBadges) = service.checkAndAwardBadges(data, streak)
        assertFalse("streak_7" in newBadges)
    }

    @Test
    fun `checkAndAwardBadges awards multiple badges at once`() {
        val data = enabledData {
            copy(
                festivalStoriesRead = (1..20).map { "festival_$it" }.toSet(),
                panchangDaysChecked = 100,
                challengesSolved = 30
            )
        }
        val streak = StreakData(longestStreak = 100)
        val (result, newBadges) = service.checkAndAwardBadges(data, streak)
        // Should have streak badges (7, 30, 100), festival (5, 20), panchang (7, 30, 100), challenge (7, 30)
        assertTrue("streak_7" in newBadges)
        assertTrue("streak_30" in newBadges)
        assertTrue("streak_100" in newBadges)
        assertTrue("festival_5" in newBadges)
        assertTrue("festival_20" in newBadges)
        assertTrue("panchang_7" in newBadges)
        assertTrue("panchang_30" in newBadges)
        assertTrue("panchang_100" in newBadges)
        assertTrue("challenge_7" in newBadges)
        assertTrue("challenge_30" in newBadges)
        assertTrue(result.earnedBadges.size >= 10)
    }

    // ── Japa & Diya Rewards ─────────────────────────────────────────────────

    @Test
    fun `rewardJapaRound grants points`() {
        val data = enabledData()
        val japa = JapaState(roundsRewardedToday = 0)
        val result = service.rewardJapaRound(data, japa)
        assertEquals(GamificationService.POINTS_JAPA_ROUND, result.totalPunyaPoints)
    }

    @Test
    fun `rewardJapaRound respects daily rounds cap`() {
        val data = enabledData()
        val japa = JapaState(roundsRewardedToday = GamificationService.MAX_JAPA_ROUNDS_REWARDED)
        val result = service.rewardJapaRound(data, japa)
        assertSame(data, result)
    }

    @Test
    fun `rewardDiyaLighting grants points`() {
        val data = enabledData()
        val diya = DiyaState(lastDiyaRewardDate = null, lightingStreak = 3)
        val result = service.rewardDiyaLighting(data, diya)
        assertEquals(GamificationService.POINTS_DIYA_LIGHTING, result.totalPunyaPoints)
    }

    @Test
    fun `rewardDiyaLighting is idempotent within same day`() {
        val data = enabledData()
        val diya = DiyaState(lastDiyaRewardDate = today)
        val result = service.rewardDiyaLighting(data, diya)
        assertSame(data, result)
    }

    @Test
    fun `rewardDiyaLighting grants streak bonus at 7-day multiples`() {
        val data = enabledData()
        val diya = DiyaState(lastDiyaRewardDate = null, lightingStreak = 7)
        val result = service.rewardDiyaLighting(data, diya)
        assertEquals(
            GamificationService.POINTS_DIYA_LIGHTING + GamificationService.POINTS_DIYA_STREAK_BONUS,
            result.totalPunyaPoints
        )
    }

    @Test
    fun `rewardDiyaLighting no streak bonus at non-7 multiples`() {
        val data = enabledData()
        val diya = DiyaState(lastDiyaRewardDate = null, lightingStreak = 5)
        val result = service.rewardDiyaLighting(data, diya)
        assertEquals(GamificationService.POINTS_DIYA_LIGHTING, result.totalPunyaPoints)
    }

    // ── Japa Badge Checking ─────────────────────────────────────────────────

    @Test
    fun `checkJapaBadges awards badge_japa_10`() {
        val data = enabledData()
        val japa = JapaState(totalRoundsLifetime = 10)
        val result = service.checkJapaBadges(data, japa)
        assertTrue(result.hasBadge("badge_japa_10"))
    }

    @Test
    fun `checkJapaBadges awards badge_japa_108`() {
        val data = enabledData()
        val japa = JapaState(totalRoundsLifetime = 108)
        val result = service.checkJapaBadges(data, japa)
        assertTrue(result.hasBadge("badge_japa_10"))
        assertTrue(result.hasBadge("badge_japa_108"))
    }

    @Test
    fun `checkJapaBadges awards streak badges`() {
        val data = enabledData()
        val japa = JapaState(japaStreak = 30)
        val result = service.checkJapaBadges(data, japa)
        assertTrue(result.hasBadge("badge_japa_streak_7"))
        assertTrue(result.hasBadge("badge_japa_streak_30"))
    }

    @Test
    fun `checkJapaBadges does not re-award existing badge`() {
        val data = enabledData { copy(earnedBadges = listOf("badge_japa_10")) }
        val japa = JapaState(totalRoundsLifetime = 10)
        val result = service.checkJapaBadges(data, japa)
        // Should still have exactly one instance
        assertEquals(1, result.earnedBadges.count { it == "badge_japa_10" })
    }

    // ── Diya Badge Checking ─────────────────────────────────────────────────

    @Test
    fun `checkDiyaBadges awards based on totalDaysLit`() {
        val data = enabledData()
        val diya = DiyaState(totalDaysLit = 30, lightingStreak = 7)
        val result = service.checkDiyaBadges(data, diya)
        assertTrue(result.hasBadge("badge_diya_7"))
        assertTrue(result.hasBadge("badge_diya_30"))
        assertTrue(result.hasBadge("badge_diya_streak_7"))
    }

    @Test
    fun `checkDiyaBadges below threshold awards nothing`() {
        val data = enabledData()
        val diya = DiyaState(totalDaysLit = 3, lightingStreak = 2)
        val result = service.checkDiyaBadges(data, diya)
        assertTrue(result.earnedBadges.isEmpty())
    }

    // ── Sanskrit Rewards ────────────────────────────────────────────────────

    @Test
    fun `rewardSanskritLesson grants specified points`() {
        val data = enabledData()
        val result = service.rewardSanskritLesson(data, 25)
        assertEquals(25, result.totalPunyaPoints)
    }

    @Test
    fun `rewardSanskritLetter grants correct points`() {
        val data = enabledData()
        val result = service.rewardSanskritLetter(data)
        assertEquals(GamificationService.POINTS_SANSKRIT_LETTER, result.totalPunyaPoints)
    }

    @Test
    fun `rewardSanskritModule grants correct points`() {
        val data = enabledData()
        val result = service.rewardSanskritModule(data)
        assertEquals(GamificationService.POINTS_SANSKRIT_MODULE, result.totalPunyaPoints)
    }

    @Test
    fun `rewardSanskritVerse grants correct points`() {
        val data = enabledData()
        val result = service.rewardSanskritVerse(data)
        assertEquals(GamificationService.POINTS_SANSKRIT_VERSE, result.totalPunyaPoints)
    }

    // ── Sanskrit Badge Checking ─────────────────────────────────────────────

    @Test
    fun `checkSanskritBadges awards first_letters on module1 completion`() {
        val data = enabledData()
        val progress = SanskritProgress(completedModules = setOf("module1"))
        val result = service.checkSanskritBadges(data, progress)
        assertTrue(result.hasBadge("badge_sanskrit_first_letters"))
    }

    @Test
    fun `checkSanskritBadges awards student on 10 lessons`() {
        val data = enabledData()
        val progress = SanskritProgress(completedLessons = (1..10).map { "lesson_$it" }.toSet())
        val result = service.checkSanskritBadges(data, progress)
        assertTrue(result.hasBadge("badge_sanskrit_student"))
    }

    @Test
    fun `checkSanskritBadges awards scholar on 20 letters`() {
        val data = enabledData()
        val progress = SanskritProgress(masteredLetters = (1..20).map { "letter_$it" }.toSet())
        val result = service.checkSanskritBadges(data, progress)
        assertTrue(result.hasBadge("badge_sanskrit_scholar"))
    }

    @Test
    fun `checkSanskritBadges awards mantra_reader on module5 completion`() {
        val data = enabledData()
        val progress = SanskritProgress(completedModules = setOf("module5"))
        val result = service.checkSanskritBadges(data, progress)
        assertTrue(result.hasBadge("badge_sanskrit_mantra_reader"))
    }

    // ── Level Progression ───────────────────────────────────────────────────

    @Test
    fun `points accumulation triggers level up`() {
        // Level 2 requires 50 points
        val data = enabledData(totalPunyaPoints = 45)
        val result = service.rewardVerseRead(data) // +10 = 55
        assertEquals(55, result.totalPunyaPoints)
        assertEquals(2, result.currentLevel)
    }
}
