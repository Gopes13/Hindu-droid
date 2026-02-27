package dev.gopes.hinducalendar.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Spacing constants matching the iOS AppSpacing system.
 * iOS uses responsive scaling (screenWidth/375); on Android we use fixed dp
 * since the density system already handles screen adaptation.
 */
object AppSpacing {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 20.dp
    val xl = 24.dp
    val xxl = 32.dp

    val sectionGap = 24.dp
    val pagePaddingH = 16.dp
    val pageBottomPadding = 32.dp
    val cardCornerRadius = 16.dp
}
