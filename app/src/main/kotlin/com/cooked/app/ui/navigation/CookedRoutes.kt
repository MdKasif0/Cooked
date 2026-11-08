package com.cooked.app.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object TodayTabRoute

@Serializable
object DeadlinesTabRoute

@Serializable
object InsightsTabRoute

@Serializable
object SettingsNavRoute

@Serializable
object DesignGalleryNavRoute

@Serializable
data class AddEditReminderRoute(
    val reminderId: String? = null
)

@Serializable
data class ReminderDetailNavRoute(
    val reminderId: String
)

@Serializable
object OemGuideNavRoute

@Serializable
object ReliabilityNavRoute

@Serializable
object OnboardingNavRoute

@Serializable
object PaywallNavRoute
