package com.cooked.app.core.designsystem.components

import android.provider.Settings
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cooked.app.R
import com.cooked.app.core.designsystem.HeatLevel
import kotlinx.coroutines.delay

/**
 * Mascot representation of Cooked urgency states:
 * - Chill: Whole raw egg, calm face
 * - Warm: Sunny-side up, slight sweat drop
 * - Toasty: Crispy brown edges, worried
 * - Cooked: Burnt, smoking, funny/dramatic
 * - Done: Perfect egg with chef's kiss
 */
@Composable
fun EggMascot(
    modifier: Modifier = Modifier,
    level: HeatLevel? = HeatLevel.CHILL,
    isDone: Boolean = false,
    size: Dp = 96.dp,
    animated: Boolean = true
) {
    val context = LocalContext.current
    val isReducedMotion = remember(context) {
        try {
            val scale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            )
            scale == 0f
        } catch (_: Exception) {
            false
        }
    }

    val shouldAnimate = animated && !isReducedMotion

    @DrawableRes
    val drawableRes = when {
        isDone -> R.drawable.egg_done
        level == HeatLevel.WARM -> R.drawable.egg_warm
        level == HeatLevel.TOASTY -> R.drawable.egg_toasty
        level == HeatLevel.COOKED -> R.drawable.egg_focused
        else -> R.drawable.egg_chill
    }

    // Squash-and-stretch hop on state change
    val scaleX = remember { Animatable(1f) }
    val scaleY = remember { Animatable(1f) }

    LaunchedEffect(level, isDone, shouldAnimate) {
        if (shouldAnimate) {
            // Squash
            scaleX.animateTo(1.15f, animationSpec = tween(90, easing = FastOutSlowInEasing))
            scaleY.animateTo(0.85f, animationSpec = tween(90, easing = FastOutSlowInEasing))
            // Stretch hop
            scaleX.animateTo(0.92f, animationSpec = tween(120, easing = FastOutSlowInEasing))
            scaleY.animateTo(1.10f, animationSpec = tween(120, easing = FastOutSlowInEasing))
            // Settle
            scaleX.animateTo(1f, animationSpec = tween(100, easing = FastOutSlowInEasing))
            scaleY.animateTo(1f, animationSpec = tween(100, easing = FastOutSlowInEasing))
        }
    }

    // Idle wobble every ~6s (subtle tilt and gentle rebound)
    var wobbleAngle by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(shouldAnimate) {
        if (!shouldAnimate) return@LaunchedEffect
        while (true) {
            delay(5400) // Idle pause
            // Wobble left, right, settle
            val wobble = Animatable(0f)
            wobble.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 600
                    0f at 0 using FastOutSlowInEasing
                    -3.5f at 150
                    3.5f at 300
                    -1.5f at 450
                    0f at 600
                }
            ) {
                wobbleAngle = value
            }
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .scale(scaleX = scaleX.value, scaleY = scaleY.value)
            .rotate(wobbleAngle),
        contentAlignment = Alignment.Center
    ) {
        // Steam particles for Toasty and Cooked states
        if (shouldAnimate && (level == HeatLevel.TOASTY || level == HeatLevel.COOKED)) {
            EggSteamParticles(
                modifier = Modifier.size(size),
                isCooked = level == HeatLevel.COOKED
            )
        }

        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = "Cooked Mascot Egg (${if (isDone) "Done" else level?.name ?: "Chill"})",
            modifier = Modifier.size(size)
        )
    }
}

/**
 * 4 subtle rising steam particles floating above hot egg states
 */
@Composable
private fun EggSteamParticles(
    modifier: Modifier = Modifier,
    isCooked: Boolean
) {
    val steamAlpha = remember { Animatable(0.4f) }
    val steamOffset = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        steamOffset.animateTo(
            targetValue = -18f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    LaunchedEffect(Unit) {
        steamAlpha.animateTo(
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1800
                    0.4f at 0
                    0.3f at 600
                    0.1f at 1200
                    0f at 1800
                },
                repeatMode = RepeatMode.Restart
            )
        )
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val particleColor = if (isCooked) Color(0xFF78716C) else Color(0xFFD97706)

        val yDrift = steamOffset.value.dp.toPx()
        val alpha = steamAlpha.value

        // 4 drifting puff circles at different horizontal offsets
        val puffs = listOf(
            Offset(w * 0.35f, h * 0.15f + yDrift),
            Offset(w * 0.48f, h * 0.10f + yDrift * 1.2f),
            Offset(w * 0.62f, h * 0.16f + yDrift * 0.9f),
            Offset(w * 0.42f, h * 0.06f + yDrift * 1.4f)
        )

        puffs.forEachIndexed { index, pos ->
            val radius = (3.dp + (index * 0.8f).dp).toPx()
            drawCircle(
                color = particleColor.copy(alpha = (alpha * (0.8f - index * 0.12f)).coerceAtLeast(0f)),
                radius = radius,
                center = pos
            )
        }
    }
}

