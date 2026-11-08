package com.cooked.app.core.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.net.Uri
import com.cooked.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(audioAttributes)
        .build()

    private val donePopId = soundPool.load(context, R.raw.done_pop, 1)
    private var previewPlayer: MediaPlayer? = null

    /**
     * In-app completion pop sound.
     * Guaranteed to never play if device is set to silent or vibrate.
     */
    fun playDonePop(force: Boolean = false) {
        if (!isRingerAudible() && !force) return
        soundPool.play(donePopId, 0.7f, 0.7f, 1, 0, 1.0f)
    }

    /**
     * Preview sound for Settings screen.
     */
    fun previewSound(resId: Int) {
        if (!isRingerAudible()) return

        stopPreview()
        try {
            previewPlayer = MediaPlayer.create(context, resId)?.apply {
                setOnCompletionListener {
                    it.release()
                    previewPlayer = null
                }
                start()
            }
        } catch (_: Exception) {}
    }

    fun stopPreview() {
        try {
            previewPlayer?.stop()
            previewPlayer?.release()
        } catch (_: Exception) {}
        previewPlayer = null
    }

    fun isRingerAudible(): Boolean {
        val mode = audioManager?.ringerMode ?: AudioManager.RINGER_MODE_NORMAL
        return mode == AudioManager.RINGER_MODE_NORMAL
    }

    companion object {
        fun getSoundUri(context: Context, resId: Int): Uri {
            return Uri.parse("android.resource://${context.packageName}/$resId")
        }
    }
}
