package dev.gopes.hinducalendar.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.gopes.hinducalendar.data.repository.DataStorePreferencesRepository
import dev.gopes.hinducalendar.domain.repository.PreferencesRepository
import dev.gopes.hinducalendar.domain.service.GamificationService
import dev.gopes.hinducalendar.data.repository.GamificationServiceImpl
import dev.gopes.hinducalendar.domain.repository.KirtanRepository
import dev.gopes.hinducalendar.data.repository.KirtanService
import dev.gopes.hinducalendar.domain.repository.SacredTextRepository
import dev.gopes.hinducalendar.data.repository.SacredTextService
import dev.gopes.hinducalendar.domain.repository.SanskritRepository
import dev.gopes.hinducalendar.data.repository.SanskritCurriculumService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun providePreferencesRepository(@ApplicationContext context: Context): PreferencesRepository {
        return DataStorePreferencesRepository(context)
    }

    @Provides
    @Singleton
    fun provideSacredTextRepository(@ApplicationContext context: Context): SacredTextRepository {
        return SacredTextService(context)
    }

    @Provides
    @Singleton
    fun provideKirtanRepository(@ApplicationContext context: Context): KirtanRepository {
        return KirtanService(context)
    }

    @Provides
    @Singleton
    fun provideGamificationService(@ApplicationContext context: Context): GamificationService {
        return GamificationServiceImpl(context)
    }

    @Provides
    @Singleton
    fun provideSanskritRepository(@ApplicationContext context: Context): SanskritRepository {
        return SanskritCurriculumService(context)
    }
}
