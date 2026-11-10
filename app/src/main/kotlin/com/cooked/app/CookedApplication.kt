package com.cooked.app

import android.app.Application
import com.cooked.app.data.billing.RevenueCatSetup
import com.cooked.app.domain.billing.EntitlementRepository
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

    @Inject
    lateinit var entitlementRepository: dagger.Lazy<EntitlementRepository>

    @Inject
    lateinit var adManager: dagger.Lazy<com.cooked.app.core.ads.CookedAdManager>

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            android.os.StrictMode.setThreadPolicy(
                android.os.StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build()
            )
            android.os.StrictMode.setVmPolicy(
                android.os.StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build()
            )
        }

        // Configure billing before anything reads entitlements, then sync Pro status with the
        // store so renewals, expiries and refunds are reflected without opening the paywall.
        if (RevenueCatSetup.ensureConfigured(this)) {
            applicationScope.launch {
                runCatching { entitlementRepository.get().refresh() }
            }
        }

        NotificationChannels.createChannels(this)
        SafetyNetWorker.enqueueDaily(this)
        WeeklySnapshotWorker.schedule(this)
        WeeklyReportWorker.schedule(this)

        // Initialize Google Mobile Ads SDK asynchronously in background without blocking app startup
        applicationScope.launch(Dispatchers.IO) {
            runCatching {
                adManager.get().initialize()
            }.onFailure { t ->
                android.util.Log.e("CookedApplication", "Ad initialization deferred", t)
            }
        }

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
