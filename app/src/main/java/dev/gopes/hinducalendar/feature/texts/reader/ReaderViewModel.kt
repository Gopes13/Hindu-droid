package dev.gopes.hinducalendar.feature.texts.reader

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.domain.model.*
import dev.gopes.hinducalendar.domain.repository.AudioPlaybackRepository
import dev.gopes.hinducalendar.domain.repository.PreferencesRepository
import dev.gopes.hinducalendar.domain.repository.SacredTextRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
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

sealed interface TextContent {
    data class Gita(val data: GitaData, val selectedChapter: Int) : TextContent
    data class Chalisa(val data: ChalisaData) : TextContent
    data class Japji(val data: JapjiData) : TextContent
    data class Episode(val data: EpisodeTextData) : TextContent
    data class Shloka(val data: ShlokaTextData) : TextContent
    data class Verse(val data: VerseTextData) : TextContent
    data class Chapter(val data: ChapterTextData) : TextContent
    data class Rudram(val data: RudramData) : TextContent
    data class Gurbani(val data: GurbaniData) : TextContent
    data class Sukhmani(val data: SukhmaniData) : TextContent
    data class Sutra(val data: SutraTextData) : TextContent
    data class Discourse(val data: DiscourseTextData) : TextContent
    data class Jain(val data: JainPrayersData) : TextContent
}

sealed interface ReaderUiState {
    data object Loading : ReaderUiState
    data class Error(val message: String) : ReaderUiState
    data class Loaded(
        val textType: SacredTextType,
        val content: TextContent,
        val language: AppLanguage,
        val bookmarks: BookmarkCollection
    ) : ReaderUiState
}

