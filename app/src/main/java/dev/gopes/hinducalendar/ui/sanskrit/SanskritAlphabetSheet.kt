package dev.gopes.hinducalendar.ui.sanskrit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SanskritAlphabetSheet(
    progress: SanskritProgress,
    onSpeak: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedGroup by remember { mutableStateOf<SanskritLetterGroup?>(null) }
    var selectedLetter by remember { mutableStateOf<SanskritLetter?>(null) }

    val groups = SanskritLetterGroup.entries
    val filteredLetters = if (selectedGroup != null) {
        SanskritData.lettersByGroup(selectedGroup!!)
    } else {
        SanskritData.allLetters
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.sanskrit_alphabet_ref),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.common_close))
                }
            }

            Spacer(Modifier.height(8.dp))

            // Filter chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedGroup == null,
                        onClick = { selectedGroup = null },
                        label = { Text(stringResource(R.string.sanskrit_all_letters)) }
                    )
                }
                items(groups) { group ->
                    FilterChip(
                        selected = selectedGroup == group,
                        onClick = { selectedGroup = group },
                        label = { Text(group.displayName) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Letter grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredLetters, key = { it.id }) { letter ->
                    val isMastered = progress.isLetterMastered(letter.id)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isMastered) Color(0xFFFFD700).copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (isMastered) {
                            androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
                        } else null,
                        modifier = Modifier.clickable {
                            selectedLetter = letter
                            onSpeak(letter.character)
                        }
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                letter.character,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                letter.transliteration,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }

    // Letter detail dialog
    if (selectedLetter != null) {
        AlertDialog(
            onDismissRequest = { selectedLetter = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(selectedLetter!!.character, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(16.dp))
                    IconButton(onClick = { onSpeak(selectedLetter!!.character) }) {
                        Icon(
                            Icons.Filled.VolumeUp,
                            contentDescription = stringResource(R.string.sanskrit_speak),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow(stringResource(R.string.sanskrit_transliteration_label), selectedLetter!!.transliteration)
                    DetailRow(stringResource(R.string.sanskrit_pronunciation_label), selectedLetter!!.pronunciation)
                    HorizontalDivider()
                    DetailRow(stringResource(R.string.sanskrit_example_label), "${selectedLetter!!.exampleWord} (${selectedLetter!!.exampleTranslit})")
                    DetailRow(stringResource(R.string.sanskrit_meaning_label), selectedLetter!!.exampleMeaning)
                    if (progress.isLetterMastered(selectedLetter!!.id)) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFFFFD700).copy(alpha = 0.15f)
                        ) {
                            Text(
                                stringResource(R.string.sanskrit_mastered),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB8860B)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedLetter = null }) {
                    Text(stringResource(R.string.common_close))
                }
            }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(100.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
