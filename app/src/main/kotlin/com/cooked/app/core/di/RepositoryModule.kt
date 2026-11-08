package com.cooked.app.core.di

import com.cooked.app.core.telemetry.Telemetry
import com.cooked.app.core.telemetry.TelemetryImpl
import com.cooked.app.data.billing.EntitlementRepositoryImpl
import com.cooked.app.data.billing.RevenueCatBillingManager
import com.cooked.app.data.repository.ItemRepositoryImpl
import com.cooked.app.data.repository.OfflineReminderRepository
import com.cooked.app.domain.billing.BillingManager
import com.cooked.app.domain.billing.EntitlementRepository
import com.cooked.app.domain.repository.ItemRepository
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
    abstract fun bindItemRepository(
        impl: ItemRepositoryImpl
    ): ItemRepository

    @Binds
    @Singleton
    abstract fun bindReminderRepository(
        impl: OfflineReminderRepository
    ): ReminderRepository

    @Binds
    @Singleton
    abstract fun bindEntitlementRepository(
        impl: EntitlementRepositoryImpl
    ): EntitlementRepository

    @Binds
    @Singleton
    abstract fun bindBillingManager(
        impl: RevenueCatBillingManager
    ): BillingManager

    @Binds
    @Singleton
    abstract fun bindTelemetry(
        impl: TelemetryImpl
    ): Telemetry
}
