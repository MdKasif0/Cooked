package com.cooked.app.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Explicit schema migrations. Every version bump of [CookedDatabase] must add one here and
 * register it in [ALL_MIGRATIONS]; user data is never dropped on upgrade.
 */
object Migrations {

    /** v1 -> v2: adds the weekly Cooked Score snapshot table used by Insights history. */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `weekly_snapshots` (" +
                    "`weekStartEpochDay` INTEGER NOT NULL, " +
                    "`cookedScore` INTEGER NOT NULL, " +
                    "`recordedAt` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`weekStartEpochDay`))"
            )
        }
    }

    val ALL_MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_1_2)
}
