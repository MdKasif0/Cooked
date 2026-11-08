package com.cooked.app.core.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
// "Ivory & Volt" — Light palette
// Warm ivory canvas, ink-black actions, volt-neon accents. Separation comes from
// tone + soft diffused shadow, not hairline borders.
// ─────────────────────────────────────────────────────────────────────────────
val Volt = Color(0xFFD4FF3F)            // Signature neon accent
val VoltSoft = Color(0xFFEDF9C4)        // Lime tint for insight surfaces (light)
val VoltDeep = Color(0xFF4F6300)        // Neon-family text that passes AA on ivory
val Ink = Color(0xFF111210)             // Near-black for primary actions & hero cards
val InkRaised = Color(0xFF1D1E1B)       // Raised element on an ink surface

val LightBackground = Color(0xFFEFECE6)
val LightSurface = Color(0xFFFBFAF7)
val LightSurfaceContainerLow = Color(0xFFF6F4EF)
val LightSurfaceContainer = Color(0xFFE8E4DC)
val LightSurfaceContainerHigh = Color(0xFFDEDAD1)
val LightOutline = Color(0xFFD6D1C6)
val LightOutlineVariant = Color(0xFFE5E1D8)
val LightOnBackground = Color(0xFF111210)
val LightOnSurface = Color(0xFF111210)
val LightOnSurfaceVariant = Color(0xFF6B6860)
val LightPrimary = Ink                  // Actions are ink in light mode
val LightOnPrimary = Color(0xFFFBFAF7)
val LightPrimaryContainer = Volt        // Selected states glow neon
val LightOnPrimaryContainer = Ink

// ─────────────────────────────────────────────────────────────────────────────
// "Obsidian & Volt" — Dark palette
// ─────────────────────────────────────────────────────────────────────────────
val DarkBackground = Color(0xFF0A0B09)
val DarkSurface = Color(0xFF141512)
val DarkSurfaceContainerLow = Color(0xFF111210)
val DarkSurfaceContainer = Color(0xFF1B1C19)
val DarkSurfaceContainerHigh = Color(0xFF262723)
val DarkOutline = Color(0xFF34362F)
val DarkOutlineVariant = Color(0xFF23241F)
val DarkOnBackground = Color(0xFFF2F3EC)
val DarkOnSurface = Color(0xFFF2F3EC)
val DarkOnSurfaceVariant = Color(0xFF9C9E94)
val DarkPrimary = Volt                  // Actions glow neon in dark mode
val DarkOnPrimary = Color(0xFF0A0B09)
val DarkPrimaryContainer = Color(0xFF29330A)
val DarkOnPrimaryContainer = Color(0xFFE2FF86)

// Heat Scale - Light variants
val HeatChillLight = Color(0xFF2E8C83)
val HeatChillLightContainer = Color(0xFFDDEFEA)
val HeatChillLightOnContainer = Color(0xFF145F58)

val HeatWarmLight = Color(0xFFB98507)
val HeatWarmLightText = Color(0xFF7A4A08) // AA body contrast on container/background
val HeatWarmLightContainer = Color(0xFFF6EBC8)
val HeatWarmLightOnContainer = Color(0xFF7A4A08)

val HeatToastyLight = Color(0xFFE0610F)
val HeatToastyLightContainer = Color(0xFFFBE3D0)
val HeatToastyLightOnContainer = Color(0xFF933210)

val HeatCookedLight = Color(0xFFE0264E)
val HeatCookedLightContainer = Color(0xFFFADCE2)
val HeatCookedLightOnContainer = Color(0xFF951236)

val HeatDoneLight = Color(0xFF5C7F00)
val HeatDoneLightContainer = VoltSoft
val HeatDoneLightOnContainer = VoltDeep

// Heat Scale - Dark variants
val HeatChillDark = Color(0xFF5FD3C6)
val HeatChillDarkContainer = Color(0xFF10302D)
val HeatChillDarkOnContainer = Color(0xFF9DEFE3)

val HeatWarmDark = Color(0xFFF4BE3E)
val HeatWarmDarkContainer = Color(0xFF332810)
val HeatWarmDarkOnContainer = Color(0xFFFCE59A)

val HeatToastyDark = Color(0xFFFF8F45)
val HeatToastyDarkContainer = Color(0xFF3A1F10)
val HeatToastyDarkOnContainer = Color(0xFFFED3B0)

val HeatCookedDark = Color(0xFFFF5E7E)
val HeatCookedDarkContainer = Color(0xFF3D1420)
val HeatCookedDarkOnContainer = Color(0xFFFFCCD6)

val HeatDoneDark = Volt
val HeatDoneDarkContainer = DarkPrimaryContainer
val HeatDoneDarkOnContainer = DarkOnPrimaryContainer

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
    /** Signature neon. Use as a fill (with [onAccent] content), never as text on light surfaces. */
    val accent: Color,
    val onAccent: Color,
    /** Soft neon-tinted surface for insights / positive callouts. */
    val accentContainer: Color,
    val onAccentContainer: Color,
    /** Neon-family color that is legible as text/icons on the regular background. */
    val accentText: Color,
    /** Dark "hero" panel used for the most important card on a screen (score, forecast). */
    val hero: Color,
    val onHero: Color,
    val onHeroMuted: Color,
    val heroRaised: Color,
    /** Ambient shadow tint for floating elements. */
    val shadow: Color,
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
    accent = Volt,
    onAccent = Ink,
    accentContainer = VoltSoft,
    onAccentContainer = VoltDeep,
    accentText = VoltDeep,
    hero = Ink,
    onHero = Color(0xFFF4F4EE),
    onHeroMuted = Color(0xFF8F9087),
    heroRaised = InkRaised,
    shadow = Color(0xFF3A3528),
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
    accent = Volt,
    onAccent = Color(0xFF0A0B09),
    accentContainer = DarkPrimaryContainer,
    onAccentContainer = DarkOnPrimaryContainer,
    accentText = Volt,
    hero = Color(0xFF181915),
    onHero = Color(0xFFF2F3EC),
    onHeroMuted = Color(0xFF8E9086),
    heroRaised = Color(0xFF242520),
    shadow = Color(0xFF000000),
    isDark = true
)

val LocalCookedColors = staticCompositionLocalOf<CookedColors> {
    error("No CookedColors provided")
}

// Backward-compatibility aliases for Step 1 screens
val FlameOrange = VoltDeep
val FlameOrangeDark = Ink
val FlameAmber = HeatWarmLight
val FlameGold = Volt
val CharcoalSlate = Ink
val WarmOffWhite = LightBackground
val CardSurfaceLight = LightSurface
val TextPrimaryLight = LightOnBackground
val TextSecondaryLight = LightOnSurfaceVariant
val BorderLight = LightOutline
val FlameOrangeGlow = Volt
val DarkSurfaceElevated = DarkSurfaceContainerHigh
val TextPrimaryDark = DarkOnBackground
val TextSecondaryDark = DarkOnSurfaceVariant
val BorderDark = DarkOutline
val PressureChill = HeatChillLight
val PressureSimmer = HeatWarmLight
val PressureCooked = HeatToastyLight
val PressureCrispy = HeatCookedLight
