package com.stitch.app.di

import android.content.Context
import com.stitch.app.data.repository.AuthRepository
import com.stitch.app.data.repository.FriendsRepository
import com.stitch.app.data.repository.PactsRepository
import com.stitch.app.data.repository.ScheduleRepository
import com.stitch.app.data.repository.UsageRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository = AuthRepository()

    @Provides
    @Singleton
    fun provideFriendsRepository(): FriendsRepository = FriendsRepository()

    @Provides
    @Singleton
    fun providePactsRepository(): PactsRepository = PactsRepository()

    @Provides
    @Singleton
    fun provideScheduleRepository(): ScheduleRepository = ScheduleRepository()

    @Provides
    @Singleton
    fun provideUsageRepository(
        @ApplicationContext context: Context
    ): UsageRepository = UsageRepository(context)
}
