package dev.gopes.hinducalendar

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import dev.gopes.hinducalendar.domain.repository.AudioPlaybackRepository
import dev.gopes.hinducalendar.data.service.NotificationHelper
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class HinduCalendarApp : Application(), Configuration.Provider {

    @Inject lateinit var audioPlaybackRepository: AudioPlaybackRepository
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        notificationHelper.createNotificationChannels()
        audioPlaybackRepository.loadManifest()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
