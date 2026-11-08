package com.cooked.app.domain.usecase

import com.cooked.app.alarm.AlarmScheduler
import com.cooked.app.domain.repository.ReminderRepository

class DeleteReminderUseCase(
    private val repository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(reminderId: String) {
        alarmScheduler.cancel(reminderId)
        repository.deleteReminder(reminderId)
    }
}

