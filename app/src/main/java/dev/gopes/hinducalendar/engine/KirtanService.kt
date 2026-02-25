package dev.gopes.hinducalendar.engine

import android.content.Context
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gopes.hinducalendar.data.model.Kirtan
import dev.gopes.hinducalendar.data.model.KirtanCategory
import dev.gopes.hinducalendar.data.model.KirtanData
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads and caches kirtans from kirtans.json in assets.
 */
@Singleton
class KirtanService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()

    @Volatile
    private var cache: KirtanData? = null

    fun loadKirtans(): KirtanData? {
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

    fun allKirtans(): List<Kirtan> = loadKirtans()?.kirtans ?: emptyList()

    fun kirtanById(id: String): Kirtan? = allKirtans().find { it.id == id }

    fun kirtansByCategory(category: KirtanCategory): List<Kirtan> =
        allKirtans().filter { it.category == category }

    val totalKirtans: Int get() = allKirtans().size
}
