package com.cooked.app.scheduling.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.cooked.app.MainActivity
import com.cooked.app.R
import com.cooked.app.core.config.FeatureFlags
import com.cooked.app.feature.snitch.SnitchActivity
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.Personality
import com.cooked.app.scheduling.alarm.AlarmActivity
import com.cooked.app.scheduling.receivers.NagDismissReceiver
import com.cooked.app.scheduling.receivers.ReminderActionReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderNotificationBuilder @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    fun buildReminderNotification(
        item: Item,
        nagCount: Int,
        personality: Personality,
        isHinglish: Boolean = false
    ): Notification {
        val stage = when {
            nagCount == 0 -> NagStage.FIRST
            nagCount in 1..2 -> NagStage.NAG_EARLY
            nagCount in 3..4 -> NagStage.NAG_PERSISTENT
            else -> NagStage.FINAL_CALL
        }

        val effectivePersonality = item.nag.personalityOverride ?: personality
        val channelId = selectChannelId(item, nagCount)
        val copyText = NotificationCopy.getCopy(
            personality = effectivePersonality,
            stage = stage,
            title = item.title,
            nagCount = nagCount,
            isHinglish = isHinglish
        )

        val notificationId = getNotificationId(item.id)
        val displayTitle = item.title

        // Tap to open app
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ITEM_ID, item.id)
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Done Action
        val doneIntent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ReminderActionReceiver.ACTION_DONE
            putExtra(EXTRA_ITEM_ID, item.id)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 1,
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze 10m Action
        val snooze10Intent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ReminderActionReceiver.ACTION_SNOOZE_10M
            putExtra(EXTRA_ITEM_ID, item.id)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val snooze10PendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 2,
            snooze10Intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze 1h Action
        val snooze1hIntent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ReminderActionReceiver.ACTION_SNOOZE_1H
            putExtra(EXTRA_ITEM_ID, item.id)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val snooze1hPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 3,
            snooze1hIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze Tomorrow 9am Action
        val snoozeTomorrowIntent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ReminderActionReceiver.ACTION_SNOOZE_TOMORROW
            putExtra(EXTRA_ITEM_ID, item.id)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val snoozeTomorrowPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 4,
            snoozeTomorrowIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Swipe Dismiss DeleteIntent (logs dismissal without canceling the nag schedule)
        val deleteIntent = Intent(context, NagDismissReceiver::class.java).apply {
            putExtra(EXTRA_ITEM_ID, item.id)
        }
        val deletePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 5,
            deleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val doneAction = NotificationCompat.Action.Builder(0, "Done", donePendingIntent).build()
        val snooze10Action = NotificationCompat.Action.Builder(0, "+10m", snooze10PendingIntent).build()
        val snoozeTomorrowAction = NotificationCompat.Action.Builder(0, "Tomorrow", snoozeTomorrowPendingIntent).build()

        val wearableExtender = NotificationCompat.WearableExtender()
            .addAction(doneAction)
            .addAction(snooze10Action)
            .addAction(snoozeTomorrowAction)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(displayTitle)
            .setContentText(copyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                if (!item.note.isNullOrBlank()) "$copyText\n\nNote: ${item.note}" else copyText
            ))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(false)
            .setGroup(GROUP_COOKED_REMINDERS)
            .setContentIntent(openPendingIntent)
            .setDeleteIntent(deletePendingIntent)
            .addAction(doneAction)
            .addAction(snooze10Action)
            .addAction(snoozeTomorrowAction)
            .extend(wearableExtender)

        if (FeatureFlags.isSnitchModeEnabled && nagCount >= 3) {
            val snitchIntent = Intent(context, SnitchActivity::class.java).apply {
                putExtra(SnitchActivity.EXTRA_ITEM_ID, item.id)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val snitchPendingIntent = PendingIntent.getActivity(
                context,
                notificationId + 7,
                snitchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val snitchAction = NotificationCompat.Action.Builder(0, "Time to snitch?", snitchPendingIntent).build()
            builder.addAction(snitchAction)
        }

        // Custom Vector Large Icon
        try {
            val iconRes = com.cooked.app.core.designsystem.components.CategoryIcons.resolve(item.emoji)
            val drawable = androidx.core.content.ContextCompat.getDrawable(context, iconRes)
            if (drawable != null) {
                val bitmap = android.graphics.Bitmap.createBitmap(192, 192, android.graphics.Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bitmap)
                drawable.setBounds(16, 16, 176, 176)
                drawable.draw(canvas)
                builder.setLargeIcon(bitmap)
            }
        } catch (_: Exception) {}

        // Deadline countdown / heat line
        if (item.kind == com.cooked.app.domain.model.ItemKind.DEADLINE) {
            val now = java.time.Instant.now()
            val subText = if (now.isAfter(item.dueAt)) {
                "Overdue · Cooked"
            } else {
                val duration = java.time.Duration.between(now, item.dueAt)
                val hours = duration.toHours()
                val mins = duration.toMinutes() % 60
                val timeStr = if (hours >= 24) "${duration.toDays()}d ${hours % 24}h" else "${hours}h ${mins}m"
                "Due in $timeStr"
            }
            builder.setSubText(subText)
        }

        // Full-screen alarm intent for critical items or high nags (5+)
        if (shouldUseFullScreenIntent(item, nagCount)) {
            val fullScreenIntent = Intent(context, AlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_ITEM_ID, item.id)
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
                putExtra(EXTRA_NAG_COUNT, nagCount)
            }
            val fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                notificationId + 6,
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.setFullScreenIntent(fullScreenPendingIntent, true)
        }

        return builder.build()
    }

    fun buildSummaryNotification(count: Int): Notification {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            SUMMARY_NOTIFICATION_ID,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, NotificationChannels.CHANNEL_REMINDERS_LOUD)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("$count things are cooking 🔥")
            .setContentText("You have multiple active reminders that need attention.")
            .setGroup(GROUP_COOKED_REMINDERS)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .build()
    }

    private fun selectChannelId(item: Item, nagCount: Int): String {
        return when {
            item.critical -> NotificationChannels.CHANNEL_CRITICAL_ALARM
            !item.nag.escalate -> NotificationChannels.CHANNEL_REMINDERS_NORMAL
            nagCount < 2 -> NotificationChannels.CHANNEL_REMINDERS_NORMAL
            nagCount in 2..4 -> NotificationChannels.CHANNEL_REMINDERS_LOUD
            else -> NotificationChannels.CHANNEL_CRITICAL_ALARM
        }
    }

    private fun shouldUseFullScreenIntent(item: Item, nagCount: Int): Boolean {
        if (!item.critical && nagCount < 5) return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            return notificationManager?.canUseFullScreenIntent() == true
        }
        return true
    }

    companion object {
        const val GROUP_COOKED_REMINDERS = "com.cooked.app.NOTIFICATIONS_GROUP"
        const val SUMMARY_NOTIFICATION_ID = 999999
        const val EXTRA_ITEM_ID = "extra_item_id"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_NAG_COUNT = "extra_nag_count"

        fun getNotificationId(itemId: String): Int = (itemId.hashCode() and 0x7FFFFFFF)
    }
}
