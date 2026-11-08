package com.cooked.app.feature.detail

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDetailScreen(
    uiState: ReminderDetailUiState,
    onNavigateBack: () -> Unit,
    onProgressChanged: (Float) -> Unit,
    onEffortSelected: (Int) -> Unit,
    onToggleChecklistStep: (String) -> Unit,
    onAddChecklistStep: (String) -> Unit,
    onDeleteChecklistStep: (String) -> Unit,
    onPushDeadline: (Instant, String) -> Unit,
    onShowRescheduleSheet: (Boolean) -> Unit,
    onMarkDone: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onNavigateBack()
        }
    }

    if (uiState.isLoading || uiState.item == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = CookedTheme.colors.primary)
        }
        return
    }

    val item = uiState.item
    val heat = uiState.heatResult ?: return
    val zone = try {
        ZoneId.of(item.timeZoneId)
    } catch (_: Exception) {
        ZoneId.systemDefault()
    }
    val formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy • h:mm a").withZone(zone)
    val startByFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM • h:mm a").withZone(zone)

    var newStepText by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CookedTheme.colors.background,
        topBar = {
            CookedTopBar(
                title = item.title,
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
                    IconButton(onClick = onDelete) {
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
            // Hero Countdown & Heat Status
            item(key = "hero_countdown") {
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
                                text = item.emoji ?: "🎯",
                                fontSize = 32.sp
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

                        Text(
                            text = "Due: ${formatter.format(item.dueAt)}",
                            style = CookedTheme.typography.bodyMedium,
                            color = CookedTheme.colors.onSurfaceVariant
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

            // Progress Slider Section (Snaps to 10%)
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
                            steps = 9, // 10 steps (0, 10, 20... 100)
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
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CookedTheme.colors.primary,
                                        selectedLabelColor = CookedTheme.colors.onPrimary,
                                        containerColor = CookedTheme.colors.surfaceContainerHigh,
                                        labelColor = CookedTheme.colors.onSurface
                                    )
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

            // Bottom Completion Button
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
