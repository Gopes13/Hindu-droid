package dev.gopes.hinducalendar.feature.texts.reader.content

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.domain.model.AppLanguage
import dev.gopes.hinducalendar.domain.model.DiscourseTextData
import dev.gopes.hinducalendar.core.ui.components.SacredCard
import dev.gopes.hinducalendar.feature.texts.reader.components.CollapsibleExplanation

@Composable
internal fun DiscourseContent(data: DiscourseTextData, lang: AppLanguage, modifier: Modifier) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(data.discourses, key = { it.discourse }) { discourse ->
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
