package com.cooked.app.scheduling.alarm

import com.cooked.app.domain.model.Reminder

interface AlarmScheduler {
    fun schedule(reminder: Reminder)
    fun cancel(reminderId: String)
}
