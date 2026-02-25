package dev.gopes.hinducalendar.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import dev.gopes.hinducalendar.data.model.DiyaState

class SacredDiyaWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = readPreferences(context)
        val diyaState = prefs?.diyaState ?: DiyaState()

        provideContent {
            DiyaWidgetContent(diyaState)
        }
    }

    @Composable
    private fun DiyaWidgetContent(state: DiyaState) {
        val statusText = if (state.isLitToday) "Lit Today" else "Tap to Light"
        val streakText = "${state.lightingStreak} day streak"
        val flameEmoji = if (state.isLitToday) "\uD83D\uDD6F\uFE0F" else "\uD83D\uDD6F"

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(12.dp)
                .background(GlanceTheme.colors.surface)
                .clickable(actionRunCallback<LaunchDiyaAction>()),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Text(
                text = "Sacred Diya",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = GlanceTheme.colors.primary
                )
            )
            Spacer(GlanceModifier.height(6.dp))
            Text(
                text = flameEmoji,
                style = TextStyle(fontSize = 32.sp)
            )
            Spacer(GlanceModifier.height(4.dp))
            Text(
                text = statusText,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (state.isLitToday) GlanceTheme.colors.primary
                    else GlanceTheme.colors.onSurfaceVariant
                )
            )
            if (state.lightingStreak > 0) {
                Spacer(GlanceModifier.height(2.dp))
                Text(
                    text = streakText,
                    style = TextStyle(
                        fontSize = 11.sp,
                        color = GlanceTheme.colors.onSurfaceVariant
                    )
                )
            }
        }
    }
}

class LaunchDiyaAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.putExtra("navigate_to", "diya")
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

class SacredDiyaWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SacredDiyaWidget()
}
