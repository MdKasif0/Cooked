package com.cooked.app.feature.onboarding

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MakeItReliableTest {

    @Test
    fun configuredCount_calculatesAccurately() {
        fun countConfigured(notif: Boolean, alarms: Boolean, battery: Boolean): Int {
            return listOf(notif, alarms, battery).count { it }
        }

        assertThat(countConfigured(notif = true, alarms = true, battery = true)).isEqualTo(3)
        assertThat(countConfigured(notif = true, alarms = false, battery = false)).isEqualTo(1)
        assertThat(countConfigured(notif = true, alarms = true, battery = false)).isEqualTo(2)
        assertThat(countConfigured(notif = false, alarms = false, battery = false)).isEqualTo(0)
    }

    @Test
    fun buttonCtaLabel_adaptsToReliabilityState() {
        fun getCtaLabel(allConfigured: Boolean): String {
            return if (allConfigured) "You’re all set" else "Lock in reliability"
        }

        assertThat(getCtaLabel(allConfigured = true)).isEqualTo("You’re all set")
        assertThat(getCtaLabel(allConfigured = false)).isEqualTo("Lock in reliability")
    }

    @Test
    fun permissionCardStrings_matchProductSpecification() {
        val notifTitle = "Notifications"
        val notifDesc = "Let Cooked send reminders and deadline alerts when you need them."

        val alarmTitle = "Exact Alarms"
        val alarmDesc = "Helps Cooked trigger important reminders close to their scheduled time."

        val batteryTitle = "Background Reliability"
        val batteryDesc = "Some battery-saving features can delay background reminders. Check your device’s battery settings for the most reliable experience."

        assertThat(notifTitle).isEqualTo("Notifications")
        assertThat(notifDesc).contains("reminders and deadline alerts")

        assertThat(alarmTitle).isEqualTo("Exact Alarms")
        assertThat(alarmDesc).contains("close to their scheduled time")

        assertThat(batteryTitle).isEqualTo("Background Reliability")
        assertThat(batteryDesc).contains("battery-saving features")
    }
}
