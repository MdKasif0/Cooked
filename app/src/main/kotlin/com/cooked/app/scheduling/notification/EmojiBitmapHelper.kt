package com.cooked.app.scheduling.notification

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.annotation.ColorInt

object EmojiBitmapHelper {

    /**
     * Renders an emoji as a crisp 128x128 bitmap with an optional tinted circle background.
     * Suitable for NotificationCompat.Builder.setLargeIcon().
     */
    fun createEmojiBitmap(
        emoji: String,
        sizePx: Int = 128,
        @ColorInt backgroundColor: Int = Color.TRANSPARENT
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw circular background if specified
        if (backgroundColor != Color.TRANSPARENT) {
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = backgroundColor
                style = Paint.Style.FILL
            }
            val radius = sizePx / 2f
            canvas.drawCircle(radius, radius, radius, bgPaint)
        }

        // Draw emoji
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = sizePx * 0.62f
            textAlign = Paint.Align.CENTER
        }

        val bounds = Rect()
        textPaint.getTextBounds(emoji, 0, emoji.length, bounds)
        val yOffset = (sizePx / 2f) - bounds.exactCenterY()

        canvas.drawText(emoji, sizePx / 2f, yOffset, textPaint)

        return bitmap
    }
}
