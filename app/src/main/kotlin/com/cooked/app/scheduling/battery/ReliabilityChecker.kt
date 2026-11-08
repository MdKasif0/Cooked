package com.cooked.app.scheduling.battery

import android.app.AlarmManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import com.cooked.app.scheduling.notification.NotificationChannels
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class ReliabilityReport(
    val canScheduleExactAlarms: Boolean,
    val isIgnoringBatteryOptimizations: Boolean,
    val notificationPermissionGranted: Boolean,
    val areChannelsEnabled: Boolean,
    val channelsStatus: Map<String, Boolean>,
    val manufacturer: String,
    val manufacturerWarning: String?,
    val isFullyReliable: Boolean
)

@Singleton
class ReliabilityChecker @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    fun check(): ReliabilityReport {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager

        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager?.canScheduleExactAlarms() == true
        } else {
            true
        }

        val isIgnoringBattery = powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: true

        val notificationManager = NotificationManagerCompat.from(context)
        val notificationsEnabled = notificationManager.areNotificationsEnabled()

        val channelsStatus = NotificationChannels.checkChannelsHealth(context)
        val areChannelsEnabled = channelsStatus.values.all { it }

        val manufacturer = Build.MANUFACTURER.lowercase()
        val warning = getOemInstructions(manufacturer)

        val isFullyReliable = canScheduleExact && isIgnoringBattery && notificationsEnabled && areChannelsEnabled

        return ReliabilityReport(
            canScheduleExactAlarms = canScheduleExact,
            isIgnoringBatteryOptimizations = isIgnoringBattery,
            notificationPermissionGranted = notificationsEnabled,
            areChannelsEnabled = areChannelsEnabled,
            channelsStatus = channelsStatus,
            manufacturer = Build.MANUFACTURER,
            manufacturerWarning = warning,
            isFullyReliable = isFullyReliable
        )
    }

    fun getExactAlarmIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(
                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                Uri.parse("package:${context.packageName}")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } else {
            Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
    }

    fun getIgnoreBatteryOptimizationIntent(): Intent {
        return Intent(
            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Uri.parse("package:${context.packageName}")
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun getAppNotificationSettingsIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } else {
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:${context.packageName}")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
    }

    fun getOemAutoStartIntents(): List<Intent> {
        val intents = mutableListOf<Intent>()
        val pkg = context.packageName

        // Xiaomi / POCO / Redmi
        intents.add(
            Intent().setComponent(
                ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
            )
        )
        // Oppo / Realme / OnePlus
        intents.add(
            Intent().setComponent(
                ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")
            )
        )
        intents.add(
            Intent().setComponent(
                ComponentName("com.oplus.battery", "com.oplus.battery.BatteryMainActivity")
            )
        )
        // Vivo / iQOO
        intents.add(
            Intent().setComponent(
                ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")
            )
        )
        intents.add(
            Intent().setComponent(
                ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")
            )
        )
        // Huawei / Honor
        intents.add(
            Intent().setComponent(
                ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")
            )
        )
        intents.add(
            Intent().setComponent(
                ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")
            )
        )

        return intents.map { it.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK } }
    }

    private fun getOemInstructions(manufacturer: String): String? {
        return when {
            manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco") ->
                "Xiaomi MIUI / HyperOS aggressively terminates background apps. Please: 1) Enable 'Autostart' in Security, 2) Set Battery Saver to 'No restrictions', 3) Lock Cooked in Recent Apps."

            manufacturer.contains("samsung") ->
                "Samsung One UI puts background apps to sleep. Please: 1) Set Battery to 'Unrestricted', 2) Add Cooked to 'Never sleeping apps', 3) Turn off 'Put unused apps to sleep'."

            manufacturer.contains("oppo") || manufacturer.contains("oneplus") || manufacturer.contains("realme") ->
                "OxygenOS / ColorOS kills background alarms to save battery. Please: 1) Enable 'Allow background activity', 2) Enable 'Auto-launch', 3) Turn off 'Optimise battery use'."

            manufacturer.contains("vivo") || manufacturer.contains("iqoo") ->
                "Vivo FuntouchOS kills reminder alarms. Please: 1) Allow 'High background power consumption', 2) Enable 'Autostart' in iManager, 3) Lock Cooked in recent tasks."

            manufacturer.contains("huawei") || manufacturer.contains("honor") ->
                "Huawei EMUI stops background alarms. Please: 1) Set Cooked to 'Manage manually' in App Launch settings, 2) Enable Auto-launch and Run in background."

            else -> null
        }
    }
}
