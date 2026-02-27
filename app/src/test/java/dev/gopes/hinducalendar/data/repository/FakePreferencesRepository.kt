package dev.gopes.hinducalendar.data.repository

import dev.gopes.hinducalendar.domain.model.UserPreferences
import dev.gopes.hinducalendar.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update

/**
 * In-memory fake for unit testing ViewModels.
 * Implements [PreferencesRepository] without DataStore.
 */
class FakePreferencesRepository(
    initial: UserPreferences = UserPreferences()
) : PreferencesRepository {
    private val _state = MutableStateFlow(initial)

    override val preferencesFlow: Flow<UserPreferences> = _state

    private val _errors = MutableSharedFlow<String>(extraBufferCapacity = 1)
    override val errors: SharedFlow<String> = _errors.asSharedFlow()

    /** Snapshot of the current value for assertions. */
    val current: UserPreferences get() = _state.value

    override suspend fun save(preferences: UserPreferences) {
        _state.value = preferences
    }

    override suspend fun update(transform: (UserPreferences) -> UserPreferences) {
        _state.update(transform)
    }
}
