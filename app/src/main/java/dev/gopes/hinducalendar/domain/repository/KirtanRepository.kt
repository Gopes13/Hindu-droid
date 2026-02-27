package dev.gopes.hinducalendar.domain.repository

import dev.gopes.hinducalendar.domain.model.Kirtan
import dev.gopes.hinducalendar.domain.model.KirtanCategory
import dev.gopes.hinducalendar.domain.model.KirtanData

/**
 * Contract for loading and querying kirtan data.
 * Backed by [KirtanService] in production; fake in tests.
 */
interface KirtanRepository {
    fun loadKirtans(): KirtanData?
    fun allKirtans(): List<Kirtan>
    fun kirtanById(id: String): Kirtan?
    fun kirtansByCategory(category: KirtanCategory): List<Kirtan>
    val totalKirtans: Int
}
