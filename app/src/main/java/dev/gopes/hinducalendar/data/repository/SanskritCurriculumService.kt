package dev.gopes.hinducalendar.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gopes.hinducalendar.domain.model.*
import dev.gopes.hinducalendar.domain.repository.SanskritRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads and caches Sanskrit Pathshala curriculum data.
 *
 * Kāṇḍa 1 is built from the existing [SanskritData] object (legacy modules).
 * Kāṇḍas 2-7 are loaded from JSON files in assets/sanskrit/.
 */
@Singleton
class SanskritCurriculumService @Inject constructor(
    @ApplicationContext private val context: Context
) : SanskritRepository {

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(SanskritExercise::class.java, SanskritExerciseTypeAdapter())
        .create()

    @Volatile
    private var kandaCache: List<SanskritKanda>? = null

    override fun loadKandas(): List<SanskritKanda> {
        kandaCache?.let { return it }
        val kandas = buildKandaList()
        kandaCache = kandas
        return kandas
    }

    override fun kandaById(id: String): SanskritKanda? =
        loadKandas().find { it.id == id }

    override fun moduleById(kandaId: String, moduleId: String): SanskritModule? =
        kandaById(kandaId)?.modules?.find { it.id == moduleId }

    override fun lessonById(lessonId: String): SanskritLesson? {
        for (kanda in loadKandas()) {
            for (module in kanda.modules) {
                val lesson = module.lessons.find { it.id == lessonId }
                if (lesson != null) return lesson
            }
        }
        return null
    }

    override val totalKandas: Int get() = loadKandas().size

    override val totalModules: Int get() = loadKandas().sumOf { it.modules.size }

    override val totalLessons: Int get() =
        loadKandas().sumOf { k -> k.modules.sumOf { it.lessons.size } }

    // ── Build Kāṇḍa list ───────────────────────────────────────────────────

    private fun buildKandaList(): List<SanskritKanda> {
        val kanda1 = buildKanda1FromLegacy()
        val kanda2 = loadKandaFromJson("sanskrit/kanda_2_shabdam.json", kanda2Metadata())
        val kanda3 = loadKandaFromJson("sanskrit/kanda_3_vakyam.json", kanda3Metadata())
        val kanda4 = loadKandaFromJson("sanskrit/kanda_4_sandhi.json", kanda4Metadata())
        val kanda5 = loadKandaFromJson("sanskrit/kanda_5_rupam.json", kanda5Metadata())
        val kanda6 = loadKandaFromJson("sanskrit/kanda_6_pathah.json", kanda6Metadata())
        val kanda7 = loadKandaFromJson("sanskrit/kanda_7_svadhyayah.json", kanda7Metadata())
        return listOf(kanda1, kanda2, kanda3, kanda4, kanda5, kanda6, kanda7)
    }

    /**
     * Kāṇḍa 1 wraps the existing 13 modules from [SanskritData].
     * Module IDs are preserved for backward-compatible progress tracking.
     */
    private fun buildKanda1FromLegacy(): SanskritKanda {
        val legacyModules = SanskritData.modules.map { it.copy(kandaId = "kanda_1") }
        return SanskritKanda(
            id = "kanda_1",
            number = 1,
            titleSanskrit = "\u0905\u0915\u094D\u0937\u0930\u092E\u094D",
            titleTranslit = "Ak\u1E63aram",
            titles = mapOf(
                "en" to "The Imperishable Letter",
                "hi" to "\u0905\u0915\u094D\u0937\u0930 \u0938\u093F\u0926\u094D\u0927\u093F"
            ),
            descriptions = mapOf(
                "en" to "Master the Devan\u0101gar\u012B script and Sanskrit sound system",
                "hi" to "\u0926\u0947\u0935\u0928\u093E\u0917\u0930\u0940 \u0932\u093F\u092A\u093F \u0914\u0930 \u0938\u0902\u0938\u094D\u0915\u0943\u0924 \u0927\u094D\u0935\u0928\u093F \u092A\u094D\u0930\u0923\u093E\u0932\u0940 \u092E\u0947\u0902 \u092E\u0939\u093E\u0930\u0924 \u0939\u093E\u0938\u093F\u0932 \u0915\u0930\u0947\u0902"
            ),
            icon = "\u0950",
            modules = legacyModules,
            milestoneId = "aksara_siddhi",
            milestoneTitles = mapOf(
                "en" to "Ak\u1E63ara Siddhi",
                "hi" to "\u0905\u0915\u094D\u0937\u0930 \u0938\u093F\u0926\u094D\u0927\u093F"
            ),
            unlockRequirement = KandaUnlockRequirement.NONE
        )
    }

    /**
     * Loads a Kanda's modules from a JSON asset file.
     * Falls back to empty modules if the file doesn't exist yet.
     */
    private fun loadKandaFromJson(assetPath: String, metadata: SanskritKanda): SanskritKanda {
        return try {
            val json = context.assets.open(assetPath).bufferedReader().use { it.readText() }
            val type = object : TypeToken<KandaJsonData>() {}.type
            val data: KandaJsonData = gson.fromJson(json, type)
            metadata.copy(modules = data.modules.map { it.copy(kandaId = metadata.id) })
        } catch (_: Exception) {
            metadata
        }
    }

    // ── Kāṇḍa metadata ─────────────────────────────────────────────────────

    private fun kanda2Metadata() = SanskritKanda(
        id = "kanda_2", number = 2,
        titleSanskrit = "\u0936\u092C\u094D\u0926\u092E\u094D",
        titleTranslit = "\u015Aabdam",
        titles = mapOf("en" to "The Sacred Word", "hi" to "\u092A\u0935\u093F\u0924\u094D\u0930 \u0936\u092C\u094D\u0926"),
        descriptions = mapOf("en" to "Build foundational vocabulary and understand word formation", "hi" to "\u092E\u0942\u0932 \u0936\u092C\u094D\u0926\u093E\u0935\u0932\u0940 \u092C\u0928\u093E\u090F\u0902 \u0914\u0930 \u0936\u092C\u094D\u0926 \u0928\u093F\u0930\u094D\u092E\u093E\u0923 \u0938\u092E\u091D\u0947\u0902"),
        icon = "\uD83D\uDCDA", modules = emptyList(),
        milestoneId = "shabda_kosha",
        milestoneTitles = mapOf("en" to "\u015Aabda Ko\u1E63a", "hi" to "\u0936\u092C\u094D\u0926 \u0915\u094B\u0936"),
        unlockRequirement = KandaUnlockRequirement.kandaComplete("kanda_1")
    )

    private fun kanda3Metadata() = SanskritKanda(
        id = "kanda_3", number = 3,
        titleSanskrit = "\u0935\u093E\u0915\u094D\u092F\u092E\u094D",
        titleTranslit = "V\u0101kyam",
        titles = mapOf("en" to "The Living Sentence", "hi" to "\u091C\u0940\u0935\u0928\u094D\u0924 \u0935\u093E\u0915\u094D\u092F"),
        descriptions = mapOf("en" to "Understand sentence structure, vibhakti, and basic grammar", "hi" to "\u0935\u093E\u0915\u094D\u092F \u0938\u0902\u0930\u091A\u0928\u093E, \u0935\u093F\u092D\u0915\u094D\u0924\u093F \u0914\u0930 \u092E\u0942\u0932 \u0935\u094D\u092F\u093E\u0915\u0930\u0923 \u0938\u092E\u091D\u0947\u0902"),
        icon = "\uD83D\uDCDC", modules = emptyList(),
        milestoneId = "vakyakara",
        milestoneTitles = mapOf("en" to "V\u0101kyak\u0101ra", "hi" to "\u0935\u093E\u0915\u094D\u092F\u0915\u093E\u0930"),
        unlockRequirement = KandaUnlockRequirement.kandaComplete("kanda_2")
    )

    private fun kanda4Metadata() = SanskritKanda(
        id = "kanda_4", number = 4,
        titleSanskrit = "\u0938\u0928\u094D\u0927\u093F\u0903",
        titleTranslit = "Sandhi\u1E25",
        titles = mapOf("en" to "The Sacred Junction", "hi" to "\u092A\u0935\u093F\u0924\u094D\u0930 \u0938\u0928\u094D\u0927\u093F"),
        descriptions = mapOf("en" to "Master sandhi rules \u2014 the key that unlocks reading real texts", "hi" to "\u0938\u0928\u094D\u0927\u093F \u0928\u093F\u092F\u092E\u094B\u0902 \u092E\u0947\u0902 \u092E\u0939\u093E\u0930\u0924 \u0939\u093E\u0938\u093F\u0932 \u0915\u0930\u0947\u0902"),
        icon = "\uD83D\uDD17", modules = emptyList(),
        milestoneId = "sandhi_vaidya",
        milestoneTitles = mapOf("en" to "Sandhi Vaidya", "hi" to "\u0938\u0928\u094D\u0927\u093F \u0935\u0948\u0926\u094D\u092F"),
        unlockRequirement = KandaUnlockRequirement.kandaComplete("kanda_3")
    )

    private fun kanda5Metadata() = SanskritKanda(
        id = "kanda_5", number = 5,
        titleSanskrit = "\u0930\u0942\u092A\u092E\u094D",
        titleTranslit = "R\u016Bpam",
        titles = mapOf("en" to "The Form", "hi" to "\u0930\u0942\u092A"),
        descriptions = mapOf("en" to "Master full noun declension and verb conjugation systems", "hi" to "\u0938\u092E\u094D\u092A\u0942\u0930\u094D\u0923 \u0936\u092C\u094D\u0926 \u0930\u0942\u092A \u0914\u0930 \u0927\u093E\u0924\u0941 \u0930\u0942\u092A \u092A\u094D\u0930\u0923\u093E\u0932\u0940"),
        icon = "\uD83D\uDCD0", modules = emptyList(),
        milestoneId = "rupa_siddhi",
        milestoneTitles = mapOf("en" to "R\u016Bpa Siddhi", "hi" to "\u0930\u0942\u092A \u0938\u093F\u0926\u094D\u0927\u093F"),
        unlockRequirement = KandaUnlockRequirement.kandaComplete("kanda_4")
    )

    private fun kanda6Metadata() = SanskritKanda(
        id = "kanda_6", number = 6,
        titleSanskrit = "\u092A\u093E\u0920\u0903",
        titleTranslit = "P\u0101\u1E6Dha\u1E25",
        titles = mapOf("en" to "The Reading", "hi" to "\u092A\u093E\u0920"),
        descriptions = mapOf("en" to "Read and comprehend real Sanskrit texts with support", "hi" to "\u0935\u093E\u0938\u094D\u0924\u0935\u093F\u0915 \u0938\u0902\u0938\u094D\u0915\u0943\u0924 \u0917\u094D\u0930\u0902\u0925 \u092A\u0922\u093C\u0947\u0902 \u0914\u0930 \u0938\u092E\u091D\u0947\u0902"),
        icon = "\uD83D\uDCD6", modules = emptyList(),
        milestoneId = "gita_pathaka",
        milestoneTitles = mapOf("en" to "G\u012Bt\u0101 P\u0101\u1E6Dhaka", "hi" to "\u0917\u0940\u0924\u093E \u092A\u093E\u0920\u0915"),
        unlockRequirement = KandaUnlockRequirement.kandaComplete("kanda_5")
    )

    private fun kanda7Metadata() = SanskritKanda(
        id = "kanda_7", number = 7,
        titleSanskrit = "\u0938\u094D\u0935\u093E\u0927\u094D\u092F\u093E\u092F\u0903",
        titleTranslit = "Sv\u0101dhy\u0101ya\u1E25",
        titles = mapOf("en" to "Self-Study", "hi" to "\u0938\u094D\u0935\u093E\u0927\u094D\u092F\u093E\u092F"),
        descriptions = mapOf("en" to "Independent reading proficiency and lifelong learning tools", "hi" to "\u0938\u094D\u0935\u0924\u0928\u094D\u0924\u094D\u0930 \u092A\u0920\u0928 \u0926\u0915\u094D\u0937\u0924\u093E \u0914\u0930 \u0906\u091C\u0940\u0935\u0928 \u0936\u093F\u0915\u094D\u0937\u093E"),
        icon = "\uD83E\uDDD8", modules = emptyList(),
        milestoneId = "svadhyaya_siddha",
        milestoneTitles = mapOf("en" to "Sv\u0101dhy\u0101ya Siddha", "hi" to "\u0938\u094D\u0935\u093E\u0927\u094D\u092F\u093E\u092F \u0938\u093F\u0926\u094D\u0927"),
        unlockRequirement = KandaUnlockRequirement.kandaComplete("kanda_6")
    )
}

/**
 * JSON DTO for loading kanda content from assets.
 */
data class KandaJsonData(
    val modules: List<SanskritModule> = emptyList()
)
