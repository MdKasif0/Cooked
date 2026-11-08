package com.cooked.app.domain.usecase

import com.cooked.app.domain.model.CookedLevel
import com.cooked.app.domain.model.Reminder
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class CalculateCookedScoreUseCaseTest {

    private lateinit var useCase: CalculateCookedScoreUseCase

    @Before
    fun setUp() {
        useCase = CalculateCookedScoreUseCase()
    }

    @Test
    fun `empty reminders returns zero score and CHILLING level`() {
        val result = useCase(emptyList())
        assertThat(result.score).isEqualTo(0)
        assertThat(result.level).isEqualTo(CookedLevel.CHILLING)
    }

    @Test
    fun `heavy overload results in high Cooked Score and DEEP_FRIED level`() {
        val now = 100_000_000L
        // 5 huge deadlines totaling 35 hours within 7 days against 28h capacity
        val deadlines = (1..5).map { i ->
            Reminder(
                title = "Massive project $i",
                dueInstant = now + (2 * 24 * 60 * 60 * 1000L),
                isDeadlineMode = true,
                estimatedEffortMinutes = 420, // 7 hours each = 35 hours
                createdAt = now,
                updatedAt = now
            )
        }

        val result = useCase(deadlines, nowUtcMillis = now, weeklyProductiveCapacityHours = 28.0)
        assertThat(result.score).isAtLeast(80)
        assertThat(result.level).isEqualTo(CookedLevel.DEEP_FRIED)
    }

    @Test
    fun `overdue item adds urgency penalty`() {
        val now = 100_000_000L
        val overdueReminder = Reminder(
            title = "Overdue UPSC test",
            dueInstant = now - 10000L,
            isDeadlineMode = true,
            estimatedEffortMinutes = 120,
            createdAt = now - 500000L,
            updatedAt = now - 500000L
        )

        val result = useCase(listOf(overdueReminder), nowUtcMillis = now)
        // Check penalty applied
        assertThat(result.score).isGreaterThan(10)
    }
}

