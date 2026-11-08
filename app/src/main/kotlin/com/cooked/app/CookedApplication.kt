package com.cooked.app

import android.app.Application
import com.cooked.app.domain.repository.ItemRepository
import com.cooked.app.scheduling.alarm.AlarmScheduler
import com.cooked.app.scheduling.notification.NotificationChannels
import com.cooked.app.scheduling.work.SafetyNetWorker
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

        // Observe ItemChanged events to keep alarms strictly synchronized with DB
        applicationScope.launch {
            itemRepository.itemChanged.collect { itemId ->
                alarmScheduler.schedule(itemId)
            }
        }
    }
}
