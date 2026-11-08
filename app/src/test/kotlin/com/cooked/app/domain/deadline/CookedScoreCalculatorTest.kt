package com.cooked.app.domain.deadline

import com.cooked.app.core.designsystem.HeatLevel
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemKind
import com.cooked.app.domain.model.ItemStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant

class CookedScoreCalculatorTest {

    private val baseNow = Instant.parse("2026-11-08T12:00:00Z")

    private fun deadline(
        id: String,
        title: String,
        dueAt: Instant,
        effortMinutes: Int = 120,
        progress: Int = 0,
        status: ItemStatus = ItemStatus.ACTIVE
    ): Item {
        return Item(
            id = id,
            title = title,
            kind = ItemKind.DEADLINE,
            dueAt = dueAt,
            effortMinutes = effortMinutes,
            progress = progress,
            status = status,
            timeZoneId = "UTC"
        )
    }

    @Test
    fun `empty active deadlines results in zero score and Chillin label`() {
        val result = CookedScoreCalculator.calculate(
            items = emptyList(),
            now = baseNow,
            dailyCapacityMinutes = 120
        )

        assertEquals(0, result.score)
        assertEquals("Chillin'", result.label)
        assertEquals(HeatLevel.CHILL, result.heatLevel)
        assertEquals(0, result.activeDeadlinesCount)
        assertNull(result.workloadWarning)
    }

    @Test
    fun `single overdue deadline increases score and identifies top contributor`() {
        val overdue = deadline(
            id = "d1",
            title = "Pay Tuition",
            dueAt = baseNow.minus(Duration.ofHours(2)),
            effortMinutes = 60
        )

        val result = CookedScoreCalculator.calculate(
            items = listOf(overdue),
            now = baseNow,
            dailyCapacityMinutes = 120 // 7 * 120 = 840 capacity
        )

        // load = (60 / 840) + 0.15 = 0.071 + 0.15 = 0.221 -> score ~22
        assertTrue(result.score > 15)
        assertEquals(1, result.overdueCount)
        assertEquals("d1", result.topContributors.first().item.id)
    }

    @Test
    fun `overloaded week generates workload warning and high cooked score`() {
        // 4 big deadlines in next 4 days: 4 * 300 = 1200 mins effort
        // Capacity = 7 * 120 = 840 mins
        // load = 1200 / 840 = 1.428 -> score = 100
        val items = (1..4).map { idx ->
            deadline(
                id = "d-$idx",
                title = "Heavy Assignment $idx",
                dueAt = baseNow.plus(Duration.ofDays(idx.toLong())),
                effortMinutes = 300
            )
        }

        val result = CookedScoreCalculator.calculate(
            items = items,
            now = baseNow,
            dailyCapacityMinutes = 120
        )

        assertEquals(100, result.score)
        assertEquals("Cooked", result.label)
        assertEquals(HeatLevel.COOKED, result.heatLevel)
        assertNotNull(result.workloadWarning)
        assertTrue(result.workloadWarning?.contains("Something's gotta give") == true)
    }

    @Test
    fun `suggested action simulates 1 day of progress on biggest contributor`() {
        val bigTask = deadline(
            id = "big",
            title = "Build Compiler",
            dueAt = baseNow.plus(Duration.ofDays(3)),
            effortMinutes = 480
        )
        val smallTask = deadline(
            id = "small",
            title = "Submit Quiz",
            dueAt = baseNow.plus(Duration.ofDays(2)),
            effortMinutes = 60
        )

        val result = CookedScoreCalculator.calculate(
            items = listOf(bigTask, smallTask),
            now = baseNow,
            dailyCapacityMinutes = 120
        )

        assertNotNull(result.suggestedAction)
        assertTrue(result.suggestedAction?.contains("Build Compiler") == true)
        assertTrue(result.simulatedScoreAfterAction != null && result.simulatedScoreAfterAction!! < result.score)
    }
}
