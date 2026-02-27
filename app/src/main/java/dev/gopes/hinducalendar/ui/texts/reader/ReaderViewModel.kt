package dev.gopes.hinducalendar.ui.texts.reader

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.data.model.AppLanguage
import dev.gopes.hinducalendar.data.model.BookmarkCollection
import dev.gopes.hinducalendar.data.model.SacredTextType
import dev.gopes.hinducalendar.data.model.VerseBookmark
import dev.gopes.hinducalendar.data.repository.PreferencesRepository
import dev.gopes.hinducalendar.engine.*
import dev.gopes.hinducalendar.engine.AudioPlayerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class StudyVerse(
    val reference: String,
    val originalText: String,
    val transliteration: String?,
    val translation: String,
    val explanation: String?,
    val names: List<Pair<String, String>>? = null,
    val audioId: String? = null
)

enum class ReaderMode { NORMAL, STUDY, FOCUS }

@HiltViewModel
class ReaderViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sacredTextService: SacredTextService,
    private val preferencesRepository: PreferencesRepository,
    val audioPlayerService: AudioPlayerService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private fun getLocalizedContext(): Context {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) return context
        val locale = locales[0] ?: return context
        val config = Configuration(context.resources.configuration).apply { setLocale(locale) }
        return context.createConfigurationContext(config)
    }

    private fun s(id: Int): String = getLocalizedContext().getString(id)

    val textType: SacredTextType? = savedStateHandle.get<String>("textType")
        ?.let { name -> SacredTextType.entries.find { it.name == name } }

    var isLoading by mutableStateOf(true)
        private set
    var language by mutableStateOf(AppLanguage.ENGLISH)
        private set

    // Gita
    var gitaData by mutableStateOf<GitaData?>(null)
        private set
    var selectedChapter by mutableIntStateOf(1)
        private set

    // Chalisa
    var chalisaData by mutableStateOf<ChalisaData?>(null)
        private set

    // Japji
    var japjiData by mutableStateOf<JapjiData?>(null)
        private set

    // Generic types
    var episodeData by mutableStateOf<EpisodeTextData?>(null)
        private set
    var shlokaData by mutableStateOf<ShlokaTextData?>(null)
        private set
    var verseData by mutableStateOf<VerseTextData?>(null)
        private set
    var chapterData by mutableStateOf<ChapterTextData?>(null)
        private set
    var rudramData by mutableStateOf<RudramData?>(null)
        private set
    var gurbaniData by mutableStateOf<GurbaniData?>(null)
        private set
    var sukhmaniData by mutableStateOf<SukhmaniData?>(null)
        private set
    var sutraData by mutableStateOf<SutraTextData?>(null)
        private set
    var discourseData by mutableStateOf<DiscourseTextData?>(null)
        private set
    var jainPrayersData by mutableStateOf<JainPrayersData?>(null)
        private set

    // Bookmarks
    var bookmarks by mutableStateOf(BookmarkCollection())
        private set

    init {
        loadData()
    }

    private fun loadData() {
        val tt = textType ?: return
        viewModelScope.launch {
            val prefs = withContext(Dispatchers.IO) {
                preferencesRepository.preferencesFlow.first()
            }
            language = prefs.language
            bookmarks = prefs.bookmarks

            when (tt) {
                SacredTextType.GITA -> {
                    gitaData = withContext(Dispatchers.IO) { sacredTextService.loadGita() }
                    selectedChapter = prefs.readingProgress.gitaChapter
                }
                SacredTextType.HANUMAN_CHALISA -> {
                    chalisaData = withContext(Dispatchers.IO) { sacredTextService.loadChalisa() }
                }
                SacredTextType.JAPJI_SAHIB -> {
                    japjiData = withContext(Dispatchers.IO) { sacredTextService.loadJapji() }
                }
                SacredTextType.BHAGAVATA, SacredTextType.SHIVA_PURANA -> {
                    episodeData = withContext(Dispatchers.IO) { sacredTextService.loadEpisodeText(tt.jsonFileName) }
                }
                SacredTextType.VISHNU_SAHASRANAMA, SacredTextType.SHIKSHAPATRI -> {
                    shlokaData = withContext(Dispatchers.IO) { sacredTextService.loadShlokaText(tt.jsonFileName) }
                }
                SacredTextType.SOUNDARYA_LAHARI -> {
                    verseData = withContext(Dispatchers.IO) { sacredTextService.loadVerseText(tt.jsonFileName) }
                }
                SacredTextType.DEVI_MAHATMYA -> {
                    chapterData = withContext(Dispatchers.IO) { sacredTextService.loadChapterText(tt.jsonFileName) }
                }
                SacredTextType.RUDRAM -> {
                    rudramData = withContext(Dispatchers.IO) { sacredTextService.loadRudram() }
                }
                SacredTextType.GURBANI -> {
                    gurbaniData = withContext(Dispatchers.IO) { sacredTextService.loadGurbani() }
                }
                SacredTextType.SUKHMANI -> {
                    sukhmaniData = withContext(Dispatchers.IO) { sacredTextService.loadSukhmani() }
                }
                SacredTextType.TATTVARTHA_SUTRA -> {
                    sutraData = withContext(Dispatchers.IO) { sacredTextService.loadSutraText(tt.jsonFileName) }
                }
                SacredTextType.VACHANAMRUT -> {
                    discourseData = withContext(Dispatchers.IO) { sacredTextService.loadDiscourseText(tt.jsonFileName) }
                }
                SacredTextType.JAIN_PRAYERS -> {
                    jainPrayersData = withContext(Dispatchers.IO) { sacredTextService.loadJainPrayers() }
                }
            }

            isLoading = false
        }
    }

    fun selectGitaChapter(chapter: Int) {
        selectedChapter = chapter
        viewModelScope.launch {
            preferencesRepository.update { prefs ->
                prefs.copy(readingProgress = prefs.readingProgress.copy(gitaChapter = chapter))
            }
        }
    }

    fun getStudyVerses(): List<StudyVerse> {
        val lang = language
        return when {
            gitaData != null -> {
                val ch = gitaData!!.chapters.find { it.chapter == selectedChapter }
                    ?: return emptyList()
                ch.verses.map { verse ->
                    StudyVerse(
                        reference = "${ch.chapter}.${verse.verse}",
                        originalText = verse.sanskrit,
                        transliteration = verse.transliteration,
                        translation = verse.translation(lang),
                        explanation = null,
                        audioId = "gita_${ch.chapter}_${verse.verse}"
                    )
                }
            }
            chalisaData != null -> chalisaData!!.allVerses.map { v ->
                StudyVerse(
                    reference = "${(v.type ?: s(R.string.text_verse)).replaceFirstChar { c -> c.uppercase() }} ${v.verse}",
                    originalText = v.sanskrit,
                    transliteration = v.transliteration,
                    translation = v.translation(lang),
                    explanation = null,
                    audioId = "chalisa_${v.type ?: "verse"}_${v.verse}"
                )
            }
            japjiData != null -> japjiData!!.pauris.map { p ->
                StudyVerse(
                    reference = "${s(R.string.text_pauri)} ${p.pauri}",
                    originalText = p.punjabi,
                    transliteration = p.transliteration,
                    translation = p.translation(lang),
                    explanation = null,
                    audioId = "japji_pauri_${p.pauri}"
                )
            }
            shlokaData != null -> {
                val prefix = textType?.jsonFileName ?: "shloka"
                shlokaData!!.shlokas.map { s ->
                    StudyVerse(
                        reference = "${s(R.string.text_shloka)} ${s.shloka}",
                        originalText = s.sanskrit,
                        transliteration = s.transliteration,
                        translation = s.translation(lang),
                        explanation = s.commentary(lang).ifEmpty { s.explanation(lang).ifEmpty { null } },
                        names = s.names?.map { it.name to it.meaning(lang) },
                        audioId = "${prefix}_${s.shloka}"
                    )
                }
            }
            verseData != null -> verseData!!.verses.map { v ->
                StudyVerse(
                    reference = "${s(R.string.text_verse)} ${v.verse}",
                    originalText = v.sanskrit,
                    transliteration = v.transliteration,
                    translation = v.translation(lang),
                    explanation = v.theme(lang).ifEmpty { null },
                    audioId = "soundarya_${v.verse}"
                )
            }
            rudramData != null -> {
                val section = rudramData!!.namakam ?: rudramData!!.chamakam
                val sectionName = if (rudramData!!.namakam != null) "namakam" else "chamakam"
                section?.anuvakas?.map { a ->
                    StudyVerse(
                        reference = "${s(R.string.text_anuvaka)} ${a.anuvaka}",
                        originalText = a.sanskrit,
                        transliteration = a.transliteration,
                        translation = a.translation(lang),
                        explanation = a.theme(lang).ifEmpty { null },
                        audioId = "rudram_${sectionName}_${a.anuvaka}"
                    )
                } ?: emptyList()
            }
            gurbaniData != null -> gurbaniData!!.shabads.mapIndexed { index, s ->
                StudyVerse(
                    reference = "${s(R.string.text_shabad)} ${s.day}",
                    originalText = s.punjabi,
                    transliteration = s.transliteration,
                    translation = s.translation(lang),
                    explanation = s.theme(lang).ifEmpty { null },
                    audioId = "gurbani_day_${index + 1}"
                )
            }
            sukhmaniData != null -> sukhmaniData!!.ashtpadis.flatMap { section ->
                section.stanzas.map { st ->
                    StudyVerse(
                        reference = "${section.ashtpadi}.${st.stanza}",
                        originalText = st.punjabi,
                        transliteration = st.transliteration,
                        translation = st.translation(lang),
                        explanation = null,
                        audioId = "sukhmani_${section.ashtpadi}_stanza_${st.stanza}"
                    )
                }
            }
            sutraData != null -> sutraData!!.chapters.flatMap { ch ->
                ch.sutras.map { s ->
                    StudyVerse(
                        reference = "${ch.chapter}.${s.sutra}",
                        originalText = s.sanskrit,
                        transliteration = s.transliteration,
                        translation = s.translation(lang),
                        explanation = s.commentary(lang).ifEmpty { null },
                        audioId = "tattvartha_${ch.chapter}_${s.sutra}"
                    )
                }
            }
            episodeData != null -> {
                val prefix = textType?.jsonFileName ?: "episode"
                episodeData!!.episodes.map { e ->
                    val hasMantra = e.relatedMantra != null
                    StudyVerse(
                        reference = "${s(R.string.text_episode)} ${e.episode}",
                        originalText = e.relatedVerse?.sanskrit ?: e.title(lang),
                        transliteration = e.relatedVerse?.transliteration,
                        translation = e.summary(lang),
                        explanation = e.keyTeaching(lang).ifEmpty { null },
                        audioId = if (hasMantra) "${prefix}_ep_${e.episode}_mantra"
                                 else "${prefix}_ep_${e.episode}_verse"
                    )
                }
            }
            discourseData != null -> discourseData!!.discourses.map { d ->
                StudyVerse(
                    reference = "${s(R.string.text_discourse)} ${d.discourse}",
                    originalText = d.title(lang),
                    transliteration = null,
                    translation = d.summary(lang),
                    explanation = d.keyTeaching(lang).ifEmpty { null }
                )
            }
            jainPrayersData != null -> {
                val lines = jainPrayersData!!.namokarMantra?.lineByLine?.map { l ->
                    StudyVerse(
                        reference = "${s(R.string.text_line)} ${l.line}",
                        originalText = l.sanskrit,
                        transliteration = l.transliteration,
                        translation = l.translation(lang),
                        explanation = l.significance(lang).ifEmpty { null },
                        audioId = if (l.line == 1) "jain_namokar" else null
                    )
                } ?: emptyList()
                val teachings = jainPrayersData!!.mahaviraTeachings.map { t ->
                    StudyVerse(
                        reference = "${s(R.string.text_teaching)} ${t.episode}",
                        originalText = t.title(lang),
                        transliteration = null,
                        translation = t.content(lang),
                        explanation = t.lesson(lang).ifEmpty { null }
                    )
                }
                lines + teachings
            }
            else -> emptyList()
        }
    }

    fun toggleBookmark(reference: String, verseText: String, translation: String) {
        val tt = textType ?: return
        viewModelScope.launch {
            preferencesRepository.update { prefs ->
                val existing = prefs.bookmarks.findBookmark(tt, reference)
                val updated = if (existing != null) {
                    prefs.bookmarks.remove(existing.id)
                } else {
                    prefs.bookmarks.add(
                        VerseBookmark(
                            textType = tt,
                            verseReference = reference,
                            verseText = verseText,
                            translation = translation
                        )
                    )
                }
                prefs.copy(bookmarks = updated)
            }
            bookmarks = preferencesRepository.preferencesFlow.first().bookmarks
        }
    }

    fun isBookmarked(reference: String): Boolean {
        val tt = textType ?: return false
        return bookmarks.isBookmarked(tt, reference)
    }

    fun saveProgress(position: Int) {
        val tt = textType ?: return
        viewModelScope.launch {
            preferencesRepository.update { prefs ->
                prefs.copy(readingProgress = prefs.readingProgress.withPosition(tt, position))
            }
        }
    }
}
