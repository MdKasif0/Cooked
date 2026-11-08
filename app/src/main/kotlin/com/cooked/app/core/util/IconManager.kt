package com.cooked.app.core.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

enum class AppIconStyle(val title: String, val proOnly: Boolean) {
    DEFAULT("Sunny Ember", false),
    BURNT("Charred Burnt", true),
    MIDNIGHT("Obsidian Gold", true)
}

object IconManager {

    fun setAppIcon(context: Context, style: AppIconStyle) {
        val pm = context.packageManager
        val packageName = context.packageName

        val mainActivity = ComponentName(packageName, "$packageName.MainActivity")
        val burntAlias = ComponentName(packageName, "$packageName.MainActivityBurnt")
        val midnightAlias = ComponentName(packageName, "$packageName.MainActivityMidnight")

        val (enableMain, enableBurnt, enableMidnight) = when (style) {
            AppIconStyle.DEFAULT -> Triple(true, false, false)
            AppIconStyle.BURNT -> Triple(false, true, false)
            AppIconStyle.MIDNIGHT -> Triple(false, false, true)
        }

        setComponentState(pm, mainActivity, enableMain)
        setComponentState(pm, burntAlias, enableBurnt)
        setComponentState(pm, midnightAlias, enableMidnight)
    }

    private fun setComponentState(pm: PackageManager, component: ComponentName, enable: Boolean) {
        val newState = if (enable) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }

        try {
            if (pm.getComponentEnabledSetting(component) != newState) {
                pm.setComponentEnabledSetting(
                    component,
                    newState,
                    PackageManager.DONT_KILL_APP
                )
            }
        } catch (_: Exception) {}
    }
}
