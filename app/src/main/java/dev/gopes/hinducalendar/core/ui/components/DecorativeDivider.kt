package dev.gopes.hinducalendar.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class DividerStyle {
    OM, LOTUS, DIAMOND, DOT
}

/**
 * A sacred decorative divider with Om, lotus, or diamond symbol.
 */
@Composable
fun DecorativeDivider(
    modifier: Modifier = Modifier,
    style: DividerStyle = DividerStyle.DIAMOND
) {
    val tertiary = MaterialTheme.colorScheme.tertiary
    val lineColor = tertiary.copy(alpha = 0.2f)
    val symbolColor = tertiary.copy(alpha = 0.6f)

    val (symbol, fontSize) = when (style) {
        DividerStyle.OM -> "\u0950" to 16.sp       // ॐ
        DividerStyle.LOTUS -> "\u273F" to 14.sp     // ✿
        DividerStyle.DIAMOND -> "\u25C6" to 8.sp    // ◆
        DividerStyle.DOT -> "\u2022" to 10.sp       // •
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier
                .weight(1f)
                .height(0.5.dp)
                .drawBehind {
                    drawLine(
                        color = lineColor,
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width, size.height / 2),
                        strokeWidth = 1f
                    )
                }
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = symbol,
            fontSize = fontSize,
            color = symbolColor
        )
        Spacer(Modifier.width(12.dp))
        Box(
            Modifier
                .weight(1f)
                .height(0.5.dp)
                .drawBehind {
                    drawLine(
                        color = lineColor,
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width, size.height / 2),
                        strokeWidth = 1f
                    )
                }
        )
    }
}
