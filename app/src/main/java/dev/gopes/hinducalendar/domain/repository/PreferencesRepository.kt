package dev.gopes.hinducalendar.domain.repository

import dev.gopes.hinducalendar.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Contract for reading and writing user preferences.
 * Backed by DataStore in production; in-memory fake in tests.
 */
interface PreferencesRepository {
    val preferencesFlow: Flow<UserPreferences>
    /** Emits error messages when deserialization or persistence fails. */
    val errors: SharedFlow<String>
    suspend fun save(preferences: UserPreferences)
    suspend fun update(transform: (UserPreferences) -> UserPreferences)
}
