package com.mayor.kavi.di

import android.content.Context
import com.mayor.kavi.data.manager.*
import com.mayor.kavi.data.manager.games.*
import com.mayor.kavi.data.service.GameTrackerImpl
import com.mayor.kavi.data.service.GameTracker
import dagger.*
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GameModule {
    @Binds
    @Singleton
    abstract fun bindGameTracker(gameTrackerImpl: GameTrackerImpl): GameTracker

    companion object {
        @Provides
        @Singleton
        fun provideDiceManager(): DiceManager = DiceManager()

        @Provides
        @Singleton
        fun providePigGameManager(
            statisticsManager: StatisticsManager,
            gameTracker: GameTracker
        ): PigGameManager = PigGameManager(statisticsManager, gameTracker)

        @Provides
        @Singleton
        fun provideGreedGameManager(
            statisticsManager: StatisticsManager,
            gameTracker: GameTracker
        ): GreedGameManager = GreedGameManager(statisticsManager, gameTracker)

        @Provides
        @Singleton
        fun provideBalutGameManager(
            statisticsManager: StatisticsManager,
            gameTracker: GameTracker
        ): BalutGameManager = BalutGameManager(statisticsManager, gameTracker)

        @Provides
        @Singleton
        fun provideMyGameManager(): MyGameManager = MyGameManager()

        @Provides
        @Singleton
        fun provideShakeDetectionService(
            @ApplicationContext context: Context,
            settingsManager: SettingsManager,
            @AppModule.IoDispatcher dispatcher: CoroutineDispatcher
        ): ShakeDetectionManager = ShakeDetectionManager(context, settingsManager, dispatcher)

        @Provides
        @Singleton
        @AppModule.GameScope
        fun provideGameCoroutineScope(): CoroutineScope =
            CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
} 