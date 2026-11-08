package com.cooked.app.scheduling.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cooked.app.domain.repository.ReminderRepository
import com.cooked.app.scheduling.alarm.AlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: ReminderRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val activeReminders = repository.getActiveReminders().first()
                        for (reminder in activeReminders) {
                            alarmScheduler.schedule(reminder)
                        }
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }
}

