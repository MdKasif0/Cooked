package com.cooked.app.core.designsystem

import androidx.compose.ui.graphics.Color
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class ColorContrastTest {

    private fun relativeLuminance(color: Color): Double {
        fun channelLum(c: Float): Double {
            return if (c <= 0.03928f) {
                (c / 12.92).toDouble()
            } else {
                ((c + 0.055) / 1.055).toDouble().pow(2.4)
            }
        }
        val r = channelLum(color.red)
        val g = channelLum(color.green)
        val b = channelLum(color.blue)
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    private fun contrastRatio(foreground: Color, background: Color): Double {
        val l1 = relativeLuminance(foreground)
        val l2 = relativeLuminance(background)
        val lighter = max(l1, l2)
        val darker = min(l1, l2)
        return (lighter + 0.05) / (darker + 0.05)
    }

    @Test
    fun lightTheme_bodyTextPairs_satisfyWcagAaBodyContrast() {
        // WCAG AA requirement for normal/body text: >= 4.5:1
        val onBackgroundRatio = contrastRatio(LightOnBackground, LightBackground)
        assertThat(onBackgroundRatio).isAtLeast(4.5)

        val onSurfaceRatio = contrastRatio(LightOnSurface, LightSurface)
        assertThat(onSurfaceRatio).isAtLeast(4.5)

        val onSurfaceVariantRatio = contrastRatio(LightOnSurfaceVariant, LightSurface)
        assertThat(onSurfaceVariantRatio).isAtLeast(4.5)

        val onPrimaryRatio = contrastRatio(LightOnPrimary, LightPrimary)
        assertThat(onPrimaryRatio).isAtLeast(4.5)

        val onPrimaryContainerRatio = contrastRatio(LightOnPrimaryContainer, LightPrimaryContainer)
        assertThat(onPrimaryContainerRatio).isAtLeast(4.5)
    }

    @Test
    fun darkTheme_bodyTextPairs_satisfyWcagAaBodyContrast() {
        val onBackgroundRatio = contrastRatio(DarkOnBackground, DarkBackground)
        assertThat(onBackgroundRatio).isAtLeast(4.5)

        val onSurfaceRatio = contrastRatio(DarkOnSurface, DarkSurface)
        assertThat(onSurfaceRatio).isAtLeast(4.5)

        val onSurfaceVariantRatio = contrastRatio(DarkOnSurfaceVariant, DarkSurface)
        assertThat(onSurfaceVariantRatio).isAtLeast(4.5)

        val onPrimaryRatio = contrastRatio(DarkOnPrimary, DarkPrimary)
        assertThat(onPrimaryRatio).isAtLeast(4.5)

        val onPrimaryContainerRatio = contrastRatio(DarkOnPrimaryContainer, DarkPrimaryContainer)
        assertThat(onPrimaryContainerRatio).isAtLeast(4.5)
    }

    @Test
    fun heatScale_containerTextPairs_satisfyWcagAaBodyContrast() {
        listOf(LightCookedColors, DarkCookedColors).forEach { palette ->
            HeatLevel.values().forEach { level ->
                val heat = palette.heatFor(level)
                val textOnContainerRatio = contrastRatio(heat.textColor, heat.container)
                assertThat(textOnContainerRatio).isAtLeast(4.5)
            }
        }
    }

    @Test
    fun neonAndHeroPairs_satisfyWcagAa_inBothThemes() {
        for (colors in listOf(LightCookedColors, DarkCookedColors)) {
            // Ink content on the volt accent (badges, nav selection, CTAs)
            assertThat(contrastRatio(colors.onAccent, colors.accent)).isAtLeast(4.5)
            // Insight / callout surfaces
            assertThat(contrastRatio(colors.onAccentContainer, colors.accentContainer)).isAtLeast(4.5)
            // Neon-family text placed directly on the canvas
            assertThat(contrastRatio(colors.accentText, colors.background)).isAtLeast(4.5)
            // Hero panels: primary + secondary text, and the volt highlight
            assertThat(contrastRatio(colors.onHero, colors.hero)).isAtLeast(4.5)
            assertThat(contrastRatio(colors.onHeroMuted, colors.hero)).isAtLeast(4.5)
            assertThat(contrastRatio(colors.accent, colors.hero)).isAtLeast(4.5)
        }
    }
}
