package com.cooked.app.ui.navigation

import android.content.Intent
import android.net.Uri
import android.os.Build

/**
 * Where an incoming intent (notification tap, launcher shortcut, share sheet, weekly report)
 * should take the user once the app UI is ready.
 */
sealed interface DeepLink {
    data class ItemDetail(val itemId: String) : DeepLink
    data object Today : DeepLink
    data object Insights : DeepLink
    data class SharedImage(val uri: String) : DeepLink

    companion object {
        /** Intent extra carrying the target item id (notifications, alarms, shortcuts). */
        const val EXTRA_ITEM_ID = "extra_item_id"

        /** Optional intent extra naming the destination (shortcuts, weekly report). */
        const val EXTRA_NAVIGATE_TO = "navigate_to"

        const val TARGET_DEADLINE_DETAIL = "deadline_detail"
        const val TARGET_INSIGHTS_REPORT = "insights_report"
        const val TARGET_TODAY = "today"

        fun fromIntent(intent: Intent?): DeepLink? {
            if (intent == null) return null
            val streamUri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_STREAM)
            }
            return parse(
                action = intent.action,
                mimeType = intent.type,
                navigateTo = intent.getStringExtra(EXTRA_NAVIGATE_TO),
                itemId = intent.getStringExtra(EXTRA_ITEM_ID),
                sharedStream = streamUri?.toString()
            )
        }

        /** Pure mapping, kept separate from [Intent] so it can be unit tested. */
        fun parse(
            action: String?,
            mimeType: String?,
            navigateTo: String?,
            itemId: String?,
            sharedStream: String?
        ): DeepLink? {
            if (action == Intent.ACTION_SEND && mimeType?.startsWith("image/") == true) {
                return sharedStream?.let { SharedImage(it) }
            }
            return when (navigateTo) {
                TARGET_INSIGHTS_REPORT -> Insights
                TARGET_TODAY -> Today
                else -> itemId?.takeIf { it.isNotBlank() }?.let { ItemDetail(it) }
            }
        }
    }
}
