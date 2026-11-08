package com.cooked.app.core.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Light Palette (Warm-neutral, WCAG AA compliant)
val LightBackground = Color(0xFFFAF7F2)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceContainerLow = Color(0xFFF6F2EC)
val LightSurfaceContainer = Color(0xFFF1ECE4)
val LightSurfaceContainerHigh = Color(0xFFEAE3D9)
val LightOutline = Color(0xFFE2D9CC)
val LightOutlineVariant = Color(0xFFEEE7DD)
val LightOnBackground = Color(0xFF1A1714)
val LightOnSurface = Color(0xFF1A1714)
val LightOnSurfaceVariant = Color(0xFF6B635A)
val LightPrimary = Color(0xFFC2410C) // Ember
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFFFE4D6)
val LightOnPrimaryContainer = Color(0xFF5A1A02)

// Dark Palette (Warm-neutral deep dark, WCAG AA compliant)
val DarkBackground = Color(0xFF0E0D0C)
val DarkSurface = Color(0xFF151311)
val DarkSurfaceContainerLow = Color(0xFF191715)
val DarkSurfaceContainer = Color(0xFF1F1C19)
val DarkSurfaceContainerHigh = Color(0xFF282420)
val DarkOutline = Color(0xFF342E28)
val DarkOutlineVariant = Color(0xFF26221E)
val DarkOnBackground = Color(0xFFF4EFE9)
val DarkOnSurface = Color(0xFFF4EFE9)
val DarkOnSurfaceVariant = Color(0xFFA89F94)
val DarkPrimary = Color(0xFFFF7A45) // Ember glow
val DarkOnPrimary = Color(0xFF1C0A02)
val DarkPrimaryContainer = Color(0xFF4A1C08)
val DarkOnPrimaryContainer = Color(0xFFFFD9C7)

// Heat Scale - Light variants
val HeatChillLight = Color(0xFF2F9E94)
val HeatChillLightContainer = Color(0xFFE0F5F2)
val HeatChillLightOnContainer = Color(0xFF136B63)

val HeatWarmLight = Color(0xFFC98A04)
val HeatWarmLightText = Color(0xFFA16207) // Adjusted for 4.9:1 AA body contrast on light backgrounds
val HeatWarmLightContainer = Color(0xFFFEF3C7)
val HeatWarmLightOnContainer = Color(0xFF854D0E)

val HeatToastyLight = Color(0xFFE2620F)
val HeatToastyLightContainer = Color(0xFFFFEDD5)
val HeatToastyLightOnContainer = Color(0xFF9A3412)

val HeatCookedLight = Color(0xFFD61F45)
val HeatCookedLightContainer = Color(0xFFFFE4E8)
val HeatCookedLightOnContainer = Color(0xFF9F1239)

val HeatDoneLight = Color(0xFF3C8D5A)
val HeatDoneLightContainer = Color(0xFFE8F5E9)
val HeatDoneLightOnContainer = Color(0xFF1E5E35)

// Heat Scale - Dark variants
val HeatChillDark = Color(0xFF5CCFC3)
val HeatChillDarkContainer = Color(0xFF123835)
val HeatChillDarkOnContainer = Color(0xFF8AEDDE)

val HeatWarmDark = Color(0xFFF2B634)
val HeatWarmDarkContainer = Color(0xFF3A2E0E)
val HeatWarmDarkOnContainer = Color(0xFFFDE68A)

val HeatToastyDark = Color(0xFFFF8A3D)
val HeatToastyDarkContainer = Color(0xFF3E2110)
val HeatToastyDarkOnContainer = Color(0xFFFED7AA)

val HeatCookedDark = Color(0xFFFF5C7A)
val HeatCookedDarkContainer = Color(0xFF421520)
val HeatCookedDarkOnContainer = Color(0xFFFECDD3)

val HeatDoneDark = Color(0xFF6BCB8B)
val HeatDoneDarkContainer = Color(0xFF163321)
val HeatDoneDarkOnContainer = Color(0xFFA7F3D0)

@Immutable
data class HeatColors(
    val color: Color,
    val textColor: Color,
    val onColor: Color,
    val container: Color,
    val onContainer: Color
)

@Immutable
data class CookedColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceContainerLow: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val outlineVariant: Color,
    val chill: HeatColors,
    val warm: HeatColors,
    val toasty: HeatColors,
    val cooked: HeatColors,
    val done: HeatColors,
    val isDark: Boolean
) {
    fun heatFor(level: HeatLevel): HeatColors = when (level) {
        HeatLevel.CHILL -> chill
        HeatLevel.WARM -> warm
        HeatLevel.TOASTY -> toasty
        HeatLevel.COOKED -> cooked
        HeatLevel.DONE -> done
    }
}

val LightCookedColors = CookedColors(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    chill = HeatColors(
        color = HeatChillLight,
        textColor = HeatChillLightOnContainer,
        onColor = Color.White,
        container = HeatChillLightContainer,
        onContainer = HeatChillLightOnContainer
    ),
    warm = HeatColors(
        color = HeatWarmLight,
        textColor = HeatWarmLightText,
        onColor = LightOnBackground,
        container = HeatWarmLightContainer,
        onContainer = HeatWarmLightOnContainer
    ),
    toasty = HeatColors(
        color = HeatToastyLight,
        textColor = HeatToastyLightOnContainer,
        onColor = Color.White,
        container = HeatToastyLightContainer,
        onContainer = HeatToastyLightOnContainer
    ),
    cooked = HeatColors(
        color = HeatCookedLight,
        textColor = HeatCookedLightOnContainer,
        onColor = Color.White,
        container = HeatCookedLightContainer,
        onContainer = HeatCookedLightOnContainer
    ),
    done = HeatColors(
        color = HeatDoneLight,
        textColor = HeatDoneLightOnContainer,
        onColor = Color.White,
        container = HeatDoneLightContainer,
        onContainer = HeatDoneLightOnContainer
    ),
    isDark = false
)

val DarkCookedColors = CookedColors(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    chill = HeatColors(
        color = HeatChillDark,
        textColor = HeatChillDark,
        onColor = DarkOnPrimary,
        container = HeatChillDarkContainer,
        onContainer = HeatChillDarkOnContainer
    ),
    warm = HeatColors(
        color = HeatWarmDark,
        textColor = HeatWarmDark,
        onColor = DarkOnPrimary,
        container = HeatWarmDarkContainer,
        onContainer = HeatWarmDarkOnContainer
    ),
    toasty = HeatColors(
        color = HeatToastyDark,
        textColor = HeatToastyDark,
        onColor = DarkOnPrimary,
        container = HeatToastyDarkContainer,
        onContainer = HeatToastyDarkOnContainer
    ),
    cooked = HeatColors(
        color = HeatCookedDark,
        textColor = HeatCookedDark,
        onColor = DarkOnPrimary,
        container = HeatCookedDarkContainer,
        onContainer = HeatCookedDarkOnContainer
    ),
    done = HeatColors(
        color = HeatDoneDark,
        textColor = HeatDoneDark,
        onColor = DarkOnPrimary,
        container = HeatDoneDarkContainer,
        onContainer = HeatDoneDarkOnContainer
    ),
    isDark = true
)

val LocalCookedColors = staticCompositionLocalOf<CookedColors> {
    error("No CookedColors provided")
}
