package dev.gopes.hinducalendar.domain.model

data class SanskritProgress(
    // ── Existing fields (backward compatible) ───────────────────────────────
    val completedLessons: Set<String> = emptySet(),
    val masteredLetters: Set<String> = emptySet(),
    val completedModules: Set<String> = emptySet(),
    val exploredVerses: Set<String> = emptySet(),
    val lastStudyDate: String? = null,

    // ── Kāṇḍa-level tracking ────────────────────────────────────────────────
    val completedKandas: Set<String> = emptySet(),
    val currentKandaId: String = "kanda_1",

    // ── Vocabulary mastery (Kāṇḍa 2+) ──────────────────────────────────────
    val masteredWords: Set<String> = emptySet(),
    val knownDhatus: Set<String> = emptySet(),

    // ── Grammar mastery (Kāṇḍa 3-5) ────────────────────────────────────────
    val masteredVibhaktis: Set<String> = emptySet(),
    val masteredSandhiRules: Set<String> = emptySet(),
    val masteredParadigms: Set<String> = emptySet(),

    // ── Reading progress (Kāṇḍa 6-7) ───────────────────────────────────────
    val guidedReadingsCompleted: Set<String> = emptySet(),

    // ── Milestones ──────────────────────────────────────────────────────────
    val earnedMilestones: Set<String> = emptySet(),

    // ── Tapas Points & Study streak ─────────────────────────────────────────
    val tapasPoints: Int = 0,
    val studyStreak: Int = 0,
    val longestStudyStreak: Int = 0,

    // ── Daily ritual tracking ───────────────────────────────────────────────
    val morningMantraDate: String? = null,
    val revisionRoundDate: String? = null,
    val eveningReflectionDate: String? = null
) {
    val lessonsCount: Int get() = completedLessons.size
    val lettersCount: Int get() = masteredLetters.size
    val modulesCount: Int get() = completedModules.size
    val wordsCount: Int get() = masteredWords.size
    val kandasCount: Int get() = completedKandas.size

    fun isLessonComplete(lessonId: String): Boolean = lessonId in completedLessons
    fun isModuleComplete(moduleId: String): Boolean = moduleId in completedModules
    fun isLetterMastered(letterId: String): Boolean = letterId in masteredLetters
    fun isVerseExplored(verseId: String): Boolean = verseId in exploredVerses
    fun isKandaComplete(kandaId: String): Boolean = kandaId in completedKandas
    fun hasMilestone(milestoneId: String): Boolean = milestoneId in earnedMilestones
}
