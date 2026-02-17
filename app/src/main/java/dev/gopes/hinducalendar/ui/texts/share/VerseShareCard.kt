package dev.gopes.hinducalendar.ui.texts.share

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val gradientTop = Color(0xFF8C2610)    // burnt orange-brown
private val gradientMid = Color(0xFF591440)    // deep burgundy
private val gradientBottom = Color(0xFF260D4D) // dark purple

@Composable
fun VerseShareCard(
    sanskrit: String,
    transliteration: String?,
    translation: String,
    reference: String,
    textName: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(360.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(gradientTop, gradientMid, gradientBottom)
                )
            )
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Om watermark
            Text(
                "\u0950",
                fontSize = 28.sp,
                color = Color.White.copy(alpha = 0.15f),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            // Sanskrit text
            Text(
                sanskrit,
                fontSize = 18.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Medium
            )

            // Transliteration
            if (!transliteration.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    transliteration,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(16.dp))

            // Divider
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(0.6f),
                color = Color.White.copy(alpha = 0.3f),
                thickness = 1.dp
            )

            Spacer(Modifier.height(16.dp))

            // Translation
            Text(
                translation,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(20.dp))

            // Reference
            Text(
                "$textName, $reference",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            // App watermark
            Text(
                "Shared via Dharmic Companion",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.35f),
                textAlign = TextAlign.Center
            )
        }
    }
}
