package dev.gopes.hinducalendar.ui.texts.reader

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.data.model.AppLanguage
import dev.gopes.hinducalendar.engine.*
import dev.gopes.hinducalendar.ui.components.SacredCard
import dev.gopes.hinducalendar.ui.components.SacredHighlightCard
import dev.gopes.hinducalendar.ui.texts.reader.components.CollapsibleExplanation
import dev.gopes.hinducalendar.ui.texts.reader.components.VerseCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericReaderScreen(
    onBack: () -> Unit,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    val title = viewModel.textType?.displayName ?: "Sacred Text"
    val studyVerses = remember(viewModel.isLoading) { viewModel.getStudyVerses() }
    var readerMode by remember { mutableStateOf(ReaderMode.NORMAL) }

    if (readerMode == ReaderMode.STUDY && studyVerses.isNotEmpty()) {
        StudyModeScreen(verses = studyVerses, onDismiss = { readerMode = ReaderMode.NORMAL })
        return
    }
    if (readerMode == ReaderMode.FOCUS && studyVerses.isNotEmpty()) {
        FocusedReadingScreen(verses = studyVerses, onDismiss = { readerMode = ReaderMode.NORMAL })
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.cd_go_back))
                    }
                },
                actions = {
                    if (studyVerses.isNotEmpty()) {
                        IconButton(onClick = { readerMode = ReaderMode.STUDY }) {
                            Icon(Icons.Filled.School, stringResource(R.string.study_mode))
                        }
                        IconButton(onClick = { readerMode = ReaderMode.FOCUS }) {
                            Icon(Icons.Filled.CropFree, stringResource(R.string.focus_mode))
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (viewModel.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val lang = viewModel.language

        when {
            viewModel.episodeData != null ->
                EpisodeContent(viewModel.episodeData!!, lang, Modifier.padding(padding))
            viewModel.shlokaData != null ->
                ShlokaContent(viewModel.shlokaData!!, lang, Modifier.padding(padding), viewModel::isBookmarked, viewModel::toggleBookmark)
            viewModel.verseData != null ->
                NumberedVerseContent(viewModel.verseData!!, lang, Modifier.padding(padding), viewModel::isBookmarked, viewModel::toggleBookmark)
            viewModel.chapterData != null ->
                ChapterContent(viewModel.chapterData!!, lang, Modifier.padding(padding))
            viewModel.rudramData != null ->
                RudramContent(viewModel.rudramData!!, lang, Modifier.padding(padding), viewModel::isBookmarked, viewModel::toggleBookmark)
            viewModel.gurbaniData != null ->
                GurbaniContent(viewModel.gurbaniData!!, lang, Modifier.padding(padding))
            viewModel.sukhmaniData != null ->
                SukhmaniContent(viewModel.sukhmaniData!!, lang, Modifier.padding(padding), viewModel::isBookmarked, viewModel::toggleBookmark)
            viewModel.sutraData != null ->
                SutraContent(viewModel.sutraData!!, lang, Modifier.padding(padding), viewModel::isBookmarked, viewModel::toggleBookmark)
            viewModel.discourseData != null ->
                DiscourseContent(viewModel.discourseData!!, lang, Modifier.padding(padding))
            viewModel.jainPrayersData != null ->
                JainContent(viewModel.jainPrayersData!!, lang, Modifier.padding(padding), viewModel::isBookmarked, viewModel::toggleBookmark)
            else ->
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    Text(stringResource(R.string.reader_unable_to_load))
                }
        }
    }
}

// ── Episode-Based (Bhagavata Purana, Shiva Purana) ──────────────────────────

@Composable
private fun EpisodeContent(data: EpisodeTextData, lang: AppLanguage, modifier: Modifier) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(data.episodes) { episode ->
            SacredCard {
                // Episode badge
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        "${stringResource(R.string.text_episode)} ${episode.episode}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    episode.title(lang),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))
                Text(
                    episode.summary(lang),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Related verse
                episode.relatedVerse?.let { rv ->
                    if (rv.sanskrit.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            rv.sanskrit,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 24.sp
                        )
                        rv.transliteration?.let {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                it,
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            rv.translation(lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Key teaching
                val teaching = episode.keyTeaching(lang)
                if (teaching.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    CollapsibleExplanation(text = teaching)
                }
            }
        }
    }
}

// ── Shloka-Based (Vishnu Sahasranama, Shikshapatri) ─────────────────────────

@Composable
private fun ShlokaContent(data: ShlokaTextData, lang: AppLanguage, modifier: Modifier, isBookmarked: (String) -> Boolean, onToggleBookmark: (String, String, String) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // Dhyana Shloka
        data.dhyanaShloka?.let { ds ->
            item {
                SacredHighlightCard {
                    Text(
                        stringResource(R.string.reader_dhyana_shloka),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(ds.sanskrit, style = MaterialTheme.typography.bodyLarge, lineHeight = 28.sp)
                    ds.transliteration?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(it, style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(8.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(8.dp))
                    Text(ds.translation(lang), style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp)
                }
            }
        }

        items(data.shlokas) { shloka ->
            val commentary = shloka.commentary(lang).ifEmpty { null }
                ?: shloka.explanation(lang).ifEmpty { null }
            val ref = "${stringResource(R.string.text_shloka)} ${shloka.shloka}"
            VerseCard(
                badge = ref,
                originalText = shloka.sanskrit,
                transliteration = shloka.transliteration,
                translation = shloka.translation(lang),
                explanation = commentary,
                isBookmarked = isBookmarked(ref),
                onBookmarkToggle = { onToggleBookmark(ref, shloka.sanskrit, shloka.translation(lang)) }
            )

            // Names (for Vishnu Sahasranama)
            shloka.names?.takeIf { it.isNotEmpty() }?.let { names ->
                Spacer(Modifier.height(4.dp))
                SacredCard {
                    Text(
                        stringResource(R.string.reader_names_meanings),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    names.forEach { name ->
                        Row(Modifier.padding(vertical = 2.dp)) {
                            Text(
                                name.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.width(120.dp)
                            )
                            Text(
                                name.meaning(lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Verse-Based (Soundarya Lahari) ──────────────────────────────────────────

@Composable
private fun NumberedVerseContent(data: VerseTextData, lang: AppLanguage, modifier: Modifier, isBookmarked: (String) -> Boolean, onToggleBookmark: (String, String, String) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(data.verses) { verse ->
            val theme = verse.theme(lang).ifEmpty { null }
            val ref = "${stringResource(R.string.text_verse)} ${verse.verse}"
            VerseCard(
                badge = ref,
                originalText = verse.sanskrit,
                transliteration = verse.transliteration,
                translation = verse.translation(lang),
                explanation = theme,
                isBookmarked = isBookmarked(ref),
                onBookmarkToggle = { onToggleBookmark(ref, verse.sanskrit, verse.translation(lang)) }
            )
        }
    }
}

// ── Chapter-Based (Devi Mahatmya) ───────────────────────────────────────────

@Composable
private fun ChapterContent(data: ChapterTextData, lang: AppLanguage, modifier: Modifier) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // Kavach
        data.kavach?.let { kav ->
            item {
                SacredHighlightCard {
                    Text(
                        stringResource(R.string.reader_kavach),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(kav.sanskrit, style = MaterialTheme.typography.bodyLarge, lineHeight = 28.sp)
                    kav.transliteration?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(it, style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(8.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(8.dp))
                    Text(kav.translation(lang), style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp)
                }
            }
        }

        items(data.chapters) { chapter ->
            SacredCard {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        "${stringResource(R.string.text_chapter)} ${chapter.chapter}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    chapter.title(lang),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    chapter.summary(lang),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )

                // Key verses
                chapter.keyVerses?.forEach { kv ->
                    Spacer(Modifier.height(12.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(12.dp))
                    Text(kv.sanskrit, style = MaterialTheme.typography.bodyMedium, lineHeight = 24.sp)
                    kv.transliteration?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(it, style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(kv.translation(lang), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// ── Rudram (Namakam + Chamakam) ─────────────────────────────────────────────

@Composable
private fun RudramContent(data: RudramData, lang: AppLanguage, modifier: Modifier, isBookmarked: (String) -> Boolean, onToggleBookmark: (String, String, String) -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOfNotNull(
        data.namakam?.let { stringResource(R.string.reader_namakam) to it },
        data.chamakam?.let { stringResource(R.string.reader_chamakam) to it }
    )

    Column(modifier.fillMaxSize()) {
        if (tabs.size > 1) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, (title, _) ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
        }

        val section = tabs.getOrNull(selectedTab)?.second
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            section?.anuvakas?.let { anuvakas ->
                items(anuvakas) { anuvaka ->
                    val theme = anuvaka.theme(lang).ifEmpty { null }
                    val ref = "${stringResource(R.string.text_shloka)} ${anuvaka.anuvaka}"
                    VerseCard(
                        badge = ref,
                        originalText = anuvaka.sanskrit,
                        transliteration = anuvaka.transliteration,
                        translation = anuvaka.translation(lang),
                        explanation = theme,
                        isBookmarked = isBookmarked(ref),
                        onBookmarkToggle = { onToggleBookmark(ref, anuvaka.sanskrit, anuvaka.translation(lang)) }
                    )
                }
            }
        }
    }
}

// ── Gurbani (Daily Shabads) ─────────────────────────────────────────────────

@Composable
private fun GurbaniContent(data: GurbaniData, lang: AppLanguage, modifier: Modifier) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(data.shabads) { shabad ->
            SacredCard {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            "${stringResource(R.string.text_shabad)} ${shabad.day}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (shabad.author.isNotBlank()) {
                        Text(
                            shabad.author,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    shabad.punjabi,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 28.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    shabad.transliteration,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
                Spacer(Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(12.dp))
                Text(
                    shabad.translation(lang),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )

                val theme = shabad.theme(lang)
                if (theme.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    CollapsibleExplanation(text = theme)
                }
            }
        }
    }
}

// ── Sukhmani Sahib (Ashtpadis) ──────────────────────────────────────────────

@Composable
private fun SukhmaniContent(data: SukhmaniData, lang: AppLanguage, modifier: Modifier, isBookmarked: (String) -> Boolean, onToggleBookmark: (String, String, String) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(data.ashtpadis) { section ->
            // Section header
            Text(
                "${stringResource(R.string.text_ashtpadi)} ${section.ashtpadi}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            val summary = section.summary(lang)
            if (summary.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Salok
            section.salok?.let { salok ->
                Spacer(Modifier.height(8.dp))
                SacredHighlightCard {
                    Text(
                        stringResource(R.string.reader_salok),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(salok.punjabi, style = MaterialTheme.typography.bodyLarge, lineHeight = 28.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(salok.transliteration, style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(8.dp))
                    Text(salok.translation(lang), style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp)
                }
            }

            // Stanzas
            section.stanzas.forEach { stanza ->
                Spacer(Modifier.height(8.dp))
                val ref = "${section.ashtpadi}.${stanza.stanza}"
                VerseCard(
                    badge = ref,
                    originalText = stanza.punjabi,
                    transliteration = stanza.transliteration,
                    translation = stanza.translation(lang),
                    isBookmarked = isBookmarked(ref),
                    onBookmarkToggle = { onToggleBookmark(ref, stanza.punjabi, stanza.translation(lang)) }
                )
            }
        }
    }
}

// ── Tattvartha Sutra (Chapter-organized Sutras) ─────────────────────────────

@Composable
private fun SutraContent(data: SutraTextData, lang: AppLanguage, modifier: Modifier, isBookmarked: (String) -> Boolean, onToggleBookmark: (String, String, String) -> Unit) {
    var selectedChapter by remember { mutableIntStateOf(0) }

    Column(modifier.fillMaxSize()) {
        // Chapter picker
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            items(data.chapters.size) { index ->
                val ch = data.chapters[index]
                val isSelected = index == selectedChapter
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { selectedChapter = index },
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "Ch ${ch.chapter}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Chapter title
        data.chapters.getOrNull(selectedChapter)?.let { ch ->
            Text(
                ch.title(lang),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        // Sutras
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            data.chapters.getOrNull(selectedChapter)?.sutras?.let { sutras ->
                items(sutras) { sutra ->
                    val ch = data.chapters[selectedChapter]
                    val ref = "${ch.chapter}.${sutra.sutra}"
                    VerseCard(
                        badge = ref,
                        originalText = sutra.sanskrit,
                        transliteration = sutra.transliteration,
                        translation = sutra.translation(lang),
                        explanation = sutra.commentary(lang).ifEmpty { null },
                        isBookmarked = isBookmarked(ref),
                        onBookmarkToggle = { onToggleBookmark(ref, sutra.sanskrit, sutra.translation(lang)) }
                    )
                }
            }
        }
    }
}

// ── Vachanamrut (Discourses) ────────────────────────────────────────────────

@Composable
private fun DiscourseContent(data: DiscourseTextData, lang: AppLanguage, modifier: Modifier) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(data.discourses) { discourse ->
            SacredCard {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            "${stringResource(R.string.text_discourse)} ${discourse.discourse}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    discourse.section?.let {
                        Text(it, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary)
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    discourse.title(lang),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    discourse.summary(lang),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )

                val question = discourse.keyQuestion(lang)
                if (question.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.reader_key_question),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(question, style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                val teaching = discourse.keyTeaching(lang)
                if (teaching.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    CollapsibleExplanation(text = teaching)
                }

                val quote = discourse.quote(lang)
                if (quote.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "\u201C$quote\u201D",
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.secondary,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

// ── Jain (Namokar Mantra + Mahavira Teachings) ──────────────────────────────

@Composable
private fun JainContent(data: JainPrayersData, lang: AppLanguage, modifier: Modifier, isBookmarked: (String) -> Boolean, onToggleBookmark: (String, String, String) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // Namokar Mantra
        data.namokarMantra?.let { nm ->
            item {
                Text(
                    stringResource(R.string.reader_namokar_mantra),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                SacredHighlightCard {
                    Text(nm.sanskrit, style = MaterialTheme.typography.bodyLarge, lineHeight = 28.sp)
                    nm.transliteration?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(it, style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(8.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(8.dp))
                    Text(nm.translation(lang), style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp)

                    val desc = nm.description(lang)
                    if (desc.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        CollapsibleExplanation(text = desc)
                    }
                }
            }

            // Line-by-line breakdown
            if (nm.lineByLine.isNotEmpty()) {
                items(nm.lineByLine) { line ->
                    val ref = "Line ${line.line}"
                    VerseCard(
                        badge = ref,
                        originalText = line.sanskrit,
                        transliteration = line.transliteration,
                        translation = line.translation(lang),
                        explanation = line.significance(lang).ifEmpty { null },
                        isBookmarked = isBookmarked(ref),
                        onBookmarkToggle = { onToggleBookmark(ref, line.sanskrit, line.translation(lang)) }
                    )
                }
            }
        }

        // Mahavira Teachings
        if (data.mahaviraTeachings.isNotEmpty()) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.reader_teachings),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(data.mahaviraTeachings) { teaching ->
                SacredCard {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            "${stringResource(R.string.text_episode)} ${teaching.episode}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        teaching.title(lang),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        teaching.content(lang),
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )

                    val quote = teaching.keyQuote(lang)
                    if (quote.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "\u201C$quote\u201D",
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    val lesson = teaching.lesson(lang)
                    if (lesson.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        CollapsibleExplanation(text = lesson)
                    }
                }
            }
        }
    }
}
