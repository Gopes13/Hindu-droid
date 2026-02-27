package dev.gopes.hinducalendar.core.ui.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dev.gopes.hinducalendar.R
import kotlinx.coroutines.flow.Flow

@Composable
fun rememberErrorSnackbarState(
    errorFlow: Flow<String>,
    onRetry: (() -> Unit)? = null
): SnackbarHostState {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(errorFlow) {
        errorFlow.collect { message ->
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = if (onRetry != null) context.getString(R.string.common_retry) else null,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                onRetry?.invoke()
            }
        }
    }

    return snackbarHostState
}
