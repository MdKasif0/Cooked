package com.cooked.app.feature.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.EditCalendar
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import com.cooked.app.core.designsystem.components.ShimmerPlaceholder
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cooked.app.R
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.HeatLevel
import com.cooked.app.core.designsystem.components.CookedCard
import com.cooked.app.core.designsystem.components.CookedTopBar
import com.cooked.app.core.designsystem.components.CountdownText
import com.cooked.app.core.designsystem.components.HeatBar
import com.cooked.app.core.designsystem.components.HeatChip
import com.cooked.app.core.designsystem.components.SectionHeader
import com.cooked.app.domain.model.ItemEvent
import com.cooked.app.domain.model.ItemEventType
import com.cooked.app.domain.model.ItemKind
import com.cooked.app.domain.model.Recurrence
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDetailScreen(
    uiState: ReminderDetailUiState,
    onNavigateBack: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onEmojiSelected: (String?) -> Unit,
    onDueAtChanged: (Instant) -> Unit,
    onRecurrenceChanged: (Recurrence?) -> Unit,
    onNagIntervalChanged: (Int) -> Unit,
    onEscalateToggle: (Boolean) -> Unit,
    onCriticalToggle: (Boolean) -> Unit,
    onNoteChanged: (String) -> Unit,
    onProgressChanged: (Float) -> Unit,
    onEffortSelected: (Int) -> Unit,
    onToggleChecklistStep: (String) -> Unit,
    onAddChecklistStep: (String) -> Unit,
    onDeleteChecklistStep: (String) -> Unit,
    onPushDeadline: (Instant, String) -> Unit,
    onShowRescheduleSheet: (Boolean) -> Unit,
    onShowEmojiPicker: (Boolean) -> Unit,
    onShowDatePicker: (Boolean) -> Unit,
    onMarkDone: () -> Unit,
    onDelete: () -> Unit,
    onStartCookSession: (() -> Unit)? = null,
    onTriggerRescuePlan: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onNavigateBack()
        }
    }

    if (uiState.isLoading || uiState.item == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = CookedTheme.spacing.screenHorizontal, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(0.7f).height(36.dp))
            ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(64.dp))
            Spacer(modifier = Modifier.height(12.dp))
            repeat(4) {
                ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(72.dp))
            }
        }
        return
    }

    val item = uiState.item
    val heat = uiState.heatResult
    val zone = try {
        ZoneId.of(item.timeZoneId)
    } catch (_: Exception) {
        ZoneId.systemDefault()
    }
    val formatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy • h:mm a").withZone(zone)
    val startByFormatter = DateTimeFormatter.ofPattern("EEE, d MMM • h:mm a").withZone(zone)
    val timeOnlyFormatter = DateTimeFormatter.ofPattern("d MMM, h:mm a").withZone(zone)

    var newStepText by remember { mutableStateOf("") }
    var titleInput by remember(item.title) { mutableStateOf(item.title) }
    var noteInput by remember(item.note) { mutableStateOf(item.note ?: "") }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val emojis = listOf("🎯", "📚", "💻", "💼", "⚡", "🔔", "💊", "🛒", "🏋️", "🧹", "🎨", "📝", "🚗", "✈️", "🍳", "🔥")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CookedTheme.colors.background,
        topBar = {
            CookedTopBar(
                title = if (item.kind == ItemKind.DEADLINE) "Deadline Details" else "Reminder Details",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = CookedTheme.colors.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirmDialog = true }) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Delete",
                            tint = CookedTheme.colors.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = CookedTheme.spacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp)
        ) {
            // 1. Inline Editable Title & Emoji
            item(key = "title_emoji_card") {
                CookedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Emoji trigger button
                            Surface(
                                shape = CircleShape,
                                color = CookedTheme.colors.surfaceContainerHigh,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clickable { onShowEmojiPicker(true) }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = item.emoji ?: "🎯",
                                        fontSize = 26.sp
                                    )
                                }
                            }

                            // Inline Editable Title
                            OutlinedTextField(
                                value = titleInput,
                                onValueChange = {
                                    titleInput = it
                                    onTitleChanged(it)
                                },
                                singleLine = false,
                                maxLines = 3,
                                textStyle = CookedTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CookedTheme.colors.onSurface
                                ),
                                placeholder = { Text("Task title...") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CookedTheme.colors.primary,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = CookedTheme.colors.surfaceContainerLow,
                                    unfocusedContainerColor = CookedTheme.colors.surfaceContainerLow
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Emoji Picker Row (if expanded)
                        AnimatedVisibility(visible = uiState.showEmojiPicker) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Choose Emoji Icon:",
                                    style = CookedTheme.typography.labelSmall,
                                    color = CookedTheme.colors.onSurfaceVariant
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    emojis.forEach { emoji ->
                                        Surface(
                                            shape = CircleShape,
                                            color = if (item.emoji == emoji) CookedTheme.colors.primaryContainer else CookedTheme.colors.surfaceContainerLow,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clickable { onEmojiSelected(emoji) }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(text = emoji, fontSize = 20.sp)
                                            }
                                        }
                                    }
                                    // Option to remove emoji
                                    TextButton(onClick = { onEmojiSelected(null) }) {
                                        Text("None", color = CookedTheme.colors.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Deadline Countdown & HeatBar (If Deadline Mode)
            if (item.kind == ItemKind.DEADLINE && heat != null) {
                item(key = "deadline_heat_hero") {
                    CookedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Deadline Urgency",
                                    style = CookedTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = CookedTheme.colors.onSurface
                                )
                                HeatChip(level = heat.heatLevel)
                            }

                            CountdownText(
                                targetInstant = item.dueAt,
                                style = CookedTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = CookedTheme.colors.heatFor(heat.heatLevel).color
                                )
                            )

                            HeatBar(
                                progress = heat.heatProgress,
                                level = heat.heatLevel,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = CookedTheme.colors.surfaceContainerLow,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = heat.reason,
                                    style = CookedTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CookedTheme.colors.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // 3. Due Date & Time Picker Row
            item(key = "due_date_time") {
                SectionHeader(title = "Due Date & Time")
                CookedCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = null,
                                tint = CookedTheme.colors.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = formatter.format(item.dueAt),
                                    style = CookedTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = CookedTheme.colors.onSurface
                                )
                                if (item.allDay) {
                                    Text(
                                        text = "All day item",
                                        style = CookedTheme.typography.bodySmall,
                                        color = CookedTheme.colors.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        FilledTonalButton(
                            onClick = { onShowDatePicker(true) },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.EditCalendar,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Change")
                        }
                    }
                }
            }

            // 4. Repeat (Recurrence) Chips
            item(key = "recurrence_chips") {
                SectionHeader(title = "Repeat")
                CookedCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isNone = item.recurrence == null
                        val isDaily = item.recurrence == Recurrence.Daily
                        val isWeekdays = item.recurrence == Recurrence.Weekdays
                        val isWeekly = item.recurrence is Recurrence.Weekly
                        val isMonthly = item.recurrence is Recurrence.Monthly

                        FilterChip(
                            selected = isNone,
                            onClick = { onRecurrenceChanged(null) },
                            label = { Text("None") },
                            colors = recurrenceChipColors()
                        )
                        FilterChip(
                            selected = isDaily,
                            onClick = { onRecurrenceChanged(Recurrence.Daily) },
                            label = { Text("Daily") },
                            colors = recurrenceChipColors()
                        )
                        FilterChip(
                            selected = isWeekdays,
                            onClick = { onRecurrenceChanged(Recurrence.Weekdays) },
                            label = { Text("Weekdays") },
                            colors = recurrenceChipColors()
                        )
                        FilterChip(
                            selected = isWeekly,
                            onClick = { onRecurrenceChanged(Recurrence.Weekly(setOf(1))) },
                            label = { Text("Weekly") },
                            colors = recurrenceChipColors()
                        )
                        FilterChip(
                            selected = isMonthly,
                            onClick = { onRecurrenceChanged(Recurrence.Monthly(1)) },
                            label = { Text("Monthly") },
                            colors = recurrenceChipColors()
                        )
                    }
                }
            }

            // 5. Nag Policy Settings
            item(key = "nag_policy") {
                SectionHeader(title = "Nag Policy")
                CookedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Interval chips: 5, 10, 15, 30, 60m
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Nag Interval",
                                style = CookedTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = CookedTheme.colors.onSurface
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(5, 10, 15, 30, 60).forEach { mins ->
                                    val selected = item.nag.intervalMinutes == mins
                                    FilterChip(
                                        selected = selected,
                                        onClick = { onNagIntervalChanged(mins) },
                                        label = { Text("${mins}m", fontSize = 12.sp) },
                                        colors = recurrenceChipColors()
                                    )
                                }
                            }
                        }

                        // Escalate toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Escalate Repeated Nags",
                                    style = CookedTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = CookedTheme.colors.onSurface
                                )
                                Text(
                                    text = "Shorten interval & ramp urgency if ignored",
                                    style = CookedTheme.typography.bodySmall,
                                    color = CookedTheme.colors.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = item.nag.escalate,
                                onCheckedChange = onEscalateToggle,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CookedTheme.colors.primary,
                                    checkedTrackColor = CookedTheme.colors.primaryContainer
                                )
                            )
                        }

                        // Critical Urgent Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Urgent !! (Critical)",
                                    style = CookedTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = CookedTheme.colors.onSurface
                                )
                                Text(
                                    text = "High priority alarm, can break through quiet hours",
                                    style = CookedTheme.typography.bodySmall,
                                    color = CookedTheme.colors.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = item.critical,
                                onCheckedChange = onCriticalToggle,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CookedTheme.colors.heatFor(HeatLevel.COOKED).color,
                                    checkedTrackColor = CookedTheme.colors.heatFor(HeatLevel.COOKED).color.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }
                }
            }

            // 6. Note Section
            item(key = "note_section") {
                SectionHeader(title = "Notes & Context")
                CookedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        OutlinedTextField(
                            value = noteInput,
                            onValueChange = {
                                noteInput = it
                                onNoteChanged(it)
                            },
                            placeholder = { Text("Add any notes, links, or context...") },
                            minLines = 3,
                            maxLines = 6,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CookedTheme.colors.primary,
                                unfocusedBorderColor = CookedTheme.colors.outline,
                                focusedContainerColor = CookedTheme.colors.surfaceContainerLow,
                                unfocusedContainerColor = CookedTheme.colors.surfaceContainerLow
                            )
                        )
                    }
                }
            }

            // 7. Deadline Specific Sections (Progress, Effort, Checklist, Push It)
            if (item.kind == ItemKind.DEADLINE) {
                // Progress Slider Section
                item(key = "progress_slider") {
                    SectionHeader(title = "Progress (${item.progress}%)")
                    CookedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Completion",
                                    style = CookedTheme.typography.titleMedium,
                                    color = CookedTheme.colors.onSurface
                                )
                                Text(
                                    text = "${item.progress}%",
                                    style = CookedTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CookedTheme.colors.primary
                                )
                            }

                            Slider(
                                value = item.progress.toFloat(),
                                onValueChange = onProgressChanged,
                                valueRange = 0f..100f,
                                steps = 9,
                                colors = SliderDefaults.colors(
                                    thumbColor = CookedTheme.colors.primary,
                                    activeTrackColor = CookedTheme.colors.primary,
                                    inactiveTrackColor = CookedTheme.colors.surfaceContainerHigh
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Estimated Effort Picker
                item(key = "effort_picker") {
                    SectionHeader(title = "Estimated Effort")
                    CookedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "How long will this task take to complete?",
                                style = CookedTheme.typography.bodyMedium,
                                color = CookedTheme.colors.onSurfaceVariant
                            )

                            val presetEfforts = listOf(
                                15 to "15m",
                                30 to "30m",
                                60 to "1h",
                                120 to "2h",
                                240 to "4h",
                                480 to "8h"
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                presetEfforts.forEach { (mins, label) ->
                                    val selected = item.effortMinutes == mins
                                    FilterChip(
                                        selected = selected,
                                        onClick = { onEffortSelected(mins) },
                                        label = { Text(label, fontSize = 12.sp) },
                                        colors = recurrenceChipColors()
                                    )
                                }
                            }
                        }
                    }
                }

                // Start-by & Reschedule Section
                item(key = "start_by_reschedule") {
                    SectionHeader(title = "Schedule & Start-By")
                    CookedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val effectiveStartBy = item.startBy ?: uiState.suggestedStartBy
                            if (effectiveStartBy != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.CalendarMonth,
                                            contentDescription = null,
                                            tint = CookedTheme.colors.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Suggested Start Date",
                                                style = CookedTheme.typography.labelSmall,
                                                color = CookedTheme.colors.onSurfaceVariant
                                            )
                                            Text(
                                                text = startByFormatter.format(effectiveStartBy),
                                                style = CookedTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = CookedTheme.colors.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            OutlinedButton(
                                onClick = { onShowRescheduleSheet(true) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.EditCalendar,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Push It (Reschedule)")
                            }
                        }
                    }
                }

                // Checklist Steps Section
                item(key = "checklist_header") {
                    SectionHeader(title = "Checklist Steps (${item.checklist.count { it.done }}/${item.checklist.size})")
                }

                items(
                    items = item.checklist,
                    key = { it.id }
                ) { step ->
                    CookedCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = step.done,
                                onCheckedChange = { onToggleChecklistStep(step.id) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = CookedTheme.colors.primary,
                                    checkmarkColor = CookedTheme.colors.onPrimary
                                )
                            )
                            Text(
                                text = step.text,
                                style = CookedTheme.typography.bodyMedium,
                                fontWeight = if (step.done) FontWeight.Normal else FontWeight.Medium,
                                color = if (step.done) CookedTheme.colors.onSurfaceVariant else CookedTheme.colors.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { onDeleteChecklistStep(step.id) }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Delete step",
                                    tint = CookedTheme.colors.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Add Step Input
                item(key = "add_step_input") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newStepText,
                            onValueChange = { newStepText = it },
                            placeholder = { Text("Add next micro-step...", fontSize = 14.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (newStepText.isNotBlank()) {
                                        onAddChecklistStep(newStepText)
                                        newStepText = ""
                                    }
                                }
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        FilledTonalButton(
                            onClick = {
                                if (newStepText.isNotBlank()) {
                                    onAddChecklistStep(newStepText)
                                    newStepText = ""
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(52.dp)
                        ) {
                            Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add")
                        }
                    }
                }
            }

            // 8. History Timeline Section (from ItemEvents)
            item(key = "history_header") {
                SectionHeader(title = "History Timeline (${uiState.events.size})")
            }

            if (uiState.events.isEmpty()) {
                item(key = "history_empty") {
                    CookedCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.History,
                                contentDescription = null,
                                tint = CookedTheme.colors.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Created on ${timeOnlyFormatter.format(item.createdAt)}",
                                style = CookedTheme.typography.bodyMedium,
                                color = CookedTheme.colors.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(
                    items = uiState.events,
                    key = { it.id }
                ) { event ->
                    TimelineEventRow(event = event, zone = zone)
                }
            }

            // 9. Bottom Completion Button
            item(key = "bottom_done_button") {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        onMarkDone()
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CookedTheme.colors.primary,
                        contentColor = CookedTheme.colors.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MARK DONE (I SURVIVED)",
                        style = CookedTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Material 3 Date Picker Dialog
    if (uiState.showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { onShowDatePicker(false) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val pickedDate = Instant.ofEpochMilli(selectedMillis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                            val originalZdt = item.dueAt.atZone(zone)
                            val newInstant = pickedDate.atTime(originalZdt.toLocalTime()).atZone(zone).toInstant()
                            onDueAtChanged(newInstant)
                        }
                        onShowDatePicker(false)
                    }
                ) {
                    Text("OK", color = CookedTheme.colors.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { onShowDatePicker(false) }) {
                    Text("Cancel", color = CookedTheme.colors.onSurfaceVariant)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete this item?") },
            text = { Text("This will remove the item from your reminders. You can undo this right after deleting.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = CookedTheme.colors.heatFor(HeatLevel.COOKED).color, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = CookedTheme.colors.onSurface)
                }
            }
        )
    }

    // "Push It" Reschedule Honest Dialog
    if (uiState.showRescheduleSheet) {
        AlertDialog(
            onDismissRequest = { onShowRescheduleSheet(false) },
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Push it: New date, or new plan?",
                    style = CookedTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Be real with yourself. Moving a deadline without cutting scope is how we get cooked.",
                        style = CookedTheme.typography.bodyMedium,
                        color = CookedTheme.colors.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val quickPushes = listOf(
                        "+1 Day" to Duration.ofDays(1),
                        "+3 Days" to Duration.ofDays(3),
                        "+1 Week" to Duration.ofDays(7)
                    )

                    quickPushes.forEach { (label, duration) ->
                        OutlinedButton(
                            onClick = {
                                val newDueAt = item.dueAt.plus(duration)
                                onPushDeadline(newDueAt, "User postponed by $label")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Postpone $label")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { onShowRescheduleSheet(false) }) {
                    Text("Nevermind, Keep Grindin'")
                }
            }
        )
    }
}

@Composable
private fun TimelineEventRow(
    event: ItemEvent,
    zone: ZoneId,
    modifier: Modifier = Modifier
) {
    val timeFormatter = DateTimeFormatter.ofPattern("EEE d MMM • h:mm a").withZone(zone)
    val (label, icon, color) = when (event.type) {
        ItemEventType.CREATED -> Triple("Created", Icons.Rounded.CalendarMonth, CookedTheme.colors.onSurface)
        ItemEventType.EDITED -> Triple("Settings updated", Icons.Rounded.EditCalendar, CookedTheme.colors.onSurfaceVariant)
        ItemEventType.FIRED -> Triple("First alert fired", Icons.Rounded.NotificationsActive, CookedTheme.colors.primary)
        ItemEventType.NAGGED -> {
            val count = event.meta["nagCount"] ?: "1"
            Triple("Nagged #$count", Icons.Rounded.NotificationsActive, CookedTheme.colors.heatFor(HeatLevel.WARM).color)
        }
        ItemEventType.SNOOZED -> {
            val mins = event.meta["snoozeMinutes"] ?: "10"
            Triple("Snoozed for ${mins}m", Icons.Rounded.Schedule, CookedTheme.colors.heatFor(HeatLevel.WARM).color)
        }
        ItemEventType.DONE -> {
            val lateMins = event.meta["lateByMinutes"]?.toIntOrNull() ?: 0
            val text = if (lateMins > 0) "Completed ${lateMins}m late" else "Completed on time"
            Triple(text, Icons.Rounded.Check, CookedTheme.colors.heatFor(HeatLevel.CHILL).color)
        }
        ItemEventType.UNDONE -> Triple("Marked not done", Icons.Rounded.Close, CookedTheme.colors.onSurfaceVariant)
        ItemEventType.RESCHEDULED -> {
            val reason = event.meta["reason"] ?: "Postponed"
            Triple("Rescheduled ($reason)", Icons.Rounded.EditCalendar, CookedTheme.colors.heatFor(HeatLevel.WARM).color)
        }
        ItemEventType.MISSED -> Triple("Alarm missed", Icons.Rounded.Close, CookedTheme.colors.heatFor(HeatLevel.COOKED).color)
        ItemEventType.STARTED -> Triple("Started working", Icons.Rounded.Whatshot, CookedTheme.colors.primary)
    }

    CookedCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = CookedTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = CookedTheme.colors.onSurface
                )
                Text(
                    text = timeFormatter.format(event.at),
                    style = CookedTheme.typography.bodySmall,
                    color = CookedTheme.colors.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun recurrenceChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = CookedTheme.colors.primary,
    selectedLabelColor = CookedTheme.colors.onPrimary,
    containerColor = CookedTheme.colors.surfaceContainerHigh,
    labelColor = CookedTheme.colors.onSurface
)
