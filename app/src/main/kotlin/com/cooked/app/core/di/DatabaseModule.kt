package com.cooked.app.core.di

import android.content.Context
import androidx.room.Room
import com.cooked.app.data.dao.AnalyticsDao
import com.cooked.app.data.dao.ChecklistStepDao
import com.cooked.app.data.dao.ItemDao
import com.cooked.app.data.dao.ItemEventDao
import com.cooked.app.data.dao.ReminderDao
import com.cooked.app.data.db.CookedDatabase
import com.cooked.app.data.db.Migrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideCookedDatabase(
        @ApplicationContext context: Context
    ): CookedDatabase {
        return Room.databaseBuilder(
            context,
            CookedDatabase::class.java,
            CookedDatabase.DATABASE_NAME
        ).addMigrations(*Migrations.ALL_MIGRATIONS)
            // Only a downgrade (e.g. sideloading an older build) may reset the DB; upgrades must migrate.
            .fallbackToDestructiveMigrationOnDowngrade(true)
            .build()
    }

    @Provides
    fun provideItemDao(database: CookedDatabase): ItemDao {
        return database.itemDao()
    }

    @Provides
    fun provideChecklistStepDao(database: CookedDatabase): ChecklistStepDao {
        return database.checklistStepDao()
    }

    @Provides
    fun provideItemEventDao(database: CookedDatabase): ItemEventDao {
        return database.itemEventDao()
    }

    @Provides
    fun provideAnalyticsDao(database: CookedDatabase): AnalyticsDao {
        return database.analyticsDao()
    }

    @Provides
    fun provideReminderDao(database: CookedDatabase): ReminderDao {
        return database.reminderDao()
    }

    @Provides
    fun provideWeeklySnapshotDao(database: CookedDatabase): com.cooked.app.data.dao.WeeklySnapshotDao {
        return database.weeklySnapshotDao()
    }
}
