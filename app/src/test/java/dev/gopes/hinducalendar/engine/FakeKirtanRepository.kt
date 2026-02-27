package dev.gopes.hinducalendar.engine

import dev.gopes.hinducalendar.domain.repository.KirtanRepository

import dev.gopes.hinducalendar.domain.model.Kirtan
import dev.gopes.hinducalendar.domain.model.KirtanCategory
import dev.gopes.hinducalendar.domain.model.KirtanData

/**
 * In-memory fake for unit testing ViewModels that depend on [KirtanRepository].
 */
class FakeKirtanRepository : KirtanRepository {

    var kirtanData: KirtanData? = null
    private val kirtans = mutableListOf<Kirtan>()

    fun addKirtan(kirtan: Kirtan) {
        kirtans.add(kirtan)
    }

    override fun loadKirtans(): KirtanData? = kirtanData

    override fun allKirtans(): List<Kirtan> = kirtans.toList()

    override fun kirtanById(id: String): Kirtan? = kirtans.find { it.id == id }

    override fun kirtansByCategory(category: KirtanCategory): List<Kirtan> =
        kirtans.filter { it.category == category }

    override val totalKirtans: Int get() = kirtans.size
}
