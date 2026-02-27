package dev.gopes.hinducalendar.domain.model

data class SanskritProgress(
    val completedLessons: Set<String> = emptySet(),
    val masteredLetters: Set<String> = emptySet(),
    val completedModules: Set<String> = emptySet(),
    val exploredVerses: Set<String> = emptySet(),
    val lastStudyDate: String? = null
) {
    val lessonsCount: Int get() = completedLessons.size
    val lettersCount: Int get() = masteredLetters.size
    val modulesCount: Int get() = completedModules.size

    fun isLessonComplete(lessonId: String): Boolean = lessonId in completedLessons
    fun isModuleComplete(moduleId: String): Boolean = moduleId in completedModules
    fun isLetterMastered(letterId: String): Boolean = letterId in masteredLetters
    fun isVerseExplored(verseId: String): Boolean = verseId in exploredVerses
}
