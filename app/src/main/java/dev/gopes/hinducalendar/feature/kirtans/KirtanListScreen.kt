package dev.gopes.hinducalendar.feature.kirtans

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.domain.model.Kirtan
import dev.gopes.hinducalendar.domain.model.KirtanCategory
import dev.gopes.hinducalendar.core.ui.components.SacredCard
import dev.gopes.hinducalendar.core.util.localizedOriginLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KirtanListScreen(
    onKirtanClick: (String) -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: KirtanListViewModel = hiltViewModel()
) {
    val aartis = viewModel.aartis
    val bhajans = viewModel.bhajans
    val language by viewModel.language.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.kirtans_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Aartis section
            if (aartis.isNotEmpty()) {
                item(key = "aartis_header") {
                    CategoryHeader(KirtanCategory.AARTI)
                }
                items(aartis, key = { "aarti_${it.id}" }) { kirtan ->
                    KirtanRow(kirtan, language) { onKirtanClick(kirtan.id) }
                }
            }

            // Bhajans section
            if (bhajans.isNotEmpty()) {
                item(key = "bhajans_header") {
                    Spacer(Modifier.height(8.dp))
                    CategoryHeader(KirtanCategory.BHAJAN)
                }
                items(bhajans, key = { "bhajan_${it.id}" }) { kirtan ->
                    KirtanRow(kirtan, language) { onKirtanClick(kirtan.id) }
                }
            }
        }
    }
}

@Composable
private fun CategoryHeader(category: KirtanCategory) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            if (category == KirtanCategory.AARTI) Icons.Filled.LocalFireDepartment
            else Icons.Filled.MusicNote,
            contentDescription = stringResource(R.string.cd_kirtan_category),
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )
        Text(
            stringResource(
                if (category == KirtanCategory.AARTI) R.string.kirtans_aartis
                else R.string.kirtans_bhajans
            ),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun KirtanRow(
    kirtan: Kirtan,
    language: dev.gopes.hinducalendar.domain.model.AppLanguage,
    onClick: () -> Unit
) {
    SacredCard(
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon circle
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        if (kirtan.category == KirtanCategory.AARTI) Icons.Filled.LocalFireDepartment
                        else Icons.Filled.MusicNote,
                        contentDescription = stringResource(R.string.cd_kirtan_category),
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    kirtan.title(language),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                // Sanskrit title if different
                val localTitle = kirtan.title(language)
                if (kirtan.titleSanskrit != localTitle) {
                    Text(
                        kirtan.titleSanskrit,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Language badge + author
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            localizedOriginLanguage(kirtan.originLanguage),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.9f),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    kirtan.author?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }

            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = stringResource(R.string.cd_open_kirtan),
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
