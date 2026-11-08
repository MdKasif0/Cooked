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
data class PaywallNavRoute(
    val entryPoint: String = "generic",
    val featureHint: String? = null
)

@Serializable
data class DatesheetReviewNavRoute(
    val imageUri: String? = null
)

@Serializable
data class CookSessionNavRoute(
    val itemId: String
)

@Serializable
data class RescuePlanNavRoute(
    val targetItemId: String
)
