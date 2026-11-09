package com.cooked.app.feature.onboarding

import androidx.compose.ui.graphics.Color
import com.cooked.app.domain.model.Personality
import com.cooked.app.feature.onboarding.components.MILD_NOTIFICATIONS
import com.cooked.app.feature.onboarding.components.PERSONALITY_OPTIONS
import com.cooked.app.feature.onboarding.components.SPICY_NOTIFICATIONS
import com.cooked.app.feature.onboarding.components.UNHINGED_NOTIFICATIONS
import com.cooked.app.feature.onboarding.components.VibeTokens
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class PickYourVibeTest {

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
    fun personalityOptions_containAllThreePersonalitiesWithAccurateCopy() {
        assertThat(PERSONALITY_OPTIONS).hasSize(3)

        val mild = PERSONALITY_OPTIONS.first { it.personality == Personality.MILD }
        assertThat(mild.title).isEqualTo("Mild")
        assertThat(mild.description).contains("Friendly")

        val spicy = PERSONALITY_OPTIONS.first { it.personality == Personality.SPICY }
        assertThat(spicy.title).isEqualTo("Spicy")
        assertThat(spicy.description).contains("Sarcastic")

        val unhinged = PERSONALITY_OPTIONS.first { it.personality == Personality.UNHINGED }
        assertThat(unhinged.title).isEqualTo("Unhinged")
        assertThat(unhinged.description).contains("Maximum accountability")
    }

    @Test
    fun mildNotifications_matchDesignSpec() {
        assertThat(MILD_NOTIFICATIONS).hasSize(3)
        assertThat(MILD_NOTIFICATIONS[0].stage).isEqualTo("Friendly reminder")
        assertThat(MILD_NOTIFICATIONS[0].message).isEqualTo("Hey! Don’t forget your final project. You’ve got this.")

        assertThat(MILD_NOTIFICATIONS[1].stage).isEqualTo("Gentle nudge")
        assertThat(MILD_NOTIFICATIONS[1].message).isEqualTo("Just checking in. Your project is still waiting.")

        assertThat(MILD_NOTIFICATIONS[2].stage).isEqualTo("Last reminder")
        assertThat(MILD_NOTIFICATIONS[2].message).isEqualTo("Time to wrap this up. Let’s get it done!")
    }

    @Test
    fun spicyNotifications_matchDesignSpec() {
        assertThat(SPICY_NOTIFICATIONS).hasSize(3)
        assertThat(SPICY_NOTIFICATIONS[0].stage).isEqualTo("First reminder")
        assertThat(SPICY_NOTIFICATIONS[0].message).isEqualTo("Heat is on for Submit final project.")

        assertThat(SPICY_NOTIFICATIONS[1].stage).isEqualTo("Nudge #2")
        assertThat(SPICY_NOTIFICATIONS[1].message).isEqualTo("Friendly reminder that Submit final project isn’t doing itself.")

        assertThat(SPICY_NOTIFICATIONS[2].stage).isEqualTo("Final call")
        assertThat(SPICY_NOTIFICATIONS[2].message).isEqualTo("Stop negotiating with your deadline. Get it done.")
    }

    @Test
    fun unhingedNotifications_matchDesignSpec() {
        assertThat(UNHINGED_NOTIFICATIONS).hasSize(3)
        assertThat(UNHINGED_NOTIFICATIONS[0].stage).isEqualTo("Incoming")
        assertThat(UNHINGED_NOTIFICATIONS[0].message).isEqualTo("BESTIE. THE PROJECT.")

        assertThat(UNHINGED_NOTIFICATIONS[1].stage).isEqualTo("Emergency")
        assertThat(UNHINGED_NOTIFICATIONS[1].message).isEqualTo("Your deadline is approaching and you’re just existing?")

        assertThat(UNHINGED_NOTIFICATIONS[2].stage).isEqualTo("FINAL WARNING")
        assertThat(UNHINGED_NOTIFICATIONS[2].message).isEqualTo("THIS IS NOT A DRILL. GO FINISH IT.")
    }

    @Test
    fun vibeColorTokens_meetAccessibilityContrastRatios() {
        // Selected State: Dark text on Lime accent background
        val selectedTextContrast = contrastRatio(Color(0xFF0B0C0A), VibeTokens.PrimaryAccent)
        assertThat(selectedTextContrast).isAtLeast(10.0) // Well above AAA 7.0 standard

        // Primary Text on Dark Background
        val primaryTextContrast = contrastRatio(VibeTokens.PrimaryText, VibeTokens.Background)
        assertThat(primaryTextContrast).isAtLeast(15.0)

        // Secondary Text on Dark Background
        val secondaryTextContrast = contrastRatio(VibeTokens.SecondaryText, VibeTokens.Background)
        assertThat(secondaryTextContrast).isAtLeast(4.5) // Meets AA standard

        // CTA Button: Dark text on Lime accent
        val ctaTextContrast = contrastRatio(Color(0xFF0B0C0A), VibeTokens.PrimaryAccent)
        assertThat(ctaTextContrast).isAtLeast(10.0)
    }
}
