package com.marine.fishtank.hilt

import android.content.Context
import com.marine.fishtank.SettingsRepository
import com.marine.fishtank.api.FishService
import com.marine.fishtank.api.RetrofitBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Module {

    @Provides
    fun fishService(): FishService = RetrofitBuilder.fishService

    @Singleton
    @Provides
    fun provideSettingRepository(@ApplicationContext context: Context) = SettingsRepository(context)
}