package dev.gopes.hinducalendar.engine

import dev.gopes.hinducalendar.data.model.*
import java.time.LocalDate
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class GamificationService @Inject constructor() {

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

    private fun generatePanchangChallenge(day: Int, rng: Random): DailyChallenge {
        val useTithi = day % 2 == 0
        if (useTithi) {
            val tithis = listOf(
                "Pratipada", "Dwitiya", "Tritiya", "Chaturthi", "Panchami",
                "Shashthi", "Saptami", "Ashtami", "Navami", "Dashami",
                "Ekadashi", "Dwadashi", "Trayodashi", "Chaturdashi", "Purnima", "Amavasya"
            )
            val correctIdx = day % tithis.size
            val correct = tithis[correctIdx]
            val ordinal = correctIdx + 1
            val question = "Which Tithi is the ${ordinal}${ordinalSuffix(ordinal)} lunar day?"
            val options = generateOptions(correct, tithis, rng)
            return DailyChallenge(
                id = "panchang_$day",
                type = ChallengeType.PANCHANG_EXPLORER,
                question = question,
                options = options.first,
                correctOptionIndex = options.second
            )
        } else {
            val nakshatras = listOf(
                "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashira",
                "Ardra", "Punarvasu", "Pushya", "Ashlesha", "Magha",
                "Purva Phalguni", "Uttara Phalguni", "Hasta", "Chitra", "Swati",
                "Vishakha", "Anuradha", "Jyeshtha", "Mula", "Purvashadha",
                "Uttarashadha", "Shravana", "Dhanishta", "Shatabhisha",
                "Purva Bhadrapada", "Uttara Bhadrapada", "Revati"
            )
            val correctIdx = day % nakshatras.size
            val correct = nakshatras[correctIdx]
            val ordinal = correctIdx + 1
            val question = "Which is the ${ordinal}${ordinalSuffix(ordinal)} Nakshatra in the zodiac?"
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

    private fun generateFestivalChallenge(day: Int, rng: Random): DailyChallenge {
        data class FestivalQ(val festival: String, val question: String, val answer: String, val wrong: List<String>)
        val festivals = listOf(
            FestivalQ("Diwali", "What does Diwali celebrate?", "Victory of light over darkness", listOf("Harvest season", "New Year only", "Spring equinox")),
            FestivalQ("Holi", "Holi is also known as the festival of?", "Colors", listOf("Lights", "Music", "Water")),
            FestivalQ("Navratri", "How many nights does Navratri span?", "Nine", listOf("Seven", "Five", "Ten")),
            FestivalQ("Raksha Bandhan", "What does a sister tie on her brother's wrist?", "Rakhi (sacred thread)", listOf("Flower garland", "Gold chain", "Silk ribbon")),
            FestivalQ("Janmashtami", "Janmashtami celebrates the birth of?", "Lord Krishna", listOf("Lord Rama", "Lord Shiva", "Lord Ganesha")),
            FestivalQ("Ganesh Chaturthi", "How many days does Ganesh Chaturthi typically last?", "10 days", listOf("7 days", "5 days", "3 days")),
            FestivalQ("Maha Shivaratri", "Maha Shivaratri is dedicated to?", "Lord Shiva", listOf("Lord Vishnu", "Lord Brahma", "Goddess Durga")),
            FestivalQ("Makar Sankranti", "Makar Sankranti marks the sun's entry into?", "Capricorn (Makar)", listOf("Aries (Mesh)", "Cancer (Karka)", "Libra (Tula)")),
            FestivalQ("Baisakhi", "Baisakhi is significant for which community?", "Sikh community", listOf("Buddhist community", "Jain community", "Parsi community")),
            FestivalQ("Ram Navami", "Ram Navami celebrates the birth of?", "Lord Rama", listOf("Lord Krishna", "Lord Hanuman", "Lord Vishnu")),
            FestivalQ("Hanuman Jayanti", "Hanuman is known as the devotee of?", "Lord Rama", listOf("Lord Shiva", "Lord Krishna", "Lord Vishnu")),
            FestivalQ("Guru Purnima", "Guru Purnima honors?", "Spiritual teachers", listOf("Harvest gods", "Ancestors", "Warriors"))
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
        data class VerseQ(val text: String, val question: String, val answer: String, val wrong: List<String>)
        val verses = listOf(
            VerseQ("Bhagavad Gita", "In the Gita, who is Krishna's charioteer role for?", "Arjuna", listOf("Bhishma", "Duryodhana", "Karna")),
            VerseQ("Hanuman Chalisa", "How many verses (chaupais) are in the Hanuman Chalisa?", "40", listOf("30", "50", "60")),
            VerseQ("Japji Sahib", "Who composed Japji Sahib?", "Guru Nanak Dev Ji", listOf("Guru Gobind Singh Ji", "Guru Arjan Dev Ji", "Guru Angad Dev Ji")),
            VerseQ("Vishnu Sahasranama", "How many names of Vishnu are in the Sahasranama?", "1000", listOf("500", "108", "1008")),
            VerseQ("Sri Rudram", "Sri Rudram is part of which Veda?", "Yajur Veda", listOf("Rig Veda", "Sama Veda", "Atharva Veda")),
            VerseQ("Devi Mahatmya", "Devi Mahatmya describes the triumph of?", "Goddess Durga", listOf("Goddess Lakshmi", "Goddess Saraswati", "Goddess Parvati")),
            VerseQ("Soundarya Lahari", "Who is traditionally credited with composing Soundarya Lahari?", "Adi Shankaracharya", listOf("Tulsidas", "Valmiki", "Vyasa")),
            VerseQ("Sukhmani Sahib", "Sukhmani Sahib was composed by?", "Guru Arjan Dev Ji", listOf("Guru Nanak Dev Ji", "Guru Gobind Singh Ji", "Guru Tegh Bahadur Ji"))
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
        data class MantraQ(val mantra: String, val question: String, val answer: String, val wrong: List<String>)
        val mantras = listOf(
            MantraQ("Om Namah Shivaya", "Om Namah Shivaya is dedicated to?", "Lord Shiva", listOf("Lord Vishnu", "Lord Brahma", "Lord Ganesha")),
            MantraQ("Om Namo Narayanaya", "Narayana is another name for?", "Lord Vishnu", listOf("Lord Shiva", "Lord Indra", "Lord Brahma")),
            MantraQ("Om Gan Ganapataye Namah", "This mantra invokes?", "Lord Ganesha", listOf("Lord Kartikeya", "Lord Hanuman", "Lord Rama")),
            MantraQ("Gayatri Mantra", "The Gayatri Mantra appears in which Veda?", "Rig Veda", listOf("Yajur Veda", "Sama Veda", "Atharva Veda")),
            MantraQ("Mahamrityunjaya", "The Mahamrityunjaya mantra is addressed to?", "Lord Shiva (Tryambaka)", listOf("Lord Vishnu", "Lord Yama", "Lord Agni")),
            MantraQ("Om Aim Hreem Kleem", "Bija mantras like Aim, Hreem, Kleem are associated with?", "Goddess energy (Shakti)", listOf("Planetary energy", "Elemental energy", "Vedic hymns")),
            MantraQ("Hare Krishna", "The Hare Krishna Maha Mantra has how many words?", "16", listOf("12", "18", "8")),
            MantraQ("Ik Onkar", "Ik Onkar is the opening of?", "Guru Granth Sahib", listOf("Bhagavad Gita", "Ramayana", "Vedas"))
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

    private fun ordinalSuffix(n: Int): String = when {
        n % 100 in 11..13 -> "th"
        n % 10 == 1 -> "st"
        n % 10 == 2 -> "nd"
        n % 10 == 3 -> "rd"
        else -> "th"
    }
}
