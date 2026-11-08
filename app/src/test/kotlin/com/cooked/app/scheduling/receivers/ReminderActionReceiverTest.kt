package com.cooked.app.scheduling.receivers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.cooked.app.domain.repository.ItemRepository
import com.cooked.app.scheduling.alarm.AlarmScheduler
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ReminderActionReceiverTest {

    private lateinit var context: Context
    private lateinit var itemRepository: ItemRepository
    private lateinit var alarmScheduler: AlarmScheduler
    private lateinit var receiver: ReminderActionReceiver

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        itemRepository = mockk(relaxed = true)
        alarmScheduler = mockk(relaxed = true)

        receiver = ReminderActionReceiver().apply {
            this.itemRepository = this@ReminderActionReceiverTest.itemRepository
            this.alarmScheduler = this@ReminderActionReceiverTest.alarmScheduler
        }
    }

    @Test
    fun `ACTION_DONE marks item complete and cancels alarm`() = runTest {
        val itemId = "test-item-done"

        receiver.executeAction(
            context = context,
            action = ReminderActionReceiver.ACTION_DONE,
            itemId = itemId,
            notificationId = 123
        )

        coVerify { itemRepository.markDone(itemId, any()) }
        verify { alarmScheduler.cancel(itemId) }
    }

    @Test
    fun `ACTION_SNOOZE_10M snoozes item for 10 minutes and reschedules`() = runTest {
        val itemId = "test-item-snooze-10m"

        receiver.executeAction(
            context = context,
            action = ReminderActionReceiver.ACTION_SNOOZE_10M,
            itemId = itemId,
            notificationId = 456
        )

        coVerify { itemRepository.snooze(itemId, any()) }
        coVerify { alarmScheduler.schedule(itemId) }
    }

    @Test
    fun `ACTION_SNOOZE_1H snoozes item for 1 hour and reschedules`() = runTest {
        val itemId = "test-item-snooze-1h"

        receiver.executeAction(
            context = context,
            action = ReminderActionReceiver.ACTION_SNOOZE_1H,
            itemId = itemId,
            notificationId = 789
        )

        coVerify { itemRepository.snooze(itemId, any()) }
        coVerify { alarmScheduler.schedule(itemId) }
    }

    @Test
    fun `ACTION_SNOOZE_TOMORROW snoozes item until tomorrow 9am and reschedules`() = runTest {
        val itemId = "test-item-snooze-tomorrow"

        receiver.executeAction(
            context = context,
            action = ReminderActionReceiver.ACTION_SNOOZE_TOMORROW,
            itemId = itemId,
            notificationId = 999
        )

        coVerify { itemRepository.snooze(itemId, any()) }
        coVerify { alarmScheduler.schedule(itemId) }
    }
}
