package dev.gopes.hinducalendar.data.service

import android.app.Activity
import com.google.android.play.core.review.ReviewManager
import dev.gopes.hinducalendar.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InAppReviewHelper @Inject constructor(
    private val reviewManager: ReviewManager,
    private val preferencesRepository: PreferencesRepository
) {
    companion object {
        private const val MIN_DAYS_OPENED = 5
        private const val MIN_DAYS_BETWEEN_PROMPTS = 90
    }

    suspend fun maybeRequestReview(activity: Activity) {
        val prefs = preferencesRepository.preferencesFlow.first()
        val streak = prefs.streakData
        val gamification = prefs.gamificationData

        if (streak.totalDaysOpened < MIN_DAYS_OPENED) return

        val lastPrompt = gamification.lastReviewPromptDate
        if (lastPrompt != null) {
            val daysSince = try {
                val last = java.time.LocalDate.parse(lastPrompt)
                java.time.temporal.ChronoUnit.DAYS.between(last, java.time.LocalDate.now())
            } catch (_: Exception) { 0L }
            if (daysSince < MIN_DAYS_BETWEEN_PROMPTS) return
        }

        try {
            val reviewInfo = reviewManager.requestReviewFlow()
            reviewInfo.addOnSuccessListener { info ->
                reviewManager.launchReviewFlow(activity, info)
                // Mark that we prompted (fire-and-forget)
            }
            reviewInfo.addOnFailureListener { e ->
                Timber.w(e, "In-app review request failed")
            }

            preferencesRepository.update { current ->
                current.copy(
                    gamificationData = current.gamificationData.copy(
                        lastReviewPromptDate = java.time.LocalDate.now().toString()
                    )
                )
            }
        } catch (e: Exception) {
            Timber.w(e, "In-app review failed")
        }
    }
}
