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
import dev.gopes.hinducalendar.data.repository.PreferencesRepository
import dev.gopes.hinducalendar.engine.SacredTextService
import kotlinx.coroutines.flow.first
import timber.log.Timber

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
        const val NOTIFICATION_ID = 1001
        const val WORK_NAME = "daily_briefing_notification"
    }

    override suspend fun doWork(): Result {
        return try {
            createNotificationChannel()

            val preferencesRepository = PreferencesRepository(appContext)
            val preferences = preferencesRepository.preferencesFlow.first()
            val sacredTextService = SacredTextService(appContext)

            val dailyContent = sacredTextService.getDailyContent(
                path = preferences.dharmaPath,
                progress = preferences.readingProgress,
                lang = preferences.language
            )

            val title = buildString {
                append(appContext.getString(R.string.notif_daily_wisdom))
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
                } ?: append(appContext.getString(R.string.notif_fallback_body))
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

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Daily notification worker failed")
            Result.retry()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                appContext.getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = appContext.getString(R.string.notif_channel_description)
            }
            val notificationManager =
                appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
