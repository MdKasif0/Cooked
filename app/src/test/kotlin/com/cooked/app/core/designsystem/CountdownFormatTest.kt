package com.cooked.app.core.designsystem

import com.cooked.app.core.designsystem.components.formatCountdown
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CountdownFormatTest {

    @Test
    fun formatCountdown_futureDaysAndHours_formatsCorrectly() {
        val now = 1_000_000_000L
        val target = now + (2 * 86_400_000L) + (4 * 3_600_000L) // 2 days 4 hours
        val result = formatCountdown(target, now)
        assertThat(result).isEqualTo("2d 4h")
    }

    @Test
    fun formatCountdown_futureHoursAndMinutes_formatsCorrectly() {
        val now = 1_000_000_000L
        val target = now + (3 * 3_600_000L) + (25 * 60_000L) // 3 hours 25 minutes
        val result = formatCountdown(target, now)
        assertThat(result).isEqualTo("3h 25m")
    }

    @Test
    fun formatCountdown_futureMinutesOnly_formatsCorrectly() {
        val now = 1_000_000_000L
        val target = now + (38 * 60_000L) // 38 minutes
        val result = formatCountdown(target, now)
        assertThat(result).isEqualTo("38m")
    }

    @Test
    fun formatCountdown_futureUnderOneMinute_formatsCorrectly() {
        val now = 1_000_000_000L
        val target = now + 40_000L // 40 seconds
        val result = formatCountdown(target, now)
        assertThat(result).isEqualTo("< 1m")
    }

    @Test
    fun formatCountdown_pastDue_formatsWithOverduePrefix() {
        val now = 1_000_000_000L
        val target = now - (12 * 60_000L) // overdue 12 minutes
        val result = formatCountdown(target, now)
        assertThat(result).isEqualTo("overdue 12m")
    }
}
