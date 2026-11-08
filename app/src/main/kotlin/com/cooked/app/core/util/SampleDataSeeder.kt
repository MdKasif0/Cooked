package com.cooked.app.core.util

import com.cooked.app.data.dao.ChecklistStepDao
import com.cooked.app.data.dao.ItemDao
import com.cooked.app.data.dao.ItemEventDao
import com.cooked.app.data.db.CookedDatabase
import com.cooked.app.data.entity.ChecklistStepEntity
import com.cooked.app.data.entity.ItemEntity
import com.cooked.app.data.entity.ItemEventEntity
import com.cooked.app.domain.model.ChecklistStep
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemEventType
import com.cooked.app.domain.model.ItemKind
import com.cooked.app.domain.model.ItemStatus
import com.cooked.app.domain.model.NagPolicy
import com.cooked.app.domain.model.Recurrence
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SampleDataSeeder @Inject constructor(
    private val database: CookedDatabase,
    private val itemDao: ItemDao,
    private val checklistStepDao: ChecklistStepDao,
    private val itemEventDao: ItemEventDao
) {
    suspend fun seedSampleData(now: Instant = Instant.now()): Int {
        val items = mutableListOf<Item>()
        val allEvents = mutableListOf<ItemEventEntity>()
        val allSteps = mutableListOf<ChecklistStepEntity>()

        // 1. Past 60 Days: Completed items (18 items, on-time and late, with nags & snoozes)
        val pastTitles = listOf(
            Triple("Submit Operating Systems Assignment 1", ItemKind.DEADLINE, "assignment"),
            Triple("Pay Semester Fee", ItemKind.REMINDER, "bill"),
            Triple("Algorithms Problem Set 2", ItemKind.DEADLINE, "code"),
            Triple("Renew Domain cooked.app", ItemKind.REMINDER, "renewal"),
            Triple("Review Pull Request #412", ItemKind.REMINDER, "code"),
            Triple("File GST Quarterly Return", ItemKind.DEADLINE, "assignment"),
            Triple("Calculus Midterm Prep", ItemKind.DEADLINE, "exam"),
            Triple("Send Client Invoice #1042", ItemKind.REMINDER, "bill"),
            Triple("Dentist Cleaning Checkup", ItemKind.REMINDER, "health"),
            Triple("Renew Two-Wheeler Insurance", ItemKind.REMINDER, "vehicle"),
            Triple("Submit Machine Learning Project Milestone", ItemKind.DEADLINE, "code"),
            Triple("Call Mom & Wish Birthday", ItemKind.REMINDER, "call"),
            Triple("Backup SSD to Cloud", ItemKind.REMINDER, "renewal"),
            Triple("Physics Thermodynamics Lab", ItemKind.DEADLINE, "lightning"),
            Triple("Order College Textbooks", ItemKind.REMINDER, "cart"),
            Triple("Freelance Dashboard Frontend", ItemKind.DEADLINE, "creative"),
            Triple("Return Amazon Return Package", ItemKind.REMINDER, "cart"),
            Triple("Electricity Bill Payment", ItemKind.REMINDER, "bill")
        )

        pastTitles.forEachIndexed { index, (title, kind, emoji) ->
            val daysAgo = 58 - (index * 3) // spread over past 60 days
            val created = now.minus(daysAgo.toLong() + 5, ChronoUnit.DAYS)
            val due = now.minus(daysAgo.toLong(), ChronoUnit.DAYS)
            val isLate = index % 3 == 0 // 1 in 3 was late
            val completed = if (isLate) due.plus(4, ChronoUnit.HOURS) else due.minus(2, ChronoUnit.HOURS)

            val itemId = UUID.randomUUID().toString()
            val item = Item(
                id = itemId,
                kind = kind,
                title = title,
                emoji = emoji,
                dueAt = due,
                status = ItemStatus.DONE,
                completedAt = completed,
                createdAt = created,
                updatedAt = completed,
                effortMinutes = if (kind == ItemKind.DEADLINE) 120 else 20,
                startBy = if (kind == ItemKind.DEADLINE) due.minus(3, ChronoUnit.DAYS) else null,
                progress = 100
            )
            items.add(item)

            // Events for this past item
            allEvents.add(ItemEventEntity(UUID.randomUUID().toString(), itemId, ItemEventType.CREATED, created, emptyMap()))
            if (isLate) {
                // Nag event
                val nagTime = due.plus(1, ChronoUnit.HOURS)
                allEvents.add(ItemEventEntity(UUID.randomUUID().toString(), itemId, ItemEventType.NAGGED, nagTime, mapOf("nagIndex" to "1")))
                val snoozeTime = due.plus(2, ChronoUnit.HOURS)
                allEvents.add(ItemEventEntity(UUID.randomUUID().toString(), itemId, ItemEventType.SNOOZED, snoozeTime, mapOf("snoozeMinutes" to "60")))
            }
            allEvents.add(ItemEventEntity(
                UUID.randomUUID().toString(),
                itemId,
                ItemEventType.DONE,
                completed,
                mapOf("lateByMinutes" to if (isLate) "240" else "0")
            ))
        }

        // 2. Today: 4 items (Active, due today)
        val todayItems = listOf(
            Triple("Submit Distributed Systems Report", ItemKind.DEADLINE, "assignment"),
            Triple("Take Evening Multivitamin", ItemKind.REMINDER, "health"),
            Triple("Reply to Client Scope Email", ItemKind.REMINDER, "work"),
            Triple("LeetCode Daily Challenge", ItemKind.REMINDER, "code")
        )

        todayItems.forEachIndexed { index, (title, kind, emoji) ->
            val due = now.plus((index * 2 + 1).toLong(), ChronoUnit.HOURS)
            val created = now.minus(1, ChronoUnit.DAYS)
            val itemId = UUID.randomUUID().toString()
            val steps = if (kind == ItemKind.DEADLINE) {
                listOf(
                    ChecklistStep(UUID.randomUUID().toString(), itemId, "Draft architecture diagram", true, 0, created),
                    ChecklistStep(UUID.randomUUID().toString(), itemId, "Benchmark latency comparison", false, 1, created),
                    ChecklistStep(UUID.randomUUID().toString(), itemId, "Export final PDF", false, 2, created)
                )
            } else {
                emptyList()
            }
            steps.forEach { allSteps.add(ChecklistStepEntity.fromDomain(it)) }

            val item = Item(
                id = itemId,
                kind = kind,
                title = title,
                emoji = emoji,
                dueAt = due,
                status = ItemStatus.ACTIVE,
                createdAt = created,
                updatedAt = created,
                effortMinutes = if (kind == ItemKind.DEADLINE) 180 else 15,
                progress = if (kind == ItemKind.DEADLINE) 33 else 0,
                checklist = steps
            )
            items.add(item)
            allEvents.add(ItemEventEntity(UUID.randomUUID().toString(), itemId, ItemEventType.CREATED, created, emptyMap()))
        }

        // 3. Next 30 Days: 8 upcoming items & deadlines
        val upcomingItems = listOf(
            Triple("GATE Mock Examination 5", ItemKind.DEADLINE, "target"),
            Triple("Renew Spotify Subscription", ItemKind.REMINDER, "renewal"),
            Triple("Compiler Design Lab Final", ItemKind.DEADLINE, "code"),
            Triple("Car Service & Wheel Alignment", ItemKind.REMINDER, "vehicle"),
            Triple("Quarterly Freelance Project Showcase", ItemKind.DEADLINE, "work"),
            Triple("Buy Mom Mother's Day Gift", ItemKind.REMINDER, "cart"),
            Triple("Database Internals Presentation", ItemKind.DEADLINE, "work"),
            Triple("Book Train Tickets to Hometown", ItemKind.REMINDER, "travel")
        )

        upcomingItems.forEachIndexed { index, (title, kind, emoji) ->
            val due = now.plus(((index + 1) * 3 + 2).toLong(), ChronoUnit.DAYS)
            val created = now.minus(2, ChronoUnit.DAYS)
            val itemId = UUID.randomUUID().toString()
            val steps = if (kind == ItemKind.DEADLINE) {
                listOf(
                    ChecklistStep(UUID.randomUUID().toString(), itemId, "Review lecture notes", false, 0, created),
                    ChecklistStep(UUID.randomUUID().toString(), itemId, "Solve 5 practice problems", false, 1, created)
                )
            } else {
                emptyList()
            }
            steps.forEach { allSteps.add(ChecklistStepEntity.fromDomain(it)) }

            val item = Item(
                id = itemId,
                kind = kind,
                title = title,
                emoji = emoji,
                dueAt = due,
                status = ItemStatus.ACTIVE,
                createdAt = created,
                updatedAt = created,
                effortMinutes = if (kind == ItemKind.DEADLINE) 240 else 30,
                startBy = if (kind == ItemKind.DEADLINE) due.minus(5, ChronoUnit.DAYS) else null,
                checklist = steps
            )
            items.add(item)
            allEvents.add(ItemEventEntity(UUID.randomUUID().toString(), itemId, ItemEventType.CREATED, created, emptyMap()))
        }

        // Insert everything in a batch
        itemDao.upsertItems(items.map { ItemEntity.fromDomain(it) })
        if (allSteps.isNotEmpty()) {
            checklistStepDao.upsertSteps(allSteps)
        }
        if (allEvents.isNotEmpty()) {
            itemEventDao.insertEvents(allEvents)
        }

        return items.size
    }
}

