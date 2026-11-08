package com.cooked.app.scheduling.notification

import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class EmojiBitmapHelperTest {

    @Test
    fun createEmojiBitmapReturnsExactDimensions() {
        val bitmap = EmojiBitmapHelper.createEmojiBitmap("🔥", sizePx = 128)
        assertNotNull(bitmap)
        assertEquals(128, bitmap.width)
        assertEquals(128, bitmap.height)
    }

    @Test
    fun createEmojiBitmapWithTintedBackground() {
        val bitmap = EmojiBitmapHelper.createEmojiBitmap("🎯", sizePx = 64, backgroundColor = Color.RED)
        assertNotNull(bitmap)
        assertEquals(64, bitmap.width)
        assertEquals(64, bitmap.height)
    }
}
