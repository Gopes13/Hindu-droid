package dev.gopes.hinducalendar.domain.repository

import dev.gopes.hinducalendar.domain.model.SanskritKanda
import dev.gopes.hinducalendar.domain.model.SanskritModule
import dev.gopes.hinducalendar.domain.model.SanskritLesson

/**
 * Contract for loading Sanskrit Pathshala curriculum data.
 * Backed by [SanskritCurriculumService] in production.
 */
interface SanskritRepository {
    fun loadKandas(): List<SanskritKanda>
    fun kandaById(id: String): SanskritKanda?
    fun moduleById(kandaId: String, moduleId: String): SanskritModule?
    fun lessonById(lessonId: String): SanskritLesson?
    val totalKandas: Int
    val totalModules: Int
    val totalLessons: Int
}
