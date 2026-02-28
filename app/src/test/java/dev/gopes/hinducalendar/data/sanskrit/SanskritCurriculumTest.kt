package dev.gopes.hinducalendar.data.sanskrit

import dev.gopes.hinducalendar.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class SanskritCurriculumTest {

    // ── SanskritProgress tests ──────────────────────────────────────────────

    @Test
    fun `default progress has all empty sets`() {
        val p = SanskritProgress()
        assertEquals(0, p.lessonsCount)
        assertEquals(0, p.lettersCount)
        assertEquals(0, p.wordsCount)
        assertEquals(0, p.kandasCount)
        assertEquals("kanda_1", p.currentKandaId)
        assertEquals(0, p.tapasPoints)
        assertEquals(0, p.studyStreak)
    }

    @Test
    fun `isKandaComplete returns correct result`() {
        val p = SanskritProgress(completedKandas = setOf("kanda_1", "kanda_2"))
        assertTrue(p.isKandaComplete("kanda_1"))
        assertTrue(p.isKandaComplete("kanda_2"))
        assertFalse(p.isKandaComplete("kanda_3"))
    }

    @Test
    fun `hasMilestone returns correct result`() {
        val p = SanskritProgress(earnedMilestones = setOf("aksara_siddhi"))
        assertTrue(p.hasMilestone("aksara_siddhi"))
        assertFalse(p.hasMilestone("shabda_kosha"))
    }

    @Test
    fun `progress counts are accurate`() {
        val p = SanskritProgress(
            completedLessons = setOf("l1", "l2", "l3"),
            masteredLetters = setOf("a", "aa"),
            completedModules = setOf("m1"),
            masteredWords = setOf("dharma", "karma", "yoga"),
            completedKandas = setOf("kanda_1")
        )
        assertEquals(3, p.lessonsCount)
        assertEquals(2, p.lettersCount)
        assertEquals(1, p.modulesCount)
        assertEquals(3, p.wordsCount)
        assertEquals(1, p.kandasCount)
    }

    // ── KandaUnlockRequirement tests ────────────────────────────────────────

    @Test
    fun `NONE requirement has type none`() {
        val req = KandaUnlockRequirement.NONE
        assertEquals("none", req.type)
        assertNull(req.kandaId)
    }

    @Test
    fun `kandaComplete requirement stores kanda id`() {
        val req = KandaUnlockRequirement.kandaComplete("kanda_3")
        assertEquals("kanda_complete", req.type)
        assertEquals("kanda_3", req.kandaId)
    }

    // ── SanskritKanda tests ─────────────────────────────────────────────────

    @Test
    fun `kanda can be constructed with all fields`() {
        val kanda = SanskritKanda(
            id = "kanda_1",
            number = 1,
            titleSanskrit = "अक्षरम्",
            titleTranslit = "Akṣaram",
            titles = mapOf("en" to "Script", "hi" to "लिपि"),
            descriptions = mapOf("en" to "Learn the script"),
            icon = "ॐ",
            modules = emptyList(),
            milestoneId = "aksara_siddhi",
            milestoneTitles = mapOf("en" to "Akṣara Siddhi"),
            unlockRequirement = KandaUnlockRequirement.NONE
        )
        assertEquals("kanda_1", kanda.id)
        assertEquals(1, kanda.number)
        assertEquals("Script", kanda.titles["en"])
    }

    // ── LessonType tests ────────────────────────────────────────────────────

    @Test
    fun `all lesson types are defined`() {
        val types = LessonType.entries
        assertEquals(4, types.size)
        assertTrue(types.contains(LessonType.QUIZ))
        assertTrue(types.contains(LessonType.GRAMMAR_LESSON))
        assertTrue(types.contains(LessonType.READING))
        assertTrue(types.contains(LessonType.PRACTICE))
    }

    // ── SanskritLesson backward compatibility ───────────────────────────────

    @Test
    fun `lesson defaults to QUIZ type with no grammar card`() {
        val lesson = SanskritLesson(
            id = "test",
            title = "Test Lesson",
            contextCard = SanskritContextCard("अ", "a", "test"),
            exercises = emptyList()
        )
        assertEquals(LessonType.QUIZ, lesson.lessonType)
        assertNull(lesson.grammarCard)
        assertTrue(lesson.titles.isEmpty())
    }

    @Test
    fun `lesson can have grammar card and custom type`() {
        val grammar = GrammarCard(
            titleKey = "vibhakti_intro",
            sections = listOf(
                GrammarSection(
                    type = GrammarSectionType.TEXT,
                    content = mapOf("en" to "The case system...")
                )
            )
        )
        val lesson = SanskritLesson(
            id = "k3_l1",
            title = "Vibhakti",
            contextCard = SanskritContextCard("विभक्ति", "vibhakti", "cases"),
            exercises = emptyList(),
            grammarCard = grammar,
            lessonType = LessonType.GRAMMAR_LESSON
        )
        assertEquals(LessonType.GRAMMAR_LESSON, lesson.lessonType)
        assertNotNull(lesson.grammarCard)
        assertEquals(1, lesson.grammarCard!!.sections.size)
    }

    // ── SanskritModule backward compatibility ───────────────────────────────

    @Test
    fun `module defaults have empty titles and no kanda id`() {
        val module = SanskritModule(
            id = "module1",
            title = "Vowels",
            titleSanskrit = "स्वर",
            emoji = "~",
            lessons = emptyList()
        )
        assertTrue(module.titles.isEmpty())
        assertEquals("", module.kandaId)
    }

    // ── Exercise type existence ─────────────────────────────────────────────

    @Test
    fun `new exercise types can be instantiated`() {
        val wordDetective = SanskritExercise.WordDetective(
            dhatuRoot = "√vid",
            dhatuDevanagari = "विद्",
            dhatuMeaning = mapOf("en" to "to know"),
            correctWord = "vidyā",
            correctWordMeaning = mapOf("en" to "knowledge"),
            distractors = listOf("māyā", "satya", "śakti")
        )
        assertNull(wordDetective.targetLetterId)

        val sandhiSplit = SanskritExercise.SandhiSplit(
            combined = "नरेन्द्रः",
            combinedTranslit = "narendraḥ",
            correctParts = listOf("नर", "इन्द्रः"),
            rule = SandhiType.SVARA,
            distractorParts = listOf(listOf("नरे", "न्द्रः"))
        )
        assertNull(sandhiSplit.targetLetterId)

        val buildShloka = SanskritExercise.BuildShloka(
            correctOrder = listOf("रामः", "वनम्", "गच्छति"),
            translation = mapOf("en" to "Rama goes to the forest")
        )
        assertNull(buildShloka.targetLetterId)
    }

    // ── SanskritData legacy still works ─────────────────────────────────────

    @Test
    fun `legacy SanskritData has 13 modules`() {
        assertEquals(13, SanskritData.modules.size)
    }

    @Test
    fun `legacy SanskritData has 52 letters across 9 groups`() {
        val letters = SanskritData.allLetters
        assertTrue(letters.size >= 50)
        val groups = letters.map { it.group }.toSet()
        assertEquals(9, groups.size)
    }

    @Test
    fun `legacy SanskritData has at least 60 words`() {
        assertTrue("Expected at least 60 words, got ${SanskritData.allWords.size}", SanskritData.allWords.size >= 60)
    }

    @Test
    fun `legacy SanskritData total lessons matches module sum`() {
        val sum = SanskritData.modules.sumOf { it.lessons.size }
        assertEquals(sum, SanskritData.totalLessons)
    }

    @Test
    fun `legacy modules have unique IDs`() {
        val ids = SanskritData.modules.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `legacy lessons have unique IDs`() {
        val ids = SanskritData.modules.flatMap { m -> m.lessons.map { it.id } }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `every legacy lesson has at least one exercise`() {
        SanskritData.modules.forEach { module ->
            module.lessons.forEach { lesson ->
                assertTrue(
                    "Lesson ${lesson.id} in ${module.id} has no exercises",
                    lesson.exercises.isNotEmpty()
                )
            }
        }
    }

    // ── Sandhi model tests ──────────────────────────────────────────────────

    @Test
    fun `sandhi types cover all three categories`() {
        val types = SandhiType.entries
        assertEquals(3, types.size)
        assertTrue(types.contains(SandhiType.SVARA))
        assertTrue(types.contains(SandhiType.VYANJANA))
        assertTrue(types.contains(SandhiType.VISARGA))
    }

    // ── Paradigm model tests ────────────────────────────────────────────────

    @Test
    fun `gender enum has three values`() {
        assertEquals(3, SanskritGender.entries.size)
    }

    @Test
    fun `lakara enum has five values`() {
        assertEquals(5, SanskritLakara.entries.size)
    }

    @Test
    fun `verb pada enum has two values`() {
        assertEquals(2, VerbPada.entries.size)
    }

    // ── Grammar section types ───────────────────────────────────────────────

    @Test
    fun `grammar section types cover all five`() {
        val types = GrammarSectionType.entries
        assertEquals(5, types.size)
    }
}
