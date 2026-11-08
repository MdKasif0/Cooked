package com.cooked.app.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
data class AddEditReminderRoute(
    val reminderId: String? = null
)

@Serializable
object DeadlineDashboardRoute

@Serializable
object OemGuideRoute

