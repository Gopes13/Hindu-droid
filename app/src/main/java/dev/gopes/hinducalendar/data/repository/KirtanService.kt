package dev.gopes.hinducalendar.data.repository

import android.content.Context
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gopes.hinducalendar.domain.model.Kirtan
import dev.gopes.hinducalendar.domain.model.KirtanCategory
import dev.gopes.hinducalendar.domain.model.KirtanData
import dev.gopes.hinducalendar.domain.repository.KirtanRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads and caches kirtans from kirtans.json in assets.
 */
@Singleton
class KirtanService @Inject constructor(
    @ApplicationContext private val context: Context
) : KirtanRepository {
    private val gson = Gson()

    @Volatile
    private var cache: KirtanData? = null

    override fun loadKirtans(): KirtanData? {
        cache?.let { return it }
        return try {
            val json = context.assets.open("kirtans.json").bufferedReader().use { it.readText() }
            val data = gson.fromJson(json, KirtanData::class.java)
            cache = data
            data
        } catch (e: Exception) {
            null
        }
    }

    override fun allKirtans(): List<Kirtan> = loadKirtans()?.kirtans ?: emptyList()

    override fun kirtanById(id: String): Kirtan? = allKirtans().find { it.id == id }

    override fun kirtansByCategory(category: KirtanCategory): List<Kirtan> =
        allKirtans().filter { it.category == category }

    override val totalKirtans: Int get() = allKirtans().size
}
