package com.cooked.app.core.oem

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import java.util.Locale

enum class OemType {
    XIAOMI,
    SAMSUNG,
    OPPO,
    VIVO,
    ONEPLUS,
    OTHER
}

object OemDetector {

    val currentOem: OemType by lazy {
        val manufacturer = Build.MANUFACTURER.lowercase(Locale.ROOT)
        when {
            manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco") -> OemType.XIAOMI
            manufacturer.contains("samsung") -> OemType.SAMSUNG
            manufacturer.contains("oppo") || manufacturer.contains("realme") -> OemType.OPPO
            manufacturer.contains("vivo") || manufacturer.contains("iqoo") -> OemType.VIVO
            manufacturer.contains("oneplus") -> OemType.ONEPLUS
            else -> OemType.OTHER
        }
    }

    fun isAggressiveOem(): Boolean = currentOem != OemType.OTHER

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: true
    }

    fun createRequestIgnoreBatteryOptimizationsIntent(context: Context): Intent {
        return Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
    }

    fun createAutostartIntent(context: Context): Intent? {
        val intents = when (currentOem) {
            OemType.XIAOMI -> listOf(
                Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
                Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.powercenter.PowerSettings"))
            )
            OemType.OPPO -> listOf(
                Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
                Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
                Intent().setComponent(ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity"))
            )
            OemType.VIVO -> listOf(
                Intent().setComponent(ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
                Intent().setComponent(ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
                Intent().setComponent(ComponentName("com.iqoo.secure", "com.iqoo.secure.MainGuideActivity"))
            )
            OemType.ONEPLUS -> listOf(
                Intent().setComponent(ComponentName("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListAct"))
            )
            OemType.SAMSUNG -> listOf(
                Intent().setComponent(ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")),
                Intent().setComponent(ComponentName("com.samsung.android.sm", "com.samsung.android.sm.ui.battery.BatteryActivity"))
            )
            OemType.OTHER -> emptyList()
        }

        val packageManager = context.packageManager
        for (intent in intents) {
            if (intent.resolveActivity(packageManager) != null) {
                return intent
            }
        }

        // Fallback to standard application details
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
    }
}

