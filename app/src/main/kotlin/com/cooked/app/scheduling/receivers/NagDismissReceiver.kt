package com.cooked.app.scheduling.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cooked.app.domain.model.ItemEventType
import com.cooked.app.domain.repository.ItemRepository
import com.cooked.app.scheduling.notification.ReminderNotificationBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NagDismissReceiver : BroadcastReceiver() {

    @Inject
    lateinit var itemRepository: ItemRepository

    override fun onReceive(context: Context, intent: Intent) {
        val itemId = intent.getStringExtra(ReminderNotificationBuilder.EXTRA_ITEM_ID) ?: return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Log swipe dismissal without cancelling the nag loop. The next nag will still fire!
                itemRepository.logEvent(
                    itemId = itemId,
                    type = ItemEventType.SNOOZED,
                    meta = mapOf("action" to "swipe_dismissed")
                )
            } finally {
                pendingResult.finish()
            }
        }
    }
}
