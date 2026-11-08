package com.cooked.app.domain.usecase

import com.cooked.app.domain.model.Reminder
import com.cooked.app.domain.repository.ReminderRepository
import com.cooked.app.scheduling.alarm.AlarmScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DismissNagUseCaseTest {

    private val repository: ReminderRepository = mockk(relaxed = true)
    private val alarmScheduler: AlarmScheduler = mockk(relaxed = true)
    private lateinit var useCase: DismissNagUseCase

    @Before
    fun setUp() {
        useCase = DismissNagUseCase(repository, alarmScheduler)
    }

    @Test
    fun `dismissing notification re-arms nag alarm when not completed`() = runTest {
        val reminder = Reminder(
            id = "test-id-123",
            title = "Drink Water",
            isCompleted = false,
            createdAt = 1000L,
            updatedAt = 1000L
        )

        coEvery { repository.getReminderById("test-id-123") } returns reminder

        useCase("test-id-123")

        coVerify { repository.setNaggingActive("test-id-123", isNagging = true, incrementNagCount = true) }
        coVerify { alarmScheduler.schedule("test-id-123") }
    }

    @Test
    fun `dismissing completed reminder does not re-arm nag alarm`() = runTest {
        val completedReminder = Reminder(
            id = "test-completed-123",
            title = "Already Done",
            isCompleted = true,
            createdAt = 1000L,
            updatedAt = 1000L
        )

        coEvery { repository.getReminderById("test-completed-123") } returns completedReminder

        useCase("test-completed-123")

        coVerify(exactly = 0) { repository.setNaggingActive(any(), any(), any()) }
        coVerify(exactly = 0) { alarmScheduler.schedule(any<String>()) }
    }
}
