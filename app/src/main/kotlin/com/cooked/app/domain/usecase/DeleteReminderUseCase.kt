package com.cooked.app.domain.usecase

import com.cooked.app.domain.repository.ReminderRepository
import com.cooked.app.scheduling.alarm.AlarmScheduler

class DeleteReminderUseCase(
    private val repository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(reminderId: String) {
        alarmScheduler.cancel(reminderId)
        repository.deleteReminder(reminderId)
    }
}
