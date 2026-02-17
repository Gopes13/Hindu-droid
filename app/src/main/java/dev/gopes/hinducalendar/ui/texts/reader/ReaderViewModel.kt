package dev.gopes.hinducalendar.ui.texts.reader

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gopes.hinducalendar.data.model.AppLanguage
import dev.gopes.hinducalendar.data.model.SacredTextType
import dev.gopes.hinducalendar.data.repository.PreferencesRepository
import dev.gopes.hinducalendar.engine.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val sacredTextService: SacredTextService,
    private val preferencesRepository: PreferencesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

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

    init {
        loadData()
    }

    private fun loadData() {
        val tt = textType ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = preferencesRepository.preferencesFlow.first()
            language = prefs.language

            when (tt) {
                SacredTextType.GITA -> {
                    gitaData = sacredTextService.loadGita()
                    selectedChapter = prefs.readingProgress.gitaChapter
                }
                SacredTextType.HANUMAN_CHALISA -> {
                    chalisaData = sacredTextService.loadChalisa()
                }
                SacredTextType.JAPJI_SAHIB -> {
                    japjiData = sacredTextService.loadJapji()
                }
                SacredTextType.BHAGAVATA, SacredTextType.SHIVA_PURANA -> {
                    episodeData = sacredTextService.loadEpisodeText(tt.jsonFileName)
                }
                SacredTextType.VISHNU_SAHASRANAMA, SacredTextType.SHIKSHAPATRI -> {
                    shlokaData = sacredTextService.loadShlokaText(tt.jsonFileName)
                }
                SacredTextType.SOUNDARYA_LAHARI -> {
                    verseData = sacredTextService.loadVerseText(tt.jsonFileName)
                }
                SacredTextType.DEVI_MAHATMYA -> {
                    chapterData = sacredTextService.loadChapterText(tt.jsonFileName)
                }
                SacredTextType.RUDRAM -> {
                    rudramData = sacredTextService.loadRudram()
                }
                SacredTextType.GURBANI -> {
                    gurbaniData = sacredTextService.loadGurbani()
                }
                SacredTextType.SUKHMANI -> {
                    sukhmaniData = sacredTextService.loadSukhmani()
                }
                SacredTextType.TATTVARTHA_SUTRA -> {
                    sutraData = sacredTextService.loadSutraText(tt.jsonFileName)
                }
                SacredTextType.VACHANAMRUT -> {
                    discourseData = sacredTextService.loadDiscourseText(tt.jsonFileName)
                }
                SacredTextType.JAIN_PRAYERS -> {
                    jainPrayersData = sacredTextService.loadJainPrayers()
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

    fun saveProgress(position: Int) {
        val tt = textType ?: return
        viewModelScope.launch {
            preferencesRepository.update { prefs ->
                prefs.copy(readingProgress = prefs.readingProgress.withPosition(tt, position))
            }
        }
    }
}
