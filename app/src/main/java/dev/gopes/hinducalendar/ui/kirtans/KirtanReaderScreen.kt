package dev.gopes.hinducalendar.ui.kirtans

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.data.model.AppLanguage
import dev.gopes.hinducalendar.data.model.Kirtan
import dev.gopes.hinducalendar.data.model.KirtanStanza
import dev.gopes.hinducalendar.ui.components.SacredCard
import dev.gopes.hinducalendar.ui.components.SacredHighlightCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KirtanReaderScreen(
    kirtanId: String,
    onBack: () -> Unit = {},
    viewModel: KirtanReaderViewModel = hiltViewModel()
) {
    LaunchedEffect(kirtanId) { viewModel.loadKirtan(kirtanId) }

    val kirtan = viewModel.kirtan
    val language = viewModel.language

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(kirtan?.title(language) ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        if (kirtan == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Header
                item(key = "header") {
                    KirtanHeader(kirtan, language)
                }

                // Stanza cards
                itemsIndexed(kirtan.stanzas, key = { _, s -> "stanza_${s.stanza}" }) { _, stanza ->
                    StanzaCard(stanza, language)
                }
            }
        }
    }
}

@Composable
private fun KirtanHeader(kirtan: Kirtan, language: AppLanguage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            kirtan.title(language),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        val localTitle = kirtan.title(language)
        if (kirtan.titleSanskrit != localTitle) {
            Spacer(Modifier.height(4.dp))
            Text(
                kirtan.titleSanskrit,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            kirtan.author?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
            ) {
                Text(
                    kirtan.originLanguage,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun StanzaCard(stanza: KirtanStanza, language: AppLanguage) {
    SacredCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Stanza number
            Text(
                "${stringResource(R.string.kirtan_stanza)} ${language.localizedNumber(stanza.stanza)}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Lyrics (serif for Devanagari scripts)
            Text(
                stanza.lyrics,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Serif,
                    lineHeight = 28.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Translation
            val meaning = stanza.translation(language)
            if (meaning.isNotEmpty()) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 2.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Text(
                    stringResource(R.string.kirtan_meaning),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    meaning,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                )
            }
        }
    }
}
