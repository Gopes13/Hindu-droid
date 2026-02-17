package dev.gopes.hinducalendar.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import dev.gopes.hinducalendar.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "hindu_calendar_prefs")

@Singleton
class PreferencesRepository @Inject constructor(
    private val context: Context
) {
    private val gson = Gson()
    private val prefsKey = stringPreferencesKey("user_preferences")

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val json = prefs[prefsKey]
        if (json != null) {
            try {
                gson.fromJson(json, UserPreferences::class.java)
            } catch (e: Exception) {
                Timber.e(e, "Failed to deserialize preferences, using defaults")
                UserPreferences()
            }
        } else {
            UserPreferences()
        }
    }

    suspend fun save(preferences: UserPreferences) {
        context.dataStore.edit { prefs ->
            prefs[prefsKey] = gson.toJson(preferences)
        }
    }

    suspend fun update(transform: (UserPreferences) -> UserPreferences) {
        context.dataStore.edit { prefs ->
            val current = prefs[prefsKey]?.let {
                try {
                    gson.fromJson(it, UserPreferences::class.java)
                } catch (e: Exception) {
                    UserPreferences()
                }
            } ?: UserPreferences()
            prefs[prefsKey] = gson.toJson(transform(current))
        }
    }
}
