package dev.gopes.hinducalendar.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.gopes.hinducalendar.engine.FestivalRulesEngine
import dev.gopes.hinducalendar.engine.PanchangService
import dev.gopes.hinducalendar.service.CalendarSyncService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

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
}
