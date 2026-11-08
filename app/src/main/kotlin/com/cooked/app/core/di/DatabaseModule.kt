package com.cooked.app.core.di

import android.content.Context
import androidx.room.Room
import com.cooked.app.data.dao.ReminderDao
import com.cooked.app.data.db.CookedDatabase
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
            "cooked_database.db"
        ).fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    fun provideReminderDao(database: CookedDatabase): ReminderDao {
        return database.reminderDao()
    }
}
