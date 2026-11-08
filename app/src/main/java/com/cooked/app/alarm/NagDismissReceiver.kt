package com.cooked.app.alarm

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
                // User swiped notification away! We re-arm the nag alarm so they aren't cooked!
                dismissNagUseCase(reminderId)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
