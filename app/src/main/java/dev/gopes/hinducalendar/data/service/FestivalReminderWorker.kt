package dev.gopes.hinducalendar.data.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.gopes.hinducalendar.MainActivity
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.domain.model.FestivalCategory
import dev.gopes.hinducalendar.domain.model.FestivalOccurrence
import dev.gopes.hinducalendar.domain.model.ReminderTiming
import dev.gopes.hinducalendar.domain.repository.PreferencesRepository
import dev.gopes.hinducalendar.domain.repository.PanchangRepository
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.LocalDate

/**
 * WorkManager worker that checks for upcoming festivals and posts
 * reminder notifications based on the user's reminder timing preferences.
 *
 * Scheduled daily by [NotificationHelper.scheduleFestivalWorker].
 */
@HiltWorker
class FestivalReminderWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val preferencesRepository: PreferencesRepository,
    private val panchangRepository: PanchangRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "festival_reminder_worker"
        private const val NOTIFICATION_ID_BASE = 2000
    }

    override suspend fun doWork(): Result {
        return try {
            val preferences = preferencesRepository.preferencesFlow.first()
            if (!preferences.notificationsEnabled) return Result.success()

            val today = LocalDate.now()
            val timings = preferences.reminderTimings
            val language = preferences.language

            // Collect festivals for the next 7 days
            val upcomingFestivals = (0..6).flatMap { offset ->
                val date = today.plusDays(offset.toLong())
                panchangRepository.computeFestivals(
                    date,
                    preferences.location,
                    preferences.tradition,
                    preferences.festivalDateReference
                )
            }.distinctBy { it.festival.id }

            // Filter to festivals that match a reminder timing for today
            val festivalsToNotify = upcomingFestivals.filter { occ ->
                shouldNotifyToday(today, occ, timings)
            }

            festivalsToNotify.forEachIndexed { index, occ ->
                postFestivalNotification(occ, language, index)
            }

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Festival reminder worker failed")
            Result.retry()
        }
    }

    private fun shouldNotifyToday(
        today: LocalDate,
        occurrence: FestivalOccurrence,
        timings: List<ReminderTiming>
    ): Boolean {
        val festivalDate = occurrence.date
        val daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, festivalDate).toInt()

        return timings.any { timing ->
            when (timing) {
                ReminderTiming.MORNING_OF -> daysUntil == 0
                ReminderTiming.EVENING_BEFORE -> daysUntil == 1
                ReminderTiming.DAY_BEFORE -> daysUntil == 1
                ReminderTiming.TWO_DAYS_BEFORE -> daysUntil == 2
            }
        }
    }

    private fun postFestivalNotification(
        occurrence: FestivalOccurrence,
        language: dev.gopes.hinducalendar.domain.model.AppLanguage,
        index: Int
    ) {
        val festival = occurrence.festival
        val name = festival.displayName(language)
        val daysUntil = java.time.temporal.ChronoUnit.DAYS.between(
            LocalDate.now(), occurrence.date
        ).toInt()

        val title = when {
            daysUntil == 0 -> appContext.getString(R.string.notif_festival_today, name)
            daysUntil == 1 -> appContext.getString(R.string.notif_festival_tomorrow, name)
            else -> appContext.getString(R.string.notif_festival_upcoming, name, daysUntil)
        }

        val body = buildString {
            val categoryLabel = when (festival.category) {
                FestivalCategory.MAJOR -> appContext.getString(R.string.notif_festival_major)
                FestivalCategory.VRAT -> appContext.getString(R.string.notif_festival_vrat)
                else -> ""
            }
            if (categoryLabel.isNotEmpty()) {
                append(categoryLabel)
                append(" \u2022 ")
            }
            append(occurrence.date.toString())
            val desc = festival.descriptionText(language)
            if (desc.isNotEmpty()) {
                append("\n")
                append(desc.take(150))
                if (desc.length > 150) append("...")
            }
        }

        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        else PendingIntent.FLAG_UPDATE_CURRENT
        val pendingIntent = PendingIntent.getActivity(
            appContext, NOTIFICATION_ID_BASE + index, intent, flags
        )

        val notification = NotificationCompat.Builder(
            appContext, NotificationHelper.CHANNEL_ID_FESTIVALS
        )
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_BASE + index, notification)
    }
}
