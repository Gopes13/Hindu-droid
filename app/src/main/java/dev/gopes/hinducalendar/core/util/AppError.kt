package dev.gopes.hinducalendar.core.util

import androidx.annotation.StringRes
import dev.gopes.hinducalendar.R

sealed interface AppError {
    val messageResId: Int

    data class DataLoadError(@StringRes override val messageResId: Int = R.string.error_data_load) : AppError
    data class NetworkError(@StringRes override val messageResId: Int = R.string.error_network) : AppError
    data class PersistenceError(@StringRes override val messageResId: Int = R.string.error_persistence) : AppError
}
