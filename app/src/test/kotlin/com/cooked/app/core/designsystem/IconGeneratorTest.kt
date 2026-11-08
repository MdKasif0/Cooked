package com.cooked.app.core.designsystem

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import com.cooked.app.R
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class IconGeneratorTest {

    @Test
    fun generateAppLauncherMipmaps() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val bgDrawable = ContextCompat.getDrawable(context, R.drawable.ic_launcher_background)
        val fgDrawable = ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground)

        val densities = mapOf(
            "mipmap-mdpi" to 48,
            "mipmap-hdpi" to 72,
            "mipmap-xhdpi" to 96,
            "mipmap-xxhdpi" to 144,
            "mipmap-xxxhdpi" to 192
        )

        for ((folder, size) in densities) {
            val targetDir = File("src/main/res/$folder")
            val altDir = if (targetDir.exists()) targetDir else File("app/src/main/res/$folder")

            // Square / Rounded squircle icon (full bleed background, centered foreground)
            val squareBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val squareCanvas = Canvas(squareBitmap)

            // Adaptive icon viewport is 108dp, safe area is 72dp.
            // When flattening adaptive icon to standard legacy icon, scale is (size * 108 / 72)
            val scale = size.toFloat() / 72f
            val totalSize = (108f * scale).toInt()
            val offset = ((totalSize - size) / 2)

            bgDrawable?.setBounds(-offset, -offset, totalSize - offset, totalSize - offset)
            bgDrawable?.draw(squareCanvas)

            fgDrawable?.setBounds(-offset, -offset, totalSize - offset, totalSize - offset)
            fgDrawable?.draw(squareCanvas)

            val squareFile = File(altDir, "ic_launcher.webp")
            FileOutputStream(squareFile).use { out ->
                squareBitmap.compress(Bitmap.CompressFormat.WEBP, 100, out)
            }

            // Round icon (circle clip)
            val roundBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val roundCanvas = Canvas(roundBitmap)
            val clipPath = Path().apply {
                addOval(RectF(0f, 0f, size.toFloat(), size.toFloat()), Path.Direction.CW)
            }
            roundCanvas.clipPath(clipPath)

            bgDrawable?.setBounds(-offset, -offset, totalSize - offset, totalSize - offset)
            bgDrawable?.draw(roundCanvas)

            fgDrawable?.setBounds(-offset, -offset, totalSize - offset, totalSize - offset)
            fgDrawable?.draw(roundCanvas)

            val roundFile = File(altDir, "ic_launcher_round.webp")
            FileOutputStream(roundFile).use { out ->
                roundBitmap.compress(Bitmap.CompressFormat.WEBP, 100, out)
            }
        }
    }
}
