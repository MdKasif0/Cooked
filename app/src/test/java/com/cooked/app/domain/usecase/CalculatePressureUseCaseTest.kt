package com.cooked.app.domain.usecase

import com.cooked.app.domain.model.PressureLevel
import com.cooked.app.domain.model.Reminder
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class CalculatePressureUseCaseTest {

    private lateinit var useCase: CalculatePressureUseCase

    @Before
    fun setUp() {
        useCase = CalculatePressureUseCase()
    }

    @Test
    fun `when overdue, pressure is CRISPY`() {
        val now = 100_000_000L
        val reminder = Reminder(
            title = "Submit Assignment",
            dueInstant = now - 5000L,
            isDeadlineMode = true,
            estimatedEffortMinutes = 60,
            createdAt = now - 200_000L,
            updatedAt = now - 200_000L
        )

        val result = useCase(reminder, nowUtcMillis = now)
        assertThat(result).isEqualTo(PressureLevel.CRISPY)
    }

    @Test
    fun `when remaining time is less than estimated effort, pressure is CRISPY`() {
        val now = 100_000_000L
        // 30 mins remaining, but effort is 60 mins!
        val reminder = Reminder(
            title = "Read 50 pages",
            dueInstant = now + (30 * 60 * 1000L),
            isDeadlineMode = true,
            estimatedEffortMinutes = 60,
            createdAt = now - (60 * 60 * 1000L),
            updatedAt = now - (60 * 60 * 1000L)
        )

        val result = useCase(reminder, nowUtcMillis = now)
        assertThat(result).isEqualTo(PressureLevel.CRISPY)
    }

    @Test
    fun `when plenty of buffer exists, pressure is CHILL`() {
        val now = 100_000_000L
        // 5 days remaining, effort is 1 hour
        val reminder = Reminder(
            title = "Tax return",
            dueInstant = now + (5 * 24 * 60 * 60 * 1000L),
            isDeadlineMode = true,
            estimatedEffortMinutes = 60,
            deadlineStartInstant = now,
            createdAt = now,
            updatedAt = now
        )

        val result = useCase(reminder, nowUtcMillis = now)
        assertThat(result).isEqualTo(PressureLevel.CHILL)
    }
}

