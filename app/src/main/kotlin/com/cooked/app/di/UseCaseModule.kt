package com.cooked.app.di

import com.cooked.app.alarm.AlarmScheduler
import com.cooked.app.domain.repository.ReminderRepository
import com.cooked.app.domain.usecase.CalculateCookedScoreUseCase
import com.cooked.app.domain.usecase.CalculatePressureUseCase
import com.cooked.app.domain.usecase.CompleteReminderUseCase
import com.cooked.app.domain.usecase.DeleteReminderUseCase
import com.cooked.app.domain.usecase.DismissNagUseCase
import com.cooked.app.domain.usecase.SaveReminderUseCase
import com.cooked.app.domain.usecase.SnoozeReminderUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideCalculatePressureUseCase(): CalculatePressureUseCase {
        return CalculatePressureUseCase()
    }

    @Provides
    @Singleton
    fun provideCalculateCookedScoreUseCase(): CalculateCookedScoreUseCase {
        return CalculateCookedScoreUseCase()
    }

    @Provides
    @Singleton
    fun provideSaveReminderUseCase(
        repository: ReminderRepository,
        alarmScheduler: AlarmScheduler
    ): SaveReminderUseCase {
        return SaveReminderUseCase(repository, alarmScheduler)
    }

    @Provides
    @Singleton
    fun provideCompleteReminderUseCase(
        repository: ReminderRepository,
        alarmScheduler: AlarmScheduler
    ): CompleteReminderUseCase {
        return CompleteReminderUseCase(repository, alarmScheduler)
    }

    @Provides
    @Singleton
    fun provideSnoozeReminderUseCase(
        repository: ReminderRepository,
        alarmScheduler: AlarmScheduler
    ): SnoozeReminderUseCase {
        return SnoozeReminderUseCase(repository, alarmScheduler)
    }

    @Provides
    @Singleton
    fun provideDismissNagUseCase(
        repository: ReminderRepository,
        alarmScheduler: AlarmScheduler
    ): DismissNagUseCase {
        return DismissNagUseCase(repository, alarmScheduler)
    }

    @Provides
    @Singleton
    fun provideDeleteReminderUseCase(
        repository: ReminderRepository,
        alarmScheduler: AlarmScheduler
    ): DeleteReminderUseCase {
        return DeleteReminderUseCase(repository, alarmScheduler)
    }
}

