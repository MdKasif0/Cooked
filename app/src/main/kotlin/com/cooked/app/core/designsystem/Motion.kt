package com.cooked.app.core.designsystem

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

object CookedMotion {
    // 0.85 damping ratio, medium stiffness for snappy, controlled movement
    const val SpringDampingRatio = 0.85f
    const val SpringStiffness = Spring.StiffnessMedium

    fun <T> layoutSpring(): FiniteAnimationSpec<T> = spring(
        dampingRatio = SpringDampingRatio,
        stiffness = SpringStiffness
    )

    // Fades: 180-280ms (standard 220ms)
    const val FadeDurationFast = 180
    const val FadeDurationMedium = 220
    const val FadeDurationSlow = 280

    fun <T> fadeSpec(durationMillis: Int = FadeDurationMedium): FiniteAnimationSpec<T> =
        tween(durationMillis = durationMillis)

    /**
     * Checks if system has animations disabled (e.g., accessibility setting "Remove animations")
     */
    fun areAnimationsDisabled(context: Context): Boolean {
        return try {
            val resolver = context.contentResolver
            val animatorScale = Settings.Global.getFloat(
                resolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
            val transitionScale = Settings.Global.getFloat(
                resolver,
                Settings.Global.TRANSITION_ANIMATION_SCALE,
                1.0f
            )
            animatorScale == 0f || transitionScale == 0f
        } catch (_: Exception) {
            false
        }
    }
}

@Composable
fun rememberAreAnimationsDisabled(): Boolean {
    val context = LocalContext.current
    return remember(context) { CookedMotion.areAnimationsDisabled(context) }
}

/**
 * Haptic helpers for Cooked interactions:
 * - Light tick on toggle/stepper
 * - Confirm haptic on Done/success
 */
class CookedHaptics(
    private val hapticFeedback: HapticFeedback,
    private val view: View?
) {
    fun lightTick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && view != null) {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        } else {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    fun confirmDone() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && view != null) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    fun rejectWarning() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && view != null) {
            view.performHapticFeedback(HapticFeedbackConstants.REJECT)
        } else {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
}
