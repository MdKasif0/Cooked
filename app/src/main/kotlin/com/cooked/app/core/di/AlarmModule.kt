package com.cooked.app.core.di

import com.cooked.app.scheduling.alarm.AlarmScheduler
import com.cooked.app.scheduling.alarm.AlarmSchedulerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AlarmModule {

    @Binds
    @Singleton
    abstract fun bindAlarmScheduler(
        impl: AlarmSchedulerImpl
    ): AlarmScheduler
}
