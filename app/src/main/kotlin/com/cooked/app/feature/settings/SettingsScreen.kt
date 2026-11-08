package com.cooked.app.feature.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.ThemeMode
import com.cooked.app.core.designsystem.components.CookedCard
import com.cooked.app.core.designsystem.components.CookedSwitch
import com.cooked.app.core.designsystem.components.CookedTopBar
import com.cooked.app.core.designsystem.components.SectionHeader
import com.cooked.app.core.designsystem.components.SegmentedControl
import com.cooked.app.domain.model.Personality
import com.cooked.app.feature.settings.components.PersonalityPreviewCard
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.DayOfWeek
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onNavigateBack: () -> Unit,
    onNavigateToReliability: () -> Unit,
    onNavigateToPaywall: () -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onDynamicColorToggled: (Boolean) -> Unit,
    onNagIntervalSelected: (Int) -> Unit,
    onPersonalitySelected: (Personality) -> Unit,
    onDailyCapacityChanged: (Int) -> Unit,
    onWeekStartsOnChanged: (DayOfWeek) -> Unit,
    onExportBackup: ((String) -> Unit) -> Unit,
    onImportContent: (String) -> Unit,
    onConfirmImport: () -> Unit,
    onDismissImportPreview: () -> Unit,
    onRequestDeleteAll: () -> Unit,
    onConfirmFirstDelete: () -> Unit,
    onConfirmFinalDelete: () -> Unit,
    onDismissDeleteDialogs: () -> Unit,
    onSetPrivacyVisible: (Boolean) -> Unit,
    onClearSnackbar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingExportJson by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null && pendingExportJson != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(pendingExportJson!!.toByteArray())
                }
            } catch (_: Exception) {}
            pendingExportJson = null
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val content = reader.readText()
                    onImportContent(content)
                }
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearSnackbar()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CookedTheme.colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CookedTopBar(
                title = "Settings",
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

            // 1. RELIABILITY HERO CARD ("Will Cooked actually yap?")
            item {
                val report = uiState.reliabilityReport
                val isSafe = report?.isFullyReliable == true

                CookedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToReliability() }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (isSafe) CookedTheme.colors.surfaceContainerHigh else CookedTheme.colors.primaryContainer,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = if (isSafe) "🛡️" else "🚨", fontSize = 20.sp)
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Will Cooked actually yap?",
                                style = CookedTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = CookedTheme.colors.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isSafe) "All permission checks passing." else "Fix background battery settings.",
                                style = CookedTheme.typography.bodySmall,
                                color = if (isSafe) CookedTheme.colors.primary else CookedTheme.colors.primary
                            )
                        }

                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = CookedTheme.colors.onSurfaceVariant
                        )
                    }
                }
            }

            // 2. APPEARANCE
            item {
                SectionHeader(title = "Appearance", subtitle = "Light and dark themes tuned for high contrast.")
                CookedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Theme",
                            style = CookedTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = CookedTheme.colors.onSurface
                        )

                        SegmentedControl(
                            items = listOf("System", "Light", "Dark"),
                            selectedIndex = when (uiState.themeMode) {
                                ThemeMode.SYSTEM -> 0
                                ThemeMode.LIGHT -> 1
                                ThemeMode.DARK -> 2
                            },
                            onItemSelected = { index ->
                                val mode = when (index) {
                                    1 -> ThemeMode.LIGHT
                                    2 -> ThemeMode.DARK
                                    else -> ThemeMode.SYSTEM
                                }
                                onThemeModeSelected(mode)
                            }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Dynamic Color",
                                    style = CookedTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = CookedTheme.colors.onSurface
                                )
                                Text(
                                    text = "Derive subtle accents from your Android wallpaper (Material You).",
                                    style = CookedTheme.typography.bodySmall,
                                    color = CookedTheme.colors.onSurfaceVariant
                                )
                            }
                            CookedSwitch(
                                checked = uiState.dynamicColor,
                                onCheckedChange = onDynamicColorToggled
                            )
                        }
                    }
                }
            }

            // 3. NAGGING & URGENCY
            item {
                SectionHeader(title = "Nagging", subtitle = "Control how aggressively Cooked pesters you.")
                CookedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Default nag interval",
                            style = CookedTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = CookedTheme.colors.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(5, 10, 15, 30).forEach { minutes ->
                                val isSelected = uiState.defaultNagIntervalMinutes == minutes
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .clickable { onNagIntervalSelected(minutes) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) CookedTheme.colors.primary else CookedTheme.colors.surfaceContainerHigh,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) CookedTheme.colors.primary else CookedTheme.colors.outlineVariant
                                    )
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${minutes}m",
                                            style = CookedTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) CookedTheme.colors.onPrimary else CookedTheme.colors.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = "Quiet hours",
                            style = CookedTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = CookedTheme.colors.onSurface
                        )
                        Text(
                            text = "Non-critical reminders will defer nags between 10:00 PM and 8:00 AM.",
                            style = CookedTheme.typography.bodySmall,
                            color = CookedTheme.colors.onSurfaceVariant
                        )
                    }
                }
            }

            // 4. DEADLINE MODE & CAPACITY
            item {
                SectionHeader(title = "Deadlines", subtitle = "Effort thresholds and calculation constants.")
                CookedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Daily Capacity",
                                    style = CookedTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = CookedTheme.colors.onSurface
                                )
                                Text(
                                    text = "Estimated waking productive minutes per day for Heat calculation.",
                                    style = CookedTheme.typography.bodySmall,
                                    color = CookedTheme.colors.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${uiState.dailyCapacityMinutes}m",
                                style = CookedTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = CookedTheme.colors.primary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Week starts on",
                                style = CookedTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = CookedTheme.colors.onSurface
                            )

                            SegmentedControl(
                                items = listOf("Mon", "Sun"),
                                selectedIndex = if (uiState.weekStartsOn == DayOfWeek.MONDAY) 0 else 1,
                                onItemSelected = { idx ->
                                    onWeekStartsOnChanged(if (idx == 0) DayOfWeek.MONDAY else DayOfWeek.SUNDAY)
                                },
                                modifier = Modifier.width(130.dp)
                            )
                        }
                    }
                }
            }

            // 5. PERSONALITY & TONE
            item {
                SectionHeader(title = "Personality", subtitle = "Choose your preferred nagging tone.")
                CookedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Personality.values().forEach { personality ->
                                val isSelected = uiState.personality == personality
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .clickable { onPersonalitySelected(personality) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) CookedTheme.colors.primary else CookedTheme.colors.surfaceContainerHigh,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) CookedTheme.colors.primary else CookedTheme.colors.outlineVariant
                                    )
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = personality.name.lowercase().replaceFirstChar { it.uppercase() },
                                            style = CookedTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) CookedTheme.colors.onPrimary else CookedTheme.colors.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        PersonalityPreviewCard(
                            personality = uiState.personality,
                            sampleTitle = "Submit tax audit"
                        )
                    }
                }
            }

            // 6. NOTIFICATIONS SYSTEM SETTINGS
            item {
                SectionHeader(title = "System Notifications", subtitle = "Deep-links into Android channel settings.")
                CookedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Notifications, contentDescription = null, tint = CookedTheme.colors.primary)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Android Notification Settings",
                                style = CookedTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = CookedTheme.colors.onSurface
                            )
                            Text(
                                text = "Configure sounds, vibration patterns, and lock screen visibility.",
                                style = CookedTheme.typography.bodySmall,
                                color = CookedTheme.colors.onSurfaceVariant
                            )
                        }
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = CookedTheme.colors.onSurfaceVariant)
                    }
                }
            }

            // 7. BACKUP, EXPORT & CLEAR DATA
            item {
                SectionHeader(title = "Data & Backup", subtitle = "Offline-first. Everything lives on your device.")
                CookedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Export
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onExportBackup { json ->
                                        pendingExportJson = json
                                        exportLauncher.launch("cooked_backup_${System.currentTimeMillis()}.json")
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.FileUpload, contentDescription = null, tint = CookedTheme.colors.primary)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Export Backup (JSON)",
                                    style = CookedTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = CookedTheme.colors.onSurface
                                )
                                Text(
                                    text = "Export all tasks, deadlines, and history via Storage Access Framework.",
                                    style = CookedTheme.typography.bodySmall,
                                    color = CookedTheme.colors.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Import
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { importLauncher.launch(arrayOf("application/json")) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.FileDownload, contentDescription = null, tint = CookedTheme.colors.primary)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Import Backup (JSON)",
                                    style = CookedTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = CookedTheme.colors.onSurface
                                )
                                Text(
                                    text = "Restore items with conflict resolution (newer updates win).",
                                    style = CookedTheme.typography.bodySmall,
                                    color = CookedTheme.colors.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Delete all
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onRequestDeleteAll),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.DeleteForever, contentDescription = null, tint = CookedTheme.colors.primary)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Delete All Data",
                                    style = CookedTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = CookedTheme.colors.primary
                                )
                                Text(
                                    text = "Wipe all tasks, deadlines, snapshots, and history completely.",
                                    style = CookedTheme.typography.bodySmall,
                                    color = CookedTheme.colors.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 8. ABOUT & PRIVACY
            item {
                SectionHeader(title = "About Cooked", subtitle = "No login. No server. No tracking.")
                CookedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Version",
                                style = CookedTheme.typography.bodyMedium,
                                color = CookedTheme.colors.onSurface
                            )
                            Text(
                                text = uiState.appVersion,
                                style = CookedTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = CookedTheme.colors.onSurfaceVariant
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSetPrivacyVisible(true) },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Privacy Policy",
                                style = CookedTheme.typography.bodyMedium,
                                color = CookedTheme.colors.onSurface
                            )
                            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = CookedTheme.colors.onSurfaceVariant)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("market://details?id=${context.packageName}")
                                    )
                                    try { context.startActivity(intent) } catch (_: Exception) {}
                                },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Rate Cooked on Google Play",
                                style = CookedTheme.typography.bodyMedium,
                                color = CookedTheme.colors.onSurface
                            )
                            Text("⭐", fontSize = 16.sp)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:feedback@cooked.app")
                                        putExtra(Intent.EXTRA_SUBJECT, "Cooked Feedback - Android ${Build.VERSION.RELEASE} (${Build.MANUFACTURER} ${Build.MODEL})")
                                    }
                                    try { context.startActivity(intent) } catch (_: Exception) {}
                                },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Send Feedback",
                                style = CookedTheme.typography.bodyMedium,
                                color = CookedTheme.colors.onSurface
                            )
                            Text("💬", fontSize = 16.sp)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    // -------------------------------------------------------------------------
    // DIALOGS
    // -------------------------------------------------------------------------

    // 1. Import Preview Dialog
    if (uiState.importPreview != null) {
        val preview = uiState.importPreview
        AlertDialog(
            onDismissRequest = onDismissImportPreview,
            title = { Text("Import Backup?") },
            text = {
                Text(
                    text = "This backup contains ${preview.totalItems} items (${preview.deadlinesCount} deadlines, " +
                        "${preview.remindersCount} reminders).\n\nConflict rule: newer edits always overwrite older ones."
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirmImport,
                    colors = ButtonDefaults.buttonColors(containerColor = CookedTheme.colors.primary)
                ) {
                    Text("Import Items")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissImportPreview) {
                    Text("Cancel")
                }
            }
        )
    }

    // 2. Delete Confirm Dialog (Step 1)
    if (uiState.showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = onDismissDeleteDialogs,
            title = { Text("Delete all data?") },
            text = {
                Text("This will permanently remove all your active tasks, deadlines, and alarm history from this device.")
            },
            confirmButton = {
                Button(
                    onClick = onConfirmFirstDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = CookedTheme.colors.primary)
                ) {
                    Text("Yes, proceed")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeleteDialogs) {
                    Text("Cancel")
                }
            }
        )
    }

    // 3. Delete Double Confirm Dialog (Step 2)
    if (uiState.showDeleteDoubleConfirmDialog) {
        AlertDialog(
            onDismissRequest = onDismissDeleteDialogs,
            title = { Text("Are you 100% sure?") },
            text = {
                Text("There is no undo. All reminders and alarms will be wiped and canceled immediately.")
            },
            confirmButton = {
                Button(
                    onClick = onConfirmFinalDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = CookedTheme.colors.primary)
                ) {
                    Text("Permanently Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeleteDialogs) {
                    Text("Cancel")
                }
            }
        )
    }

    // 4. Privacy Policy Dialog
    if (uiState.showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { onSetPrivacyVisible(false) },
            title = { Text("Privacy Policy") },
            text = {
                Text(
                    text = "Cooked is completely offline-first.\n\n" +
                        "• No account is required.\n" +
                        "• All your reminders, deadlines, notes, and habits are stored locally on your device in a secure SQLite database.\n" +
                        "• Zero personal data is sent to external servers or third-party advertisers.\n" +
                        "• Crash logs and anonymous diagnostic events are optional and strictly non-identifiable."
                )
            },
            confirmButton = {
                TextButton(onClick = { onSetPrivacyVisible(false) }) {
                    Text("Understood")
                }
            }
        )
    }
}
