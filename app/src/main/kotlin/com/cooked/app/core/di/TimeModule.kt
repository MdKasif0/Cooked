package com.cooked.app.core.di

import com.cooked.app.core.time.CookedClock
import com.cooked.app.core.time.SystemCookedClock
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TimeModule {

    @Binds
    @Singleton
    abstract fun bindCookedClock(
        impl: SystemCookedClock
    ): CookedClock
}

