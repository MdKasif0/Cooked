package com.cooked.app.core.config

/**
 * Feature flags for Cooked v1.1 growth capabilities.
 * Allows safely turning off or rolling out individual capabilities independently.
 */
object FeatureFlags {
    var isSnitchModeEnabled: Boolean = true
    var isDatesheetScanEnabled: Boolean = true
    var isLiveCountdownEnabled: Boolean = true
    var isTemplatesEnabled: Boolean = true
    var isCookSessionEnabled: Boolean = true
    var isPanicButtonEnabled: Boolean = true
    var isCalendarCapacityEnabled: Boolean = true
}

