package com.cooked.app.domain.snitch

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import androidx.core.content.FileProvider
import com.cooked.app.core.designsystem.DarkBackground
import com.cooked.app.core.designsystem.DarkPrimary
import com.cooked.app.domain.model.Personality
import java.io.File
import java.io.FileOutputStream

object SnitchManager {

    const val DEFAULT_PLAY_STORE_LINK = "https://play.google.com/store/apps/details?id=com.cooked.app"

    fun buildSnitchMessage(
        personality: Personality,
        contactName: String,
        itemTitle: String,
        snoozeCount: Int,
        playStoreLink: String = DEFAULT_PLAY_STORE_LINK
    ): String {
        val nameLabel = if (contactName.isNotBlank()) contactName else "Your friend"
        return when (personality) {
            Personality.MILD -> {
                "$nameLabel has snoozed '$itemTitle' $snoozeCount times and asked for an accountability check. Please check in on them! - sent via Cooked $playStoreLink"
            }
            Personality.SPICY -> {
                "$nameLabel has snoozed '$itemTitle' $snoozeCount times. Please bully them into finishing it. - sent via Cooked $playStoreLink"
            }
            Personality.UNHINGED -> {
                "🚨 EMERGENCY ACCOUNTABILITY ALERT 🚨 $nameLabel has snoozed '$itemTitle' $snoozeCount TIMES. Roast them into submission immediately. - sent via Cooked $playStoreLink"
            }
        }
    }

    fun createShareIntent(
        context: Context,
        message: String,
        preferredApp: String = "whatsapp"
    ): Intent {
        val isWhatsAppInstalled = try {
            context.packageManager.getPackageInfo("com.whatsapp", 0)
            true
        } catch (_: Exception) {
            false
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }

        return if (preferredApp == "whatsapp" && isWhatsAppInstalled) {
            sendIntent.setPackage("com.whatsapp")
            sendIntent
        } else {
            Intent.createChooser(sendIntent, "Share with Accountability Buddy")
        }
    }

    /**
     * Creates an image card for Instagram / WhatsApp stories.
     */
    fun createStoryCardUri(
        context: Context,
        itemTitle: String,
        snoozeCount: Int
    ): Uri? {
        return try {
            val size = 1080
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Background #0E0D0C
            val bgPaint = Paint().apply {
                color = 0xFF0E0D0C.toInt()
                isAntiAlias = true
            }
            canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), bgPaint)

            // Ember Border
            val borderPaint = Paint().apply {
                color = 0xFFC2410C.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 24f
                isAntiAlias = true
            }
            val borderRect = RectF(48f, 48f, size - 48f, size - 48f)
            canvas.drawRoundRect(borderRect, 64f, 64f, borderPaint)

            // Emoji / Mascot circle
            val circlePaint = Paint().apply {
                color = 0xFF1F1C19.toInt()
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawCircle(size / 2f, 320f, 130f, circlePaint)

            // Mascot text / emoji
            val emojiPaint = Paint().apply {
                textSize = 120f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("🍳", size / 2f, 360f, emojiPaint)

            // Snooze callout
            val headlinePaint = Paint().apply {
                color = 0xFFFF7A45.toInt() // Ember glow
                textSize = 58f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("I'VE SNOOZED THIS $snoozeCount TIMES", size / 2f, 540f, headlinePaint)

            // Task title
            val titlePaint = Paint().apply {
                color = 0xFFF4EFE9.toInt()
                textSize = 52f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            val displayTitle = if (itemTitle.length > 28) itemTitle.take(25) + "..." else itemTitle
            canvas.drawText("“$displayTitle”", size / 2f, 640f, titlePaint)

            // Roast caption
            val roastPaint = Paint().apply {
                color = 0xFFA89F94.toInt()
                textSize = 38f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("Please bully me until it's done.", size / 2f, 740f, roastPaint)

            // Footer branding
            val brandPaint = Paint().apply {
                color = 0xFFC2410C.toInt()
                textSize = 34f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("Cooked · Reminders that won't shut up", size / 2f, 960f, brandPaint)

            // Save to cache
            val cacheDir = File(context.cacheDir, "snitch_cards").apply { mkdirs() }
            val cardFile = File(cacheDir, "snitch_${System.currentTimeMillis()}.png")
            FileOutputStream(cardFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", cardFile)
        } catch (_: Exception) {
            null
        }
    }
}