@HiltViewModel
class ReaderViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sacredTextRepository: SacredTextRepository,
    private val preferencesRepository: PreferencesRepository,
    private val audioPlaybackRepository: AudioPlaybackRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Stable callback references — created once per ViewModel lifetime
    private val audioToggle: (String) -> Unit = { audioPlaybackRepository.toggle(it) }
    private val audioSeek: (Int) -> Unit = { audioPlaybackRepository.seekTo(it) }
    private val audioSkipForward: () -> Unit = { audioPlaybackRepository.skipForward() }
    private val audioSkipBackward: () -> Unit = { audioPlaybackRepository.skipBackward() }
    private val audioSetSpeed: (Float) -> Unit = { audioPlaybackRepository.setSpeed(it) }
    private val audioHasAudio: (String) -> Boolean = { audioPlaybackRepository.hasAudio(it) }
    private val audioDuration: (String) -> Int? = { audioPlaybackRepository.duration(it) }

    val audioUiState: StateFlow<AudioUiState> = combine(
        audioPlaybackRepository.state,
        audioPlaybackRepository.currentlyPlayingId,
        audioPlaybackRepository.playbackProgress,
        audioPlaybackRepository.currentPositionMs,
        audioPlaybackRepository.playbackSpeed
    ) { state, id, progress, position, speed ->
        AudioUiState(
            playbackState = state,
            currentlyPlayingId = id,
            playbackProgress = progress,
            currentPositionMs = position,
            playbackSpeed = speed,
            onToggle = audioToggle,
            onSeek = audioSeek,
            onSkipForward = audioSkipForward,
            onSkipBackward = audioSkipBackward,
            onSetSpeed = audioSetSpeed,
            hasAudio = audioHasAudio,
            duration = audioDuration
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AudioUiState())

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

    private val _loadedContent = MutableStateFlow<TextContent?>(null)
    private val _selectedChapter = MutableStateFlow(1)

    val uiState: StateFlow<ReaderUiState> = combine(
        _loadedContent,
        _selectedChapter,
        preferencesRepository.preferencesFlow
    ) { content, chapter, prefs ->
        when {
            content == null -> ReaderUiState.Loading
            content is TextContent.Gita -> ReaderUiState.Loaded(
                textType = textType!!,
                content = content.copy(selectedChapter = chapter),
                language = prefs.language,
                bookmarks = prefs.bookmarks
            )
            else -> ReaderUiState.Loaded(
                textType = textType!!,
                content = content,
                language = prefs.language,
                bookmarks = prefs.bookmarks
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReaderUiState.Loading)

    init {
        loadData()
    }

    private fun loadData() {
        val tt = textType ?: return
        viewModelScope.launch {
            val prefs = withContext(Dispatchers.IO) {
                preferencesRepository.preferencesFlow.first()
            }
            val content: TextContent? = withContext(Dispatchers.IO) {
                when (tt) {
                    SacredTextType.GITA -> {
                        _selectedChapter.value = prefs.readingProgress.gitaChapter
                        sacredTextRepository.loadGita()?.let {
                            TextContent.Gita(it, prefs.readingProgress.gitaChapter)
                        }
                    }
                    SacredTextType.HANUMAN_CHALISA ->
                        sacredTextRepository.loadChalisa()?.let { TextContent.Chalisa(it) }
                    SacredTextType.JAPJI_SAHIB ->
                        sacredTextRepository.loadJapji()?.let { TextContent.Japji(it) }
                    SacredTextType.BHAGAVATA, SacredTextType.SHIVA_PURANA ->
                        sacredTextRepository.loadEpisodeText(tt.jsonFileName)?.let { TextContent.Episode(it) }
                    SacredTextType.VISHNU_SAHASRANAMA, SacredTextType.SHIKSHAPATRI ->
                        sacredTextRepository.loadShlokaText(tt.jsonFileName)?.let { TextContent.Shloka(it) }
                    SacredTextType.SOUNDARYA_LAHARI ->
                        sacredTextRepository.loadVerseText(tt.jsonFileName)?.let { TextContent.Verse(it) }
                    SacredTextType.DEVI_MAHATMYA ->
                        sacredTextRepository.loadChapterText(tt.jsonFileName)?.let { TextContent.Chapter(it) }
                    SacredTextType.RUDRAM ->
                        sacredTextRepository.loadRudram()?.let { TextContent.Rudram(it) }
                    SacredTextType.GURBANI ->
                        sacredTextRepository.loadGurbani()?.let { TextContent.Gurbani(it) }
                    SacredTextType.SUKHMANI ->
                        sacredTextRepository.loadSukhmani()?.let { TextContent.Sukhmani(it) }
                    SacredTextType.TATTVARTHA_SUTRA ->
                        sacredTextRepository.loadSutraText(tt.jsonFileName)?.let { TextContent.Sutra(it) }
                    SacredTextType.VACHANAMRUT ->
                        sacredTextRepository.loadDiscourseText(tt.jsonFileName)?.let { TextContent.Discourse(it) }
                    SacredTextType.JAIN_PRAYERS ->
                        sacredTextRepository.loadJainPrayers()?.let { TextContent.Jain(it) }
                }
            }
            _loadedContent.value = content
        }
    }

    fun selectGitaChapter(chapter: Int) {
        _selectedChapter.value = chapter
        viewModelScope.launch {
            preferencesRepository.update { prefs ->
                prefs.copy(readingProgress = prefs.readingProgress.copy(gitaChapter = chapter))
            }
        }
    }

    fun getStudyVerses(): List<StudyVerse> {
        val state = uiState.value
        if (state !is ReaderUiState.Loaded) return emptyList()
        return buildStudyVerses(state.content, state.language)
    }

    @Suppress("CyclomaticComplexity")
    private fun buildStudyVerses(content: TextContent, lang: AppLanguage): List<StudyVerse> {
        return when (content) {
            is TextContent.Gita -> {
                val ch = content.data.chapters.find { it.chapter == content.selectedChapter }
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
            is TextContent.Chalisa -> content.data.allVerses.map { v ->
                StudyVerse(
                    reference = "${(v.type ?: s(R.string.text_verse)).replaceFirstChar { c -> c.uppercase() }} ${v.verse}",
                    originalText = v.sanskrit,
                    transliteration = v.transliteration,
                    translation = v.translation(lang),
                    explanation = null,
                    audioId = "chalisa_${v.type ?: "verse"}_${v.verse}"
                )
            }
            is TextContent.Japji -> content.data.pauris.map { p ->
                StudyVerse(
                    reference = "${s(R.string.text_pauri)} ${p.pauri}",
                    originalText = p.punjabi,
                    transliteration = p.transliteration,
                    translation = p.translation(lang),
                    explanation = null,
                    audioId = "japji_pauri_${p.pauri}"
                )
            }
            is TextContent.Shloka -> {
                val prefix = textType?.jsonFileName ?: "shloka"
                content.data.shlokas.map { shloka ->
                    StudyVerse(
                        reference = "${s(R.string.text_shloka)} ${shloka.shloka}",
                        originalText = shloka.sanskrit,
                        transliteration = shloka.transliteration,
                        translation = shloka.translation(lang),
                        explanation = shloka.commentary(lang).ifEmpty { shloka.explanation(lang).ifEmpty { null } },
                        names = shloka.names?.map { it.name to it.meaning(lang) },
                        audioId = "${prefix}_${shloka.shloka}"
                    )
                }
            }
            is TextContent.Verse -> content.data.verses.map { v ->
                StudyVerse(
                    reference = "${s(R.string.text_verse)} ${v.verse}",
                    originalText = v.sanskrit,
                    transliteration = v.transliteration,
                    translation = v.translation(lang),
                    explanation = v.theme(lang).ifEmpty { null },
                    audioId = "soundarya_${v.verse}"
                )
            }
            is TextContent.Rudram -> {
                val section = content.data.namakam ?: content.data.chamakam
                val sectionName = if (content.data.namakam != null) "namakam" else "chamakam"
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
            is TextContent.Gurbani -> content.data.shabads.mapIndexed { index, shabad ->
                StudyVerse(
                    reference = "${s(R.string.text_shabad)} ${shabad.day}",
                    originalText = shabad.punjabi,
                    transliteration = shabad.transliteration,
                    translation = shabad.translation(lang),
                    explanation = shabad.theme(lang).ifEmpty { null },
                    audioId = "gurbani_day_${index + 1}"
                )
            }
            is TextContent.Sukhmani -> content.data.ashtpadis.flatMap { section ->
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
            is TextContent.Sutra -> content.data.chapters.flatMap { ch ->
                ch.sutras.map { sutra ->
                    StudyVerse(
                        reference = "${ch.chapter}.${sutra.sutra}",
                        originalText = sutra.sanskrit,
                        transliteration = sutra.transliteration,
                        translation = sutra.translation(lang),
                        explanation = sutra.commentary(lang).ifEmpty { null },
                        audioId = "tattvartha_${ch.chapter}_${sutra.sutra}"
                    )
                }
            }
            is TextContent.Episode -> {
                val prefix = textType?.jsonFileName ?: "episode"
                content.data.episodes.map { e ->
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
            is TextContent.Discourse -> content.data.discourses.map { d ->
                StudyVerse(
                    reference = "${s(R.string.text_discourse)} ${d.discourse}",
                    originalText = d.title(lang),
                    transliteration = null,
                    translation = d.summary(lang),
                    explanation = d.keyTeaching(lang).ifEmpty { null }
                )
            }
            is TextContent.Jain -> {
                val lines = content.data.namokarMantra?.lineByLine?.map { l ->
                    StudyVerse(
                        reference = "${s(R.string.text_line)} ${l.line}",
                        originalText = l.sanskrit,
                        transliteration = l.transliteration,
                        translation = l.translation(lang),
                        explanation = l.significance(lang).ifEmpty { null },
                        audioId = if (l.line == 1) "jain_namokar" else null
                    )
                } ?: emptyList()
                val teachings = content.data.mahaviraTeachings.map { t ->
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
            is TextContent.Chapter -> emptyList()
        }
    }

    fun isBookmarked(reference: String): Boolean {
        val tt = textType ?: return false
        val state = uiState.value
        if (state !is ReaderUiState.Loaded) return false
        return state.bookmarks.isBookmarked(tt, reference)
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
        }
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
