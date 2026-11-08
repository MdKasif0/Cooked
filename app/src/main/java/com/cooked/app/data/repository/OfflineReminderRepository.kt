package com.cooked.app.data.repository

import com.cooked.app.core.time.TimeUtils
import com.cooked.app.data.local.dao.ReminderDao
import com.cooked.app.data.local.entity.ReminderEntity
import com.cooked.app.domain.model.Reminder
import com.cooked.app.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineReminderRepository @Inject constructor(
    private val reminderDao: ReminderDao
) : ReminderRepository {

    override fun getAllReminders(): Flow<List<Reminder>> {
        return reminderDao.getAllReminders().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getActiveReminders(): Flow<List<Reminder>> {
        return reminderDao.getActiveReminders().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getCompletedReminders(): Flow<List<Reminder>> {
        return reminderDao.getCompletedReminders().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getActiveDeadlines(): Flow<List<Reminder>> {
        return reminderDao.getActiveDeadlines().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getReminderById(id: String): Reminder? {
        return reminderDao.getReminderById(id)?.toDomain()
    }

    override suspend fun insertOrUpdate(reminder: Reminder) {
        val entity = ReminderEntity.fromDomain(reminder)
        reminderDao.insertOrUpdate(entity)
    }

    override suspend fun completeReminder(id: String, completedAtUtcMillis: Long) {
        reminderDao.markCompleted(id, completedAtUtcMillis)
    }

    override suspend fun snoozeReminder(id: String, snoozeUntilUtcMillis: Long) {
        val now = TimeUtils.nowUtcMillis()
        reminderDao.setSnooze(id, snoozeUntilUtcMillis, now)
    }

    override suspend fun setNaggingActive(
        id: String,
        isNagging: Boolean,
        incrementNagCount: Boolean
    ) {
        val now = TimeUtils.nowUtcMillis()
        reminderDao.setNagging(id, isNagging, incrementNagCount, now)
    }

    override suspend fun deleteReminder(id: String) {
        reminderDao.deleteById(id)
    }
}
