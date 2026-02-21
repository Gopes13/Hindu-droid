package dev.gopes.hinducalendar.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.gopes.hinducalendar.data.repository.PreferencesRepository
import dev.gopes.hinducalendar.engine.AudioDownloadManager
import dev.gopes.hinducalendar.engine.AudioPlayerService
import dev.gopes.hinducalendar.engine.FestivalRulesEngine
import dev.gopes.hinducalendar.engine.GamificationService
import dev.gopes.hinducalendar.engine.PanchangService
import dev.gopes.hinducalendar.engine.SacredTextService
import dev.gopes.hinducalendar.service.CalendarSyncService
import dev.gopes.hinducalendar.service.NotificationHelper
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePreferencesRepository(@ApplicationContext context: Context): PreferencesRepository {
        return PreferencesRepository(context)
    }

    @Provides
    @Singleton
    fun provideFestivalRulesEngine(@ApplicationContext context: Context): FestivalRulesEngine {
        return FestivalRulesEngine(context)
    }

    @Provides
    @Singleton
    fun providePanchangService(festivalEngine: FestivalRulesEngine): PanchangService {
        return PanchangService(festivalEngine)
    }

    @Provides
    @Singleton
    fun provideCalendarSyncService(@ApplicationContext context: Context): CalendarSyncService {
        return CalendarSyncService(context)
    }

    @Provides
    @Singleton
    fun provideSacredTextService(@ApplicationContext context: Context): SacredTextService {
        return SacredTextService(context)
    }

    @Provides
    @Singleton
    fun provideNotificationHelper(@ApplicationContext context: Context): NotificationHelper {
        return NotificationHelper(context)
    }

    @Provides
    @Singleton
    fun provideGamificationService(@ApplicationContext context: Context): GamificationService {
        return GamificationService(context)
    }

    @Provides
    @Singleton
    fun provideAudioDownloadManager(@ApplicationContext context: Context): AudioDownloadManager {
        return AudioDownloadManager(context)
    }

    @Provides
    @Singleton
    fun provideAudioPlayerService(
        @ApplicationContext context: Context,
        downloadManager: AudioDownloadManager
    ): AudioPlayerService {
        return AudioPlayerService(context, downloadManager)
    }
}
