package dev.gopes.hinducalendar.ui.components

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

/**
 * Micro-interactions ported from iOS MicroInteractions.swift.
 */

// ── Sacred Press Feedback ───────────────────────────────────────────────────

/**
 * Scale to 0.96 + opacity 0.85 on press, with spring animation.
 * iOS: response=0.2, dampingFraction=0.65
 */
fun Modifier.sacredPress(
    onPress: () -> Unit = {}
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pressScale"
    )
    val pressAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pressAlpha"
    )

    this
        .scale(pressScale)
        .alpha(pressAlpha)
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    tryAwaitRelease()
                    isPressed = false
                    onPress()
                }
            )
        }
}

// ── Numeric Slot-Machine Transition ─────────────────────────────────────────

/**
 * AnimatedContent with slide-up for changing numbers (bead count, streak, etc.).
 * Matches iOS .numericText() content transition.
 */
@Composable
fun NumericTransition(
    value: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineMedium,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight = FontWeight.Bold,
    textAlign: TextAlign = TextAlign.Center,
    formatString: (Int) -> String = { it.toString() },
) {
    AnimatedContent(
        targetState = value,
        modifier = modifier,
        transitionSpec = {
            val direction = if (targetState > initialState) {
                // Counting up: slide in from bottom
                slideInVertically { it } + fadeIn(
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium)
                ) togetherWith slideOutVertically { -it } + fadeOut(
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium)
                )
            } else {
                // Counting down: slide in from top
                slideInVertically { -it } + fadeIn(
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium)
                ) togetherWith slideOutVertically { it } + fadeOut(
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium)
                )
            }
            direction using SizeTransform(clip = false)
        },
        label = "numericTransition"
    ) { targetValue ->
        Text(
            text = formatString(targetValue),
            style = style,
            color = color,
            fontWeight = fontWeight,
            textAlign = textAlign,
        )
    }
}

/**
 * Animated digit-by-digit transition for large numbers.
 * Each digit slides independently for a slot-machine effect.
 */
@Composable
fun SlotMachineNumber(
    value: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.displaySmall,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    val digits = value.toString()
    Row(modifier = modifier) {
        digits.forEachIndexed { index, _ ->
            val digitValue = digits.getOrNull(index)?.digitToIntOrNull() ?: 0
            AnimatedContent(
                targetState = digitValue,
                transitionSpec = {
                    slideInVertically { it } + fadeIn() togetherWith
                            slideOutVertically { -it } + fadeOut() using
                            SizeTransform(clip = false)
                },
                label = "digit_$index"
            ) { target ->
                Text(
                    text = target.toString(),
                    style = style,
                    color = color,
                )
            }
        }
    }
}

// ── Haptic Feedback Helper ──────────────────────────────────────────────────

object HapticHelper {

    fun selection(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    fun reveal(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    fun verseComplete(view: View) {
        if (Build.VERSION.SDK_INT >= 30) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    fun sectionComplete(view: View) {
        if (Build.VERSION.SDK_INT >= 30) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    fun celebration(view: View) {
        if (Build.VERSION.SDK_INT >= 30) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }
}
