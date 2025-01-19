package com.mayor.kavi.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.mayor.kavi.data.manager.*
import com.mayor.kavi.data.service.RoboflowService
import com.mayor.kavi.data.repository.*
import dagger.*
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.*

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class GameScope

    @Retention(AnnotationRetention.BINARY)
    @Qualifier
    annotation class IoDispatcher

    // Network Dependencies
    @Provides
    @Singleton
    fun provideRoboflowService(): RoboflowService {
        return Retrofit.Builder()
            .baseUrl("https://detect.roboflow.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RoboflowService::class.java)
    }

    // Repositories
    @Provides
    @Singleton
    fun provideRoboflowRepository(
        roboflowService: RoboflowService
    ): RoboflowRepository = RoboflowRepositoryImpl(roboflowService)

    // Utility Managers
    @Provides
    @Singleton
    fun provideDataStoreManager(@ApplicationContext context: Context): DataStoreManager =
        DataStoreManager(context)

    @Provides
    @Singleton
    fun provideSettingsManager(@ApplicationContext context: Context): SettingsManager =
        SettingsManager(context)

    @Provides
    @Singleton
    fun provideStatisticsManager(
        @ApplicationContext context: Context,
        @IoDispatcher dispatcher: CoroutineDispatcher
    ): StatisticsManager = StatisticsManager(context, dispatcher)

    // Coroutines & Threading
    @Provides
    @Singleton
    @GameScope
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    @Singleton
    fun provideAuthCoroutineScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @IoDispatcher
    @Provides
    @Singleton
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
