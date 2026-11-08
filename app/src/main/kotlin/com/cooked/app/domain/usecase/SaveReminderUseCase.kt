package com.cooked.app.domain.usecase

import com.cooked.app.domain.model.Reminder
import com.cooked.app.domain.repository.ReminderRepository
import com.cooked.app.scheduling.alarm.AlarmScheduler

class SaveReminderUseCase(
    private val repository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(reminder: Reminder) {
        repository.insertOrUpdate(reminder)
        alarmScheduler.schedule(reminder)
    }
}
