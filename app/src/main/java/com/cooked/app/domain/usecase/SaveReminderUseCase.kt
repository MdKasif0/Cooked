package com.cooked.app.domain.usecase

import com.cooked.app.alarm.AlarmScheduler
import com.cooked.app.domain.model.Reminder
import com.cooked.app.domain.repository.ReminderRepository

class SaveReminderUseCase(
    private val repository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(reminder: Reminder) {
        repository.insertOrUpdate(reminder)
        alarmScheduler.schedule(reminder)
    }
}
