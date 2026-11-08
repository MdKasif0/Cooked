package com.cooked.app.core.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class CookedSpacing(
    val xs4: Dp = 4.dp,
    val s8: Dp = 8.dp,
    val m12: Dp = 12.dp,
    val l16: Dp = 16.dp,
    val xl24: Dp = 24.dp,
    val xxl32: Dp = 32.dp,
    val screenHorizontal: Dp = 20.dp
)

val LocalCookedSpacing = staticCompositionLocalOf { CookedSpacing() }

