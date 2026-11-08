package com.cooked.app.core.util

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.cooked.app.MainActivity
import com.cooked.app.R
import com.cooked.app.domain.model.Item
import com.cooked.app.scheduling.notification.EmojiBitmapHelper

object ShortcutHelper {

    /**
     * Pushes the top 2 hottest deadlines as dynamic app launcher shortcuts.
     * Users can long-press the Cooked app icon to see them and drag them to pin on their home screen.
     */
    fun updateDynamicShortcuts(context: Context, hottestDeadlines: List<Item>) {
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context) &&
            ShortcutManagerCompat.getDynamicShortcuts(context).isEmpty() &&
            hottestDeadlines.isEmpty()
        ) {
            return
        }

        val shortcuts = hottestDeadlines.take(2).mapIndexed { index, item ->
            val iconRes = com.cooked.app.core.designsystem.components.CategoryIcons.resolve(item.emoji)
            val icon = IconCompat.createWithResource(context, iconRes)

            val intent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                putExtra("navigate_to", "deadline_detail")
                putExtra("extra_item_id", item.id)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            ShortcutInfoCompat.Builder(context, "deadline_${item.id}")
                .setShortLabel(item.title.take(16))
                .setLongLabel(item.title)
                .setIcon(icon)
                .setIntent(intent)
                .setRank(index)
                .build()
        }

        try {
            ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts)
        } catch (_: Exception) {
            // Silently ignore if launcher restricts shortcut updates
        }
    }

    /**
     * Request pinning a specific deadline to the user's home screen as a 1x1 shortcut.
     */
    fun requestPinDeadline(context: Context, item: Item) {
        if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            val iconRes = com.cooked.app.core.designsystem.components.CategoryIcons.resolve(item.emoji)
            val icon = IconCompat.createWithResource(context, iconRes)

            val intent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                putExtra("navigate_to", "deadline_detail")
                putExtra("extra_item_id", item.id)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val pinShortcutInfo = ShortcutInfoCompat.Builder(context, "pinned_deadline_${item.id}")
                .setShortLabel(item.title.take(16))
                .setLongLabel(item.title)
                .setIcon(icon)
                .setIntent(intent)
                .build()

            ShortcutManagerCompat.requestPinShortcut(context, pinShortcutInfo, null)
        }
    }
}

