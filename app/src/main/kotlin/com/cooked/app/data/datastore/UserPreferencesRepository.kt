package com.cooked.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cooked_user_prefs")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val OEM_WARNING_DISMISSED = booleanPreferencesKey("oem_warning_dismissed")
        val DEFAULT_NAG_INTERVAL_MINUTES = intPreferencesKey("default_nag_interval_minutes")
    }

    val isOemWarningDismissed: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.OEM_WARNING_DISMISSED] ?: false
    }

    val defaultNagIntervalMinutes: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DEFAULT_NAG_INTERVAL_MINUTES] ?: 10
    }

    suspend fun setOemWarningDismissed(dismissed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.OEM_WARNING_DISMISSED] = dismissed
        }
    }

    suspend fun setDefaultNagIntervalMinutes(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_NAG_INTERVAL_MINUTES] = minutes
        }
    }
}

