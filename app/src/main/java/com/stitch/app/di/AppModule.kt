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
    fun provideAuthRepository(
        @ApplicationContext context: Context
    ): AuthRepository = AuthRepository(context)

    @Provides
    @Singleton
    fun provideFriendsRepository(
        @ApplicationContext context: Context,
        authRepository: AuthRepository
    ): FriendsRepository = FriendsRepository(context, authRepository)

    @Provides
    @Singleton
    fun providePactsRepository(
        @ApplicationContext context: Context,
        authRepository: AuthRepository
    ): PactsRepository = PactsRepository(context, authRepository)

    @Provides
    @Singleton
    fun provideScheduleRepository(
        @ApplicationContext context: Context,
        authRepository: AuthRepository
    ): ScheduleRepository = ScheduleRepository(context, authRepository)

    @Provides
    @Singleton
    fun provideUsageRepository(
        @ApplicationContext context: Context
    ): UsageRepository = UsageRepository(context)
}
