package dev.gopes.hinducalendar.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import com.google.gson.Gson
import dev.gopes.hinducalendar.domain.model.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Bridges DataStore preferences to a SharedPreferences file that Glance widgets can read,
 * then triggers widget updates.
 *
 * Call this after every preferences update that affects japa or diya state.
 */
object WidgetDataSyncHelper {

    private const val BRIDGE_PREFS = "widget_bridge"
    private const val KEY_USER_PREFS = "user_preferences"

    suspend fun syncAndUpdate(context: Context, prefs: UserPreferences) {
        withContext(Dispatchers.IO) {
            val json = Gson().toJson(prefs)
            context.getSharedPreferences(BRIDGE_PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_USER_PREFS, json)
                .apply()
        }
        // Update all widget instances
        try {
            JapaMalaWidget().updateAll(context)
            SacredDiyaWidget().updateAll(context)
        } catch (_: Exception) {
            // Widgets may not be placed yet
        }
    }
}
