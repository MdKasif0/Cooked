package com.cooked.app

import android.app.Application
import com.cooked.app.domain.repository.ItemRepository
import com.cooked.app.scheduling.alarm.AlarmScheduler
import com.cooked.app.scheduling.notification.NotificationChannels
import com.cooked.app.scheduling.work.SafetyNetWorker
import com.cooked.app.scheduling.work.WeeklyReportWorker
import com.cooked.app.scheduling.work.WeeklySnapshotWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class CookedApplication : Application() {

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var itemRepository: ItemRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.createChannels(this)
        SafetyNetWorker.enqueueDaily(this)
        WeeklySnapshotWorker.schedule(this)
        WeeklyReportWorker.schedule(this)

        // Observe ItemChanged events to keep alarms, widgets, and dynamic shortcuts strictly synchronized with DB
        applicationScope.launch {
            itemRepository.itemChanged.collect { itemId ->
                alarmScheduler.schedule(itemId)
                com.cooked.app.widget.WidgetUpdater.refreshAll(this@CookedApplication)

                val activeDeadlines = itemRepository.getActiveItems()
                    .filter { it.kind == com.cooked.app.domain.model.ItemKind.DEADLINE }
                    .sortedBy { it.dueAt }
                com.cooked.app.core.util.ShortcutHelper.updateDynamicShortcuts(
                    this@CookedApplication,
                    activeDeadlines
                )
            }
        }
    }
}
