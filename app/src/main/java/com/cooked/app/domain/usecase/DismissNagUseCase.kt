package com.cooked.app.domain.usecase

import com.cooked.app.alarm.AlarmScheduler
import com.cooked.app.domain.repository.ReminderRepository

class DismissNagUseCase(
    private val repository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(reminderId: String) {
        val reminder = repository.getReminderById(reminderId) ?: return
        if (reminder.isCompleted) return

        // Increment nag count and ensure nagging is active
        repository.setNaggingActive(reminderId, isNagging = true, incrementNagCount = true)
        val updated = repository.getReminderById(reminderId)
        if (updated != null) {
            alarmScheduler.schedule(updated)
        }
    }
}
