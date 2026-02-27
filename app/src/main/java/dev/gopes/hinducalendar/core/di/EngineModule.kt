package dev.gopes.hinducalendar.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.gopes.hinducalendar.data.engine.FestivalRulesEngine
import dev.gopes.hinducalendar.domain.repository.PanchangRepository
import dev.gopes.hinducalendar.data.repository.PanchangService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EngineModule {

    @Provides
    @Singleton
    fun provideFestivalRulesEngine(@ApplicationContext context: Context): FestivalRulesEngine {
        return FestivalRulesEngine(context)
    }

    @Provides
    @Singleton
    fun providePanchangRepository(festivalEngine: FestivalRulesEngine): PanchangRepository {
        return PanchangService(festivalEngine)
    }

    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}
