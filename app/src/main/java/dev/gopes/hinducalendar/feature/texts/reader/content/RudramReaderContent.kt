package dev.gopes.hinducalendar.feature.texts.reader.content

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.domain.model.AppLanguage
import dev.gopes.hinducalendar.domain.model.RudramData
import dev.gopes.hinducalendar.feature.texts.reader.AudioUiState
import dev.gopes.hinducalendar.feature.texts.reader.components.VerseCard

@Composable
internal fun RudramContent(data: RudramData, lang: AppLanguage, modifier: Modifier, isBookmarked: (String) -> Boolean, onToggleBookmark: (String, String, String) -> Unit, audio: AudioUiState? = null) {
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
                val sectionName = if (selectedTab == 0) "namakam" else "chamakam"
                items(anuvakas, key = { "${sectionName}_${it.anuvaka}" }) { anuvaka ->
                    val theme = anuvaka.theme(lang).ifEmpty { null }
                    val ref = "${stringResource(R.string.text_shloka)} ${anuvaka.anuvaka}"
                    VerseCard(
                        badge = ref,
                        originalText = anuvaka.sanskrit,
                        transliteration = anuvaka.transliteration,
                        translation = anuvaka.translation(lang),
                        explanation = theme,
                        isBookmarked = isBookmarked(ref),
                        onBookmarkToggle = { onToggleBookmark(ref, anuvaka.sanskrit, anuvaka.translation(lang)) },
                        audioId = "rudram_${sectionName}_${anuvaka.anuvaka}",
                        audio = audio
                    )
                }
            }
        }
    }
}
