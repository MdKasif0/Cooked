package com.cooked.app.domain.usecase

import com.cooked.app.core.time.CookedClock
import com.cooked.app.data.datastore.UserPreferencesRepository
import com.cooked.app.domain.deadline.HeatCalculator
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemKind
import com.cooked.app.domain.model.ItemStatus
import com.cooked.app.domain.model.NagPolicy
import com.cooked.app.domain.parse.QuickParser
import com.cooked.app.domain.repository.ItemRepository
import com.cooked.app.scheduling.alarm.AlarmScheduler
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gemini / Google Assistant / App Actions / External Intent entry point for creating reminders & deadlines.
 *
 * NOTE ON ASSISTANT INTEGRATION ARCHITECTURE:
 * -------------------------------------------
 * Android App Functions (`androidx.appfunctions:appfunctions`) was introduced in Android 16 developer
 * previews and is not yet available as a stable Maven dependency for production apps targeting SDK 34/35.
 * Google Assistant legacy Built-in Intents (BII) via `actions.xml` have been officially deprecated by Google
 * in favor of dynamic Android App Shortcuts and the upcoming AppFunctions library.
 *
 * This use case provides a clean, fully-tested abstraction (`CreateItemFromExternal`) that:
 * 1. Accepts raw voice/text queries ("submit physics assignment by friday 5pm").
 * 2. Parses natural language using our deterministic, zero-latency rule-based `QuickParser`.
 * 3. Applies user preferences (quiet hours, daily capacity, nudge time).
 * 4. Persists the item and schedules exact alarms.
 *
 * TODO (Android 16 AppFunctions):
 * When `androidx.appfunctions` reaches public stable release, create `CookedAppFunction` annotated with
 * `@AppFunction` that delegates directly to this usecase:
 * ```
 * @AppFunction
 * suspend fun createReminder(rawQuery: String): Item = createItemFromExternal(rawQuery)
 * ```
 */
@Singleton
class CreateItemFromExternal @Inject constructor(
    private val itemRepository: ItemRepository,
    private val alarmScheduler: AlarmScheduler,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val clock: CookedClock
) {

    suspend operator fun invoke(
        rawText: String,
        forcedKind: ItemKind? = null
    ): Result<Item> {
        val trimmed = rawText.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Cannot create reminder from empty text"))
        }

        val now = clock.now()
        val zone = clock.zone()
        val prefs = userPreferencesRepository.userPrefs.first()

        // 1. Natural language parse
        val parseResult = QuickParser.parse(trimmed, now, zone, prefs)
        val kind = forcedKind ?: parseResult.kindGuess ?: ItemKind.REMINDER
        val dueAt = parseResult.dueAt ?: now.plus(Duration.ofHours(1))

        // 2. Build domain item
        val tentativeItem = Item(
            id = UUID.randomUUID().toString(),
            kind = kind,
            title = parseResult.title.ifBlank { trimmed },
            note = null,
            emoji = if (kind == ItemKind.DEADLINE) "🎯" else "🔥",
            dueAt = dueAt,
            allDay = parseResult.allDay,
            timeZoneId = zone.id,
            recurrence = parseResult.recurrence,
            nag = NagPolicy(
                intervalMinutes = prefs.defaultNagInterval,
                escalate = true,
                maxNags = null,
                respectQuietHours = true
            ),
            critical = parseResult.critical,
            status = ItemStatus.ACTIVE,
            startBy = null,
            effortMinutes = parseResult.effortMinutes,
            progress = 0,
            createdAt = now,
            updatedAt = now
        )

        // 3. Start-by calculation for deadlines
        val startBy = if (kind == ItemKind.DEADLINE) {
            HeatCalculator.suggestStartBy(
                item = tentativeItem,
                now = now,
                dailyCapacityMinutes = prefs.dailyCapacityMinutes,
                preferredNudgeTime = prefs.preferredNudgeTime,
                zone = zone
            )
        } else null

        val item = tentativeItem.copy(startBy = startBy)

        // 4. Save and schedule alarm
        itemRepository.create(item)
        alarmScheduler.schedule(item)

        return Result.success(item)
    }
}
