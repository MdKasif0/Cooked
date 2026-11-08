package com.cooked.app.domain.usecase

import com.cooked.app.alarm.AlarmScheduler
import com.cooked.app.core.time.TimeUtils
import com.cooked.app.domain.repository.ReminderRepository

class CompleteReminderUseCase(
    private val repository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(reminderId: String) {
        val now = TimeUtils.nowUtcMillis()
        repository.completeReminder(reminderId, now)
        alarmScheduler.cancel(reminderId)
    }
}

