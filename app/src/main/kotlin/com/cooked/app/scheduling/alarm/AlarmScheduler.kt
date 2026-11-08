package com.cooked.app.scheduling.alarm

import com.cooked.app.domain.model.Item
import java.time.Instant

data class ScheduledAlarmInfo(
    val itemId: String,
    val itemTitle: String,
    val triggerTime: Instant,
    val type: AlarmType,
    val isExact: Boolean
)

interface AlarmScheduler {
    suspend fun schedule(item: Item, nagCount: Int? = null)
    suspend fun schedule(itemId: String)
    fun cancel(itemId: String)
    suspend fun rescheduleAll()
    fun getScheduledAlarms(): List<ScheduledAlarmInfo>
    suspend fun fireNow(itemId: String)
}
