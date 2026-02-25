package dev.gopes.hinducalendar.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.stringPreferencesKey
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
import com.google.gson.Gson
import dev.gopes.hinducalendar.data.model.JapaState
import dev.gopes.hinducalendar.data.model.UserPreferences
import java.io.File

class JapaMalaWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = readPreferences(context)
        val japaState = prefs?.japaState ?: JapaState()

        provideContent {
            JapaWidgetContent(japaState)
        }
    }

    @Composable
    private fun JapaWidgetContent(state: JapaState) {
        val beadText = "${state.currentBead} / 108"
        val roundsText = "${state.roundsToday} rounds today"
        val streakText = "${state.japaStreak} day streak"
        val mantraText = state.selectedMantra.displayText()

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(12.dp)
                .background(GlanceTheme.colors.surface)
                .clickable(actionRunCallback<LaunchJapaAction>()),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Text(
                text = "Japa Mala",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = GlanceTheme.colors.primary
                )
            )
            Spacer(GlanceModifier.height(4.dp))
            Text(
                text = beadText,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = GlanceTheme.colors.onSurface
                )
            )
            Spacer(GlanceModifier.height(4.dp))
            Text(
                text = roundsText,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
            if (state.japaStreak > 0) {
                Text(
                    text = streakText,
                    style = TextStyle(
                        fontSize = 11.sp,
                        color = GlanceTheme.colors.onSurfaceVariant
                    )
                )
            }
            Spacer(GlanceModifier.height(2.dp))
            Text(
                text = mantraText,
                style = TextStyle(
                    fontSize = 10.sp,
                    color = GlanceTheme.colors.primary
                ),
                maxLines = 1
            )
        }
    }
}

class LaunchJapaAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.putExtra("navigate_to", "japa")
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

class JapaMalaWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = JapaMalaWidget()
}

// Shared utility to read UserPreferences from DataStore
internal fun readPreferences(context: Context): UserPreferences? {
    return try {
        val prefsFile = File(context.filesDir, "datastore/hindu_calendar_prefs.preferences_pb")
        if (!prefsFile.exists()) return null
        // Read from DataStore's underlying file via the preferences key
        // Since DataStore uses protobuf, we use the Gson-based approach through shared prefs bridge
        val sharedPrefs = context.getSharedPreferences("widget_bridge", Context.MODE_PRIVATE)
        val json = sharedPrefs.getString("user_preferences", null) ?: return null
        Gson().fromJson(json, UserPreferences::class.java)
    } catch (e: Exception) {
        null
    }
}
