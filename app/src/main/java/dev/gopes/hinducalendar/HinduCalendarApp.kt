package dev.gopes.hinducalendar

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dev.gopes.hinducalendar.engine.AudioPlayerService
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class HinduCalendarApp : Application() {

    @Inject lateinit var audioPlayerService: AudioPlayerService

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        audioPlayerService.loadManifest()
    }
}
