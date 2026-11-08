package com.cooked.app.feature.settings.reliability

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cooked.app.R
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.components.CookedCard
import com.cooked.app.core.designsystem.components.CookedTopBar
import com.cooked.app.scheduling.battery.ReliabilityChecker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReliabilityCheckRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReliabilityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Auto refresh when returning from system settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshChecks()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    ReliabilityCheckScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onScheduleTest = viewModel::schedule60SecondTest,
        onRefresh = viewModel::refreshChecks,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReliabilityCheckScreen(
    uiState: ReliabilityUiState,
    onNavigateBack: () -> Unit,
    onScheduleTest: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val checker = rememberChecker(context)
    val report = uiState.report

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CookedTheme.colors.background,
        topBar = {
            CookedTopBar(
                title = "Will Cooked actually yap?",
                onNavigationClick = onNavigateBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. Health Summary Banner
            item {
                val isSafe = report?.isFullyReliable == true
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSafe) CookedTheme.colors.surfaceContainerHigh else CookedTheme.colors.primaryContainer,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSafe) CookedTheme.colors.outlineVariant else CookedTheme.colors.primary.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(if (isSafe) R.drawable.ic_cat_bell else R.drawable.ic_emergency_siren),
                            contentDescription = null,
                            tint = CookedTheme.colors.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isSafe) "All clear. Cooked will yap on time." else "Reminders might stay silent.",
                                style = CookedTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isSafe) CookedTheme.colors.onSurface else CookedTheme.colors.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isSafe)
                                    "Your permissions and battery settings allow alarms to wake your phone."
                                else
                                    "Aggressive OEM battery optimizers may delay or suppress your reminders.",
                                style = CookedTheme.typography.bodySmall,
                                color = if (isSafe) CookedTheme.colors.onSurfaceVariant else CookedTheme.colors.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // 2. Core Checklist
            item {
                CookedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "SYSTEM PERMISSIONS",
                            style = CookedTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = CookedTheme.colors.primary
                        )

                        // Row 1: Notifications
                        ChecklistRow(
                            title = "Notifications enabled",
                            subtitle = "Allows Cooked to post alert cards and nag banners.",
                            isPassing = report?.notificationPermissionGranted == true,
                            onFix = { launchIntentSafely(context, checker.getAppNotificationSettingsIntent()) }
                        )

                        // Row 2: Exact Alarms
                        ChecklistRow(
                            title = "Exact alarms permitted",
                            subtitle = "Fires alarms to the exact second rather than batched delays.",
                            isPassing = report?.canScheduleExactAlarms == true,
                            onFix = { launchIntentSafely(context, checker.getExactAlarmIntent()) }
                        )

                        // Row 3: Battery Optimization
                        ChecklistRow(
                            title = "Battery unrestricted",
                            subtitle = "Stops Android from killing Cooked while your screen is locked.",
                            isPassing = report?.isIgnoringBatteryOptimizations == true,
                            onFix = { launchIntentSafely(context, checker.getIgnoreBatteryOptimizationIntent()) }
                        )

                        // Row 4: Notification Channels
                        ChecklistRow(
                            title = "Notification channels active",
                            subtitle = "Loud and critical alarm sound channels are enabled.",
                            isPassing = report?.areChannelsEnabled == true,
                            onFix = { launchIntentSafely(context, checker.getAppNotificationSettingsIntent()) }
                        )

                        // Row 5: Do Not Disturb override (optional)
                        ChecklistRow(
                            title = "Do Not Disturb access (optional)",
                            subtitle = "Enables critical alarms to bypass system silent mode.",
                            isPassing = report?.dndOverrideGranted == true,
                            actionLabel = if (report?.dndOverrideGranted == true) "Allowed" else "Grant",
                            onFix = { launchIntentSafely(context, checker.getDndSettingsIntent()) }
                        )
                    }
                }
            }

            // 3. OEM Specific Guide Card
            item {
                val manufacturer = report?.manufacturer ?: "Android"
                CookedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DEVICE-SPECIFIC FIXES",
                                style = CookedTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = CookedTheme.colors.primary
                            )
                            Text(
                                text = manufacturer.uppercase(),
                                style = CookedTheme.typography.labelSmall,
                                color = CookedTheme.colors.onSurfaceVariant
                            )
                        }

                        val guideText = report?.manufacturerWarning ?: (
                            "Generic Android detected: Ensure Battery Saver does not restrict background activity " +
                            "for Cooked in Settings > Apps."
                        )

                        Text(
                            text = guideText,
                            style = CookedTheme.typography.bodyMedium,
                            color = CookedTheme.colors.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        FilledTonalButton(
                            onClick = {
                                val oemIntents = checker.getOemAutoStartIntents()
                                var launched = false
                                for (intent in oemIntents) {
                                    try {
                                        context.startActivity(intent)
                                        launched = true
                                        break
                                    } catch (_: Exception) {}
                                }
                                if (!launched) {
                                    launchIntentSafely(context, checker.getAppDetailsIntent())
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Open ${manufacturer.replaceFirstChar { it.uppercase() }} Settings")
                        }
                    }
                }
            }

            // 4. "Test it" 60-Second Real Alarm
            item {
                CookedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "REAL-WORLD ALARM TEST",
                            style = CookedTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = CookedTheme.colors.primary
                        )

                        Text(
                            text = "Schedule a real alarm for 60 seconds from now. Lock your screen and see if Cooked wakes your phone properly.",
                            style = CookedTheme.typography.bodyMedium,
                            color = CookedTheme.colors.onSurfaceVariant
                        )

                        if (uiState.testResultMessage != null) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = CookedTheme.colors.surfaceContainerHigh
                            ) {
                                Text(
                                    text = uiState.testResultMessage,
                                    style = CookedTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = CookedTheme.colors.onSurface,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        Button(
                            onClick = onScheduleTest,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            enabled = !uiState.isTestingAlarm,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CookedTheme.colors.primary,
                                contentColor = CookedTheme.colors.onPrimary
                            )
                        ) {
                            Text(
                                text = if (uiState.isTestingAlarm) "Alarm scheduled in 60s (Lock your phone now!)" else "Test it in 60s"
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun ChecklistRow(
    title: String,
    subtitle: String,
    isPassing: Boolean,
    actionLabel: String = "Fix",
    onFix: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    if (isPassing) CookedTheme.colors.surfaceContainerHigh else CookedTheme.colors.primaryContainer,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPassing) Icons.Rounded.Check else Icons.Rounded.Warning,
                contentDescription = null,
                tint = if (isPassing) CookedTheme.colors.primary else CookedTheme.colors.primary,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = CookedTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = CookedTheme.colors.onSurface
            )
            Text(
                text = subtitle,
                style = CookedTheme.typography.bodySmall,
                color = CookedTheme.colors.onSurfaceVariant
            )
        }

        if (!isPassing) {
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick = onFix,
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(text = actionLabel, fontSize = 12.sp)
            }
        }
    }
}

private fun launchIntentSafely(context: Context, intent: Intent) {
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        val appDetails = Intent(
            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            android.net.Uri.parse("package:${context.packageName}")
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(appDetails)
        } catch (_: Exception) {}
    }
}

@Composable
private fun rememberChecker(context: Context): ReliabilityChecker {
    return androidx.compose.runtime.remember {
        ReliabilityChecker(context.applicationContext)
    }
}
