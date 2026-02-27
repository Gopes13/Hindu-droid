package dev.gopes.hinducalendar.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import dev.gopes.hinducalendar.domain.model.UserPreferences
import dev.gopes.hinducalendar.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "hindu_calendar_prefs")

@Singleton
class DataStorePreferencesRepository @Inject constructor(
    private val context: Context
) : PreferencesRepository {
    private val gson = Gson()
    private val prefsKey = stringPreferencesKey("user_preferences")

    private val _errors = MutableSharedFlow<String>(extraBufferCapacity = 1)
    override val errors: SharedFlow<String> = _errors.asSharedFlow()

    override val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val json = prefs[prefsKey]
        if (json != null) {
            try {
                gson.fromJson(json, UserPreferences::class.java)
            } catch (e: Exception) {
                Timber.e(e, "Failed to deserialize preferences, using defaults")
                _errors.tryEmit("Failed to load preferences: ${e.message}")
                UserPreferences()
            }
        } else {
            UserPreferences()
        }
    }

    override suspend fun save(preferences: UserPreferences) {
        try {
            context.dataStore.edit { prefs ->
                prefs[prefsKey] = gson.toJson(preferences)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to save preferences")
            _errors.tryEmit("Failed to save preferences: ${e.message}")
        }
    }

    override suspend fun update(transform: (UserPreferences) -> UserPreferences) {
        try {
            context.dataStore.edit { prefs ->
                val current = prefs[prefsKey]?.let {
                    try {
                        gson.fromJson(it, UserPreferences::class.java)
                    } catch (e: Exception) {
                        _errors.tryEmit("Failed to read preferences: ${e.message}")
                        UserPreferences()
                    }
                } ?: UserPreferences()
                prefs[prefsKey] = gson.toJson(transform(current))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to update preferences")
            _errors.tryEmit("Failed to update preferences: ${e.message}")
        }
    }
}
