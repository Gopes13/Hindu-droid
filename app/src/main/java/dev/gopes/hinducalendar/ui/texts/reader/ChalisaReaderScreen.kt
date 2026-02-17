package dev.gopes.hinducalendar.ui.texts.reader

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.ui.texts.reader.components.VerseCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChalisaReaderScreen(
    onBack: () -> Unit,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    val chalisa = viewModel.chalisaData
    val lang = viewModel.language

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hanuman Chalisa") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.cd_go_back))
                    }
                }
            )
        }
    ) { padding ->
        if (viewModel.isLoading || chalisa == null) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            // Dohas
            if (chalisa.dohas.isNotEmpty()) {
                item {
                    Text(
                        stringResource(R.string.reader_doha),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(chalisa.dohas) { verse ->
                    VerseCard(
                        badge = "${stringResource(R.string.reader_doha)} ${verse.verse}",
                        originalText = verse.sanskrit,
                        transliteration = verse.transliteration,
                        translation = verse.translation(lang),
                        isHighlighted = true
                    )
                }
            }

            // Chaupais
            if (chalisa.chaupais.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.reader_chaupai),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(chalisa.chaupais) { verse ->
                    VerseCard(
                        badge = "${stringResource(R.string.reader_chaupai)} ${verse.verse}",
                        originalText = verse.sanskrit,
                        transliteration = verse.transliteration,
                        translation = verse.translation(lang)
                    )
                }
            }

            // Closing Doha
            chalisa.closingDoha?.let { closing ->
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.reader_closing_doha),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                item {
                    VerseCard(
                        badge = stringResource(R.string.reader_closing_doha),
                        originalText = closing.sanskrit,
                        transliteration = closing.transliteration,
                        translation = closing.translation(lang),
                        isHighlighted = true
                    )
                }
            }
        }
    }
}
