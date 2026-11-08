package com.cooked.app.scheduling.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cooked.app.domain.usecase.DismissNagUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NagDismissReceiver : BroadcastReceiver() {

    @Inject
    lateinit var dismissNagUseCase: DismissNagUseCase

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra(ReminderAlarmReceiver.EXTRA_REMINDER_ID) ?: return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                dismissNagUseCase(reminderId)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
