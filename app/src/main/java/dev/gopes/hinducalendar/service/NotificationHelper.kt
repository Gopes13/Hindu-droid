package dev.gopes.hinducalendar.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.*
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.data.model.NotificationTime
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for managing notification channels and scheduling
 * the daily briefing WorkManager worker.
 */
@Singleton
class NotificationHelper @Inject constructor(
    private val context: Context
) {

    companion object {
        const val CHANNEL_ID_DAILY = DailyNotificationWorker.CHANNEL_ID
        const val CHANNEL_ID_FESTIVALS = "festival_reminders"
        const val CHANNEL_ID_PLAYBACK = "kirtan_playback"
    }

    /**
     * Creates all notification channels used by the app.
     * Safe to call multiple times; channels are only created once.
     */
    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Daily Briefing channel
            val dailyChannel = NotificationChannel(
                CHANNEL_ID_DAILY,
                context.getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notif_channel_description)
            }
            notificationManager.createNotificationChannel(dailyChannel)

            // Festival Reminders channel
            val festivalChannel = NotificationChannel(
                CHANNEL_ID_FESTIVALS,
                "Festival Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for upcoming Hindu festivals and observances"
            }
            notificationManager.createNotificationChannel(festivalChannel)

            // Kirtan Playback channel (low importance — no sound, just ongoing notification)
            val playbackChannel = NotificationChannel(
                CHANNEL_ID_PLAYBACK,
                "Kirtan Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controls for kirtan audio playback"
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(playbackChannel)
        }
    }

    /**
     * Schedules the daily briefing notification using WorkManager.
     * The worker runs once per day at the specified time.
     *
     * @param notificationTime The desired hour and minute for the notification.
     */
    fun scheduleDailyWorker(notificationTime: NotificationTime = NotificationTime()) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, notificationTime.hour)
            set(Calendar.MINUTE, notificationTime.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If the target time has already passed today, schedule for tomorrow
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_MONTH, 1)
        }

        val initialDelayMillis = target.timeInMillis - now.timeInMillis

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyNotificationWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .addTag(DailyNotificationWorker.WORK_NAME)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DailyNotificationWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyWorkRequest
        )
    }

    /**
     * Cancels the daily briefing notification worker.
     */
    fun cancelDailyWorker() {
        WorkManager.getInstance(context).cancelUniqueWork(DailyNotificationWorker.WORK_NAME)
    }
}
