package dev.gopes.hinducalendar

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dev.gopes.hinducalendar.engine.AudioPlayerService
import dev.gopes.hinducalendar.service.NotificationHelper
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class HinduCalendarApp : Application() {

    @Inject lateinit var audioPlayerService: AudioPlayerService
    @Inject lateinit var notificationHelper: NotificationHelper

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        notificationHelper.createNotificationChannels()
        audioPlayerService.loadManifest()
    }
}
