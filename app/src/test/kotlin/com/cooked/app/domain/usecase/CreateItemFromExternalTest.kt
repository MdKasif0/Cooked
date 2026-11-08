package com.cooked.app.domain.usecase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.cooked.app.core.time.CookedClock
import com.cooked.app.data.datastore.UserPreferencesRepository
import com.cooked.app.data.db.CookedDatabase
import com.cooked.app.data.repository.ItemRepositoryImpl
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemKind
import com.cooked.app.scheduling.alarm.AlarmScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant
import java.time.ZoneId

private class ExternalMockClock(
    var currentInstant: Instant = Instant.parse("2026-11-08T10:00:00Z"),
    var currentZone: ZoneId = ZoneId.of("UTC")
) : CookedClock {
    override fun now(): Instant = currentInstant
    override fun zone(): ZoneId = currentZone
}

private class MockAlarmScheduler : AlarmScheduler {
    val scheduledItemIds = mutableListOf<String>()
    val canceledItemIds = mutableListOf<String>()

    override suspend fun schedule(item: Item, nagCount: Int?) {
        scheduledItemIds.add(item.id)
    }

    override suspend fun schedule(itemId: String) {
        scheduledItemIds.add(itemId)
    }

    override fun cancel(itemId: String) {
        canceledItemIds.add(itemId)
    }

    override suspend fun rescheduleAll() {}

    override fun getScheduledAlarms(): List<com.cooked.app.scheduling.alarm.ScheduledAlarmInfo> = emptyList()

    override suspend fun fireNow(itemId: String) {}
}

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CreateItemFromExternalTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var database: CookedDatabase
    private lateinit var repository: ItemRepositoryImpl
    private lateinit var alarmScheduler: MockAlarmScheduler
    private lateinit var userPrefsRepo: UserPreferencesRepository
    private lateinit var clock: ExternalMockClock
    private lateinit var useCase: CreateItemFromExternal

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, CookedDatabase::class.java)
            .allowMainThreadQueries()
            .setQueryExecutor(testDispatcher.asExecutor())
            .setTransactionExecutor(testDispatcher.asExecutor())
            .build()
        clock = ExternalMockClock()
        repository = ItemRepositoryImpl(
            database = database,
            itemDao = database.itemDao(),
            checklistStepDao = database.checklistStepDao(),
            itemEventDao = database.itemEventDao(),
            clock = clock
        )
        alarmScheduler = MockAlarmScheduler()
        userPrefsRepo = UserPreferencesRepository(context)

        useCase = CreateItemFromExternal(
            itemRepository = repository,
            alarmScheduler = alarmScheduler,
            userPreferencesRepository = userPrefsRepo,
            clock = clock
        )
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun rawQueryCreatesAndSchedulesItem() = runTest(testDispatcher) {
        val result = useCase("call mom tonight")
        advanceUntilIdle()

        assertTrue(result.isSuccess)
        val item = result.getOrNull()
        assertNotNull(item)
        assertEquals("call mom", item?.title?.trim())
        assertEquals(ItemKind.REMINDER, item?.kind)
        assertTrue(alarmScheduler.scheduledItemIds.contains(item?.id))
    }

    @Test
    fun deadlineKeywordsCreateDeadlineWithStartBy() = runTest(testDispatcher) {
        val result = useCase("submit chemistry lab report due fri 5pm 2h")
        advanceUntilIdle()

        assertTrue(result.isSuccess)
        val item = result.getOrNull()
        assertNotNull(item)
        assertEquals(ItemKind.DEADLINE, item?.kind)
        assertEquals(120, item?.effortMinutes)
        assertNotNull(item?.startBy)
        assertTrue(alarmScheduler.scheduledItemIds.contains(item?.id))
    }

    @Test
    fun emptyQueryFailsGracefully() = runTest(testDispatcher) {
        val result = useCase("   ")
        advanceUntilIdle()

        assertTrue(result.isFailure)
        assertTrue(alarmScheduler.scheduledItemIds.isEmpty())
    }
}
