package dev.gopes.hinducalendar.core.di

import android.content.Context
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.gopes.hinducalendar.data.engine.AudioDownloadManager
import dev.gopes.hinducalendar.domain.repository.AudioPlaybackRepository
import dev.gopes.hinducalendar.data.repository.AudioPlayerService
import dev.gopes.hinducalendar.data.service.CalendarSyncService
import dev.gopes.hinducalendar.data.service.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Provides
    @Singleton
    fun provideAudioDownloadManager(
        @ApplicationContext context: Context,
        scope: CoroutineScope
    ): AudioDownloadManager {
        return AudioDownloadManager(context, scope)
    }

    @Provides
    @Singleton
    fun provideAudioPlaybackRepository(
        @ApplicationContext context: Context,
        downloadManager: AudioDownloadManager
    ): AudioPlaybackRepository {
        return AudioPlayerService(context, downloadManager)
    }

    @Provides
    @Singleton
    fun provideCalendarSyncService(@ApplicationContext context: Context): CalendarSyncService {
        return CalendarSyncService(context)
    }

    @Provides
    @Singleton
    fun provideNotificationHelper(@ApplicationContext context: Context): NotificationHelper {
        return NotificationHelper(context)
    }

    @Provides
    @Singleton
    fun provideReviewManager(@ApplicationContext context: Context): ReviewManager {
        return ReviewManagerFactory.create(context)
    }
}
