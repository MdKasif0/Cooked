package com.cooked.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cooked.app.core.designsystem.ThemeMode
import com.cooked.app.domain.model.Personality
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cooked_user_prefs")

data class UserPrefs(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = false,
    val defaultNagInterval: Int = 10,
    val quietHoursStart: LocalTime = LocalTime.of(22, 0),
    val quietHoursEnd: LocalTime = LocalTime.of(8, 0),
    val weekStartsOn: DayOfWeek = DayOfWeek.MONDAY,
    val onboardingDone: Boolean = false,
    val personality: Personality = Personality.SPICY,
    val hapticsOn: Boolean = true,
    val firstDayUsed: LocalDate? = null,
    val isPro: Boolean = false,
    val dailyCapacityMinutes: Int = 120,
    val preferredNudgeTime: LocalTime = LocalTime.of(18, 0),
    val telemetryEnabled: Boolean = true,
    val toastyCompletionsCount: Int = 0,
    val lastReviewPromptEpochDay: Long = 0L,
    val hinglishVibes: Boolean = false,
    val soundNormalNag: String = "sizzle_soft",
    val soundEscalatedNag: String = "sizzle_loud",
    val soundCritical: String = "kettle",
    val inAppSoundsEnabled: Boolean = true,
    val appIconChoice: String = "DEFAULT"
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val DEFAULT_NAG_INTERVAL = intPreferencesKey("default_nag_interval")
        val QUIET_HOURS_START = stringPreferencesKey("quiet_hours_start")
        val QUIET_HOURS_END = stringPreferencesKey("quiet_hours_end")
        val WEEK_STARTS_ON = stringPreferencesKey("week_starts_on")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val PERSONALITY = stringPreferencesKey("personality")
        val HAPTICS_ON = booleanPreferencesKey("haptics_on")
        val FIRST_DAY_USED = stringPreferencesKey("first_day_used")
        val IS_PRO = booleanPreferencesKey("is_pro")
        val OEM_WARNING_DISMISSED = booleanPreferencesKey("oem_warning_dismissed")
        val DAILY_CAPACITY_MINUTES = intPreferencesKey("daily_capacity_minutes")
        val PREFERRED_NUDGE_TIME = stringPreferencesKey("preferred_nudge_time")
        val TELEMETRY_ENABLED = booleanPreferencesKey("telemetry_enabled")
        val TOASTY_COMPLETIONS_COUNT = intPreferencesKey("toasty_completions_count")
        val LAST_REVIEW_PROMPT_EPOCH_DAY = androidx.datastore.preferences.core.longPreferencesKey("last_review_prompt_epoch_day")
    }

    val userPrefs: Flow<UserPrefs> = context.dataStore.data.map { preferences ->
        UserPrefs(
            themeMode = parseEnum(preferences[PreferencesKeys.THEME_MODE], ThemeMode.SYSTEM),
            dynamicColor = preferences[PreferencesKeys.DYNAMIC_COLOR] ?: false,
            defaultNagInterval = preferences[PreferencesKeys.DEFAULT_NAG_INTERVAL] ?: 10,
            quietHoursStart = parseTime(preferences[PreferencesKeys.QUIET_HOURS_START], LocalTime.of(22, 0)),
            quietHoursEnd = parseTime(preferences[PreferencesKeys.QUIET_HOURS_END], LocalTime.of(8, 0)),
            weekStartsOn = parseEnum(preferences[PreferencesKeys.WEEK_STARTS_ON], DayOfWeek.MONDAY),
            onboardingDone = preferences[PreferencesKeys.ONBOARDING_DONE] ?: false,
            personality = parseEnum(preferences[PreferencesKeys.PERSONALITY], Personality.SPICY),
            hapticsOn = preferences[PreferencesKeys.HAPTICS_ON] ?: true,
            firstDayUsed = parseDate(preferences[PreferencesKeys.FIRST_DAY_USED]),
            isPro = preferences[PreferencesKeys.IS_PRO] ?: false,
            dailyCapacityMinutes = preferences[PreferencesKeys.DAILY_CAPACITY_MINUTES] ?: 120,
            preferredNudgeTime = parseTime(preferences[PreferencesKeys.PREFERRED_NUDGE_TIME], LocalTime.of(18, 0)),
            telemetryEnabled = preferences[PreferencesKeys.TELEMETRY_ENABLED] ?: true,
            toastyCompletionsCount = preferences[PreferencesKeys.TOASTY_COMPLETIONS_COUNT] ?: 0,
            lastReviewPromptEpochDay = preferences[PreferencesKeys.LAST_REVIEW_PROMPT_EPOCH_DAY] ?: 0L
        )
    }

    val themeMode: Flow<ThemeMode> = userPrefs.map { it.themeMode }
    val dynamicColor: Flow<Boolean> = userPrefs.map { it.dynamicColor }
    val defaultNagInterval: Flow<Int> = userPrefs.map { it.defaultNagInterval }
    val defaultNagIntervalMinutes: Flow<Int> = defaultNagInterval
    val dailyCapacityMinutes: Flow<Int> = userPrefs.map { it.dailyCapacityMinutes }
    val preferredNudgeTime: Flow<LocalTime> = userPrefs.map { it.preferredNudgeTime }
    val isOemWarningDismissed: Flow<Boolean> = context.dataStore.data.map {
        it[PreferencesKeys.OEM_WARNING_DISMISSED] ?: false
    }
    val personality: Flow<Personality> = userPrefs.map { it.personality }
    val isPro: Flow<Boolean> = userPrefs.map { it.isPro }
    val hapticsOn: Flow<Boolean> = userPrefs.map { it.hapticsOn }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[PreferencesKeys.THEME_MODE] = mode.name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.DYNAMIC_COLOR] = enabled }
    }

    suspend fun setDefaultNagInterval(minutes: Int) {
        context.dataStore.edit { it[PreferencesKeys.DEFAULT_NAG_INTERVAL] = minutes }
    }

    suspend fun setDefaultNagIntervalMinutes(minutes: Int) {
        setDefaultNagInterval(minutes)
    }

    suspend fun setQuietHours(start: LocalTime, end: LocalTime) {
        context.dataStore.edit {
            it[PreferencesKeys.QUIET_HOURS_START] = start.toString()
            it[PreferencesKeys.QUIET_HOURS_END] = end.toString()
        }
    }

    suspend fun setWeekStartsOn(dayOfWeek: DayOfWeek) {
        context.dataStore.edit { it[PreferencesKeys.WEEK_STARTS_ON] = dayOfWeek.name }
    }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.ONBOARDING_DONE] = done }
    }

    suspend fun setPersonality(personality: Personality) {
        context.dataStore.edit { it[PreferencesKeys.PERSONALITY] = personality.name }
    }

    suspend fun setHapticsOn(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.HAPTICS_ON] = enabled }
    }

    suspend fun setFirstDayUsed(date: LocalDate) {
        context.dataStore.edit { it[PreferencesKeys.FIRST_DAY_USED] = date.toString() }
    }

    suspend fun setIsPro(isPro: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.IS_PRO] = isPro }
    }

    suspend fun setOemWarningDismissed(dismissed: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.OEM_WARNING_DISMISSED] = dismissed }
    }

    suspend fun setDailyCapacityMinutes(minutes: Int) {
        context.dataStore.edit { it[PreferencesKeys.DAILY_CAPACITY_MINUTES] = minutes }
    }

    suspend fun setPreferredNudgeTime(time: LocalTime) {
        context.dataStore.edit { it[PreferencesKeys.PREFERRED_NUDGE_TIME] = time.toString() }
    }

    suspend fun setTelemetryEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.TELEMETRY_ENABLED] = enabled }
    }

    suspend fun incrementToastyCompletions(): Int {
        var newCount = 0
        context.dataStore.edit { prefs ->
            val current = prefs[PreferencesKeys.TOASTY_COMPLETIONS_COUNT] ?: 0
            newCount = current + 1
            prefs[PreferencesKeys.TOASTY_COMPLETIONS_COUNT] = newCount
        }
        return newCount
    }

    suspend fun setLastReviewPromptEpochDay(epochDay: Long) {
        context.dataStore.edit { it[PreferencesKeys.LAST_REVIEW_PROMPT_EPOCH_DAY] = epochDay }
    }

    suspend fun resetForTesting() {
        context.dataStore.edit { it.clear() }
    }

    private companion object {
        inline fun <reified T : Enum<T>> parseEnum(value: String?, default: T): T {
            if (value == null) return default
            return try {
                java.lang.Enum.valueOf(T::class.java, value)
            } catch (_: Exception) {
                default
            }
        }

        fun parseTime(value: String?, default: LocalTime): LocalTime {
            if (value == null) return default
            return try {
                LocalTime.parse(value)
            } catch (_: Exception) {
                default
            }
        }

        fun parseDate(value: String?): LocalDate? {
            if (value == null) return null
            return try {
                LocalDate.parse(value)
            } catch (_: Exception) {
                null
            }
        }
    }
}
