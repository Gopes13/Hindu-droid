package dev.gopes.hinducalendar.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.gopes.hinducalendar.MainActivity
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.data.model.DharmaPath
import dev.gopes.hinducalendar.data.model.ReadingProgress
import dev.gopes.hinducalendar.data.model.UserPreferences
import dev.gopes.hinducalendar.engine.SacredTextService

/**
 * A WorkManager CoroutineWorker that creates a morning daily briefing notification
 * with the user's daily sacred text content.
 */
class DailyNotificationWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val CHANNEL_ID = "daily_briefing"
        const val CHANNEL_NAME = "Daily Briefing"
        const val CHANNEL_DESCRIPTION = "Morning notification with daily Panchang and sacred text wisdom"
        const val NOTIFICATION_ID = 1001
        const val WORK_NAME = "daily_briefing_notification"
    }

    override suspend fun doWork(): Result {
        createNotificationChannel()

        // In production, load preferences from SharedPreferences / DataStore
        val preferences = UserPreferences()
        val sacredTextService = SacredTextService(appContext)

        val dailyContent = sacredTextService.getDailyContent(
            path = preferences.dharmaPath,
            progress = preferences.readingProgress,
            lang = preferences.language
        )

        val title = buildString {
            append("Daily Wisdom")
            dailyContent.primaryVerse?.let {
                append(" - ${it.title}")
            }
        }

        val body = buildString {
            dailyContent.primaryVerse?.let { verse ->
                append(verse.subtitle)
                append("\n")
                append(verse.translation.take(150))
                if (verse.translation.length > 150) append("...")
            } ?: append("Open the app for today's Panchang and sacred readings.")
        }

        // Build the intent to open the app
        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        else PendingIntent.FLAG_UPDATE_CURRENT
        val pendingIntent = PendingIntent.getActivity(appContext, 0, intent, flags)

        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)

        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            val notificationManager =
                appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
