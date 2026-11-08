package com.cooked.app.domain.usecase

import com.cooked.app.alarm.AlarmScheduler
import com.cooked.app.core.time.TimeUtils
import com.cooked.app.domain.repository.ReminderRepository

class SnoozeReminderUseCase(
    private val repository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(reminderId: String, snoozeMinutes: Int = 10) {
        val snoozeInstant = TimeUtils.nowUtcMillis() + (snoozeMinutes * 60 * 1000L)
        repository.snoozeReminder(reminderId, snoozeInstant)
        val updated = repository.getReminderById(reminderId)
        if (updated != null) {
            alarmScheduler.schedule(updated)
        }
    }
}
