package com.cooked.app.di

import com.cooked.app.data.repository.OfflineReminderRepository
import com.cooked.app.domain.repository.ReminderRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindReminderRepository(
        impl: OfflineReminderRepository
    ): ReminderRepository
}
