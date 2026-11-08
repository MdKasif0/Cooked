package com.cooked.app.feature.add

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EditCalendar
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import com.cooked.app.core.config.FeatureFlags
import com.cooked.app.domain.template.BuiltInTemplates
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.HeatLevel
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemKind
import com.cooked.app.domain.model.Recurrence
import com.cooked.app.domain.parse.Highlight
import com.cooked.app.domain.parse.HighlightType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuickAddBottomSheet(
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    viewModel: QuickAddViewModel,
    modifier: Modifier = Modifier,
    onShowSnackbar: ((message: String, onUndo: () -> Unit) -> Unit)? = null,
    onLaunchScan: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Micro-animation for Ember sizzle on save
    val sizzleScale = remember { Animatable(1f) }
    var showSizzleEffect by remember { mutableStateOf(false) }

    // Speech Recognizer launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                viewModel.onTextChanged(spokenText)
            }
        }
    }

    // Cache draft when dismissed
    DisposableEffect(Unit) {
        onDispose {
            viewModel.onDismiss()
        }
    }

    // Auto-focus text field instantly without animation delay
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Observe save events for feedback and dismissing
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            val savedItem = uiState.savedItem
            val formattedTime = if (savedItem != null) {
                val zdt = savedItem.dueAt.atZone(ZoneId.systemDefault())
                DateTimeFormatter.ofPattern("h:mm a").format(zdt)
            } else {
                "due time"
            }

            // Haptic & Ember sizzle
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            showSizzleEffect = true
            sizzleScale.animateTo(
                targetValue = 1.25f,
                animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
            )
            sizzleScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
            )

            val snackbarMessage = "Locked in. I'll yap at $formattedTime."
            onShowSnackbar?.invoke(snackbarMessage) {
                viewModel.undoLastSave()
            }

            delay(120)
            viewModel.reset()
            onDismissRequest()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = CookedTheme.colors.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Title and Voice Mic Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Add",
                    style = CookedTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = CookedTheme.colors.onSurface
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (FeatureFlags.isDatesheetScanEnabled && onLaunchScan != null) {
                        IconButton(onClick = onLaunchScan) {
                            Icon(
                                imageVector = Icons.Rounded.CameraAlt,
                                contentDescription = "Scan datesheet or syllabus",
                                tint = CookedTheme.colors.primary
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(
                                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                )
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your reminder or deadline...")
                            }
                            try {
                                speechLauncher.launch(intent)
                            } catch (_: Exception) {
                                // Fallback if no speech recognition service available
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Mic,
                            contentDescription = "Voice input",
                            tint = CookedTheme.colors.primary
                        )
                    }

                    IconButton(onClick = onDismissRequest) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = CookedTheme.colors.onSurfaceVariant
                        )
                    }
                }
            }

            // Growth Feature: Built-in Templates
            if (FeatureFlags.isTemplatesEnabled) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(BuiltInTemplates.all, key = { it.id }) { template ->
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.applyTemplate(template) },
                            label = { Text("${template.emoji} ${template.name}") },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = CookedTheme.colors.surfaceContainer,
                                labelColor = CookedTheme.colors.onSurface
                            )
                        )
                    }
                }
            }

            // Syntax Highlighting Visual Transformation
            val primaryColor = CookedTheme.colors.primary
            val chillColor = CookedTheme.colors.heatFor(HeatLevel.CHILL).color
            val warmColor = CookedTheme.colors.heatFor(HeatLevel.WARM).color
            val cookedColor = CookedTheme.colors.heatFor(HeatLevel.COOKED).color

            val visualTransformation = remember(
                uiState.parsedResult?.tokens,
                primaryColor,
                chillColor,
                warmColor,
                cookedColor
            ) {
                QuickAddVisualTransformation(
                    tokens = uiState.parsedResult?.tokens ?: emptyList(),
                    timeColor = primaryColor,
                    recurrenceColor = chillColor,
                    effortColor = warmColor,
                    criticalColor = cookedColor,
                    deadlineColor = primaryColor
                )
            }

            // Main Input Text Field
            OutlinedTextField(
                value = uiState.input,
                onValueChange = viewModel::onTextChanged,
                placeholder = {
                    Text(
                        text = "e.g. submit dbms assignment fri 5pm 3h !!",
                        color = CookedTheme.colors.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 15.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .scale(sizzleScale.value),
                shape = RoundedCornerShape(14.dp),
                visualTransformation = visualTransformation,
                singleLine = false,
                maxLines = 3,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (uiState.canSave) {
                            viewModel.save()
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CookedTheme.colors.primary,
                    unfocusedBorderColor = CookedTheme.colors.outline,
                    focusedContainerColor = CookedTheme.colors.surfaceContainerLow,
                    unfocusedContainerColor = CookedTheme.colors.surfaceContainerLow,
                    focusedTextColor = CookedTheme.colors.onSurface,
                    unfocusedTextColor = CookedTheme.colors.onSurface
                )
            )

            // Live Preview Row: "Fri 14 Nov, 5:00 pm • Deadline • ~3h • Daily • Urgent"
            val previewText = buildPreviewText(uiState)
            AnimatedVisibility(
                visible = previewText.isNotBlank(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CookedTheme.colors.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = previewText,
                        style = CookedTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = CookedTheme.colors.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            // Quick Action Chips Row (Scrollable horizontally)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Today chip
                FilterChip(
                    selected = false,
                    onClick = { viewModel.onQuickChipSelected(QuickTimeChip.TODAY) },
                    label = { Text("Today") },
                    colors = chipColors()
                )

                // Tonight chip
                FilterChip(
                    selected = false,
                    onClick = { viewModel.onQuickChipSelected(QuickTimeChip.TONIGHT) },
                    label = { Text("Tonight") },
                    colors = chipColors()
                )

                // Tomorrow chip
                FilterChip(
                    selected = false,
                    onClick = { viewModel.onQuickChipSelected(QuickTimeChip.TOMORROW) },
                    label = { Text("Tomorrow") },
                    colors = chipColors()
                )

                // +10m chip
                FilterChip(
                    selected = false,
                    onClick = { viewModel.onQuickChipSelected(QuickTimeChip.PLUS_10M) },
                    label = { Text("+10m") },
                    colors = chipColors()
                )

                // +1h chip
                FilterChip(
                    selected = false,
                    onClick = { viewModel.onQuickChipSelected(QuickTimeChip.PLUS_1H) },
                    label = { Text("+1h") },
                    colors = chipColors()
                )

                // Pick date/time
                FilterChip(
                    selected = uiState.overrideDueAt != null,
                    onClick = { viewModel.setShowDateTimePicker(true) },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.EditCalendar,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pick date")
                        }
                    },
                    colors = chipColors()
                )

                // Repeat chip (cycles None -> Daily -> Weekdays -> Weekly)
                val recurrenceLabel = when (uiState.effectiveRecurrence) {
                    Recurrence.Daily -> "Daily"
                    Recurrence.Weekdays -> "Weekdays"
                    is Recurrence.Weekly -> "Weekly"
                    is Recurrence.Monthly -> "Monthly"
                    else -> "Repeat"
                }
                FilterChip(
                    selected = uiState.effectiveRecurrence != null,
                    onClick = {
                        val next = when (uiState.effectiveRecurrence) {
                            null -> Recurrence.Daily
                            Recurrence.Daily -> Recurrence.Weekdays
                            Recurrence.Weekdays -> Recurrence.Weekly(setOf(1))
                            else -> null
                        }
                        viewModel.onSetRecurrence(next)
                    },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Repeat,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(recurrenceLabel)
                        }
                    },
                    colors = chipColors()
                )

                // Deadline Toggle Chip
                val isDeadline = uiState.effectiveKind == ItemKind.DEADLINE
                FilterChip(
                    selected = isDeadline,
                    onClick = viewModel::onToggleDeadline,
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Whatshot,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isDeadline) CookedTheme.colors.onPrimary else CookedTheme.colors.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Deadline")
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CookedTheme.colors.primary,
                        selectedLabelColor = CookedTheme.colors.onPrimary,
                        containerColor = CookedTheme.colors.surfaceContainerLow,
                        labelColor = CookedTheme.colors.onSurface
                    )
                )

                // Critical Toggle Chip ("Urgent !!")
                val isCritical = uiState.effectiveCritical
                FilterChip(
                    selected = isCritical,
                    onClick = viewModel::onToggleCritical,
                    label = { Text("Urgent !!") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CookedTheme.colors.heatFor(HeatLevel.COOKED).color,
                        selectedLabelColor = CookedTheme.colors.heatFor(HeatLevel.COOKED).onColor,
                        containerColor = CookedTheme.colors.surfaceContainerLow,
                        labelColor = CookedTheme.colors.onSurface
                    )
                )
            }

            // Save Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = {
                        if (uiState.canSave) {
                            viewModel.save()
                        }
                    },
                    enabled = uiState.canSave && !uiState.isSaving,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (uiState.canSave) CookedTheme.colors.primary else CookedTheme.colors.surfaceContainerHigh,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "Save",
                        tint = if (uiState.canSave) CookedTheme.colors.onPrimary else CookedTheme.colors.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }

    // Material 3 Date Picker Dialog
    if (uiState.showDateTimePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { viewModel.setShowDateTimePicker(false) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val pickedDate = Instant.ofEpochMilli(selectedMillis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                            val now = Instant.now().atZone(ZoneId.systemDefault())
                            val pickedInstant = pickedDate.atTime(18, 0).atZone(ZoneId.systemDefault()).toInstant()
                            viewModel.onSetDueAt(pickedInstant)
                        }
                        viewModel.setShowDateTimePicker(false)
                    }
                ) {
                    Text("OK", color = CookedTheme.colors.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setShowDateTimePicker(false) }) {
                    Text("Cancel", color = CookedTheme.colors.onSurfaceVariant)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun chipColors() = FilterChipDefaults.filterChipColors(
    containerColor = CookedTheme.colors.surfaceContainerLow,
    labelColor = CookedTheme.colors.onSurface,
    selectedContainerColor = CookedTheme.colors.primaryContainer,
    selectedLabelColor = CookedTheme.colors.onPrimaryContainer
)

private fun buildPreviewText(uiState: QuickAddUiState): String {
    val dueAt = uiState.effectiveDueAt
    if (dueAt == null && uiState.effectiveEffortMinutes == null && uiState.effectiveRecurrence == null && !uiState.effectiveCritical) {
        return ""
    }

    val parts = mutableListOf<String>()

    if (dueAt != null) {
        val zone = ZoneId.systemDefault()
        val zdt = dueAt.atZone(zone)
        val today = LocalDate.now(zone)
        val targetDate = zdt.toLocalDate()
        val timeStr = DateTimeFormatter.ofPattern("h:mm a").format(zdt)

        val datePart = when (targetDate) {
            today -> "Today, $timeStr"
            today.plusDays(1) -> "Tomorrow, $timeStr"
            else -> DateTimeFormatter.ofPattern("EEE d MMM, h:mm a").format(zdt)
        }
        parts.add(datePart)
    }

    if (uiState.effectiveKind == ItemKind.DEADLINE) {
        parts.add("Deadline")
    }

    if (uiState.effectiveEffortMinutes != null) {
        val mins = uiState.effectiveEffortMinutes!!
        val effortStr = if (mins >= 60) {
            val h = mins / 60
            val m = mins % 60
            if (m > 0) "~${h}h ${m}m" else "~${h}h"
        } else {
            "~${mins}m"
        }
        parts.add(effortStr)
    }

    when (val rec = uiState.effectiveRecurrence) {
        Recurrence.Daily -> parts.add("🔁 Daily")
        Recurrence.Weekdays -> parts.add("🔁 Weekdays")
        is Recurrence.Weekly -> parts.add("🔁 Weekly")
        is Recurrence.Monthly -> parts.add("🔁 Monthly")
        is Recurrence.EveryNDays -> parts.add("🔁 Every ${rec.n}d")
        Recurrence.Yearly -> parts.add("🔁 Yearly")
        null -> {}
    }

    if (uiState.effectiveCritical) {
        parts.add("🚨 Urgent")
    }

    return parts.joinToString(" • ")
}

private class QuickAddVisualTransformation(
    private val tokens: List<Highlight>,
    private val timeColor: Color,
    private val recurrenceColor: Color,
    private val effortColor: Color,
    private val criticalColor: Color,
    private val deadlineColor: Color
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        if (tokens.isEmpty() || raw.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val builder = AnnotatedString.Builder(raw)
        for (token in tokens) {
            val start = token.start.coerceIn(0, raw.length)
            val end = token.end.coerceIn(0, raw.length)
            if (start < end) {
                val color = when (token.type) {
                    HighlightType.TIME -> timeColor
                    HighlightType.RECURRENCE -> recurrenceColor
                    HighlightType.EFFORT -> effortColor
                    HighlightType.CRITICAL -> criticalColor
                    HighlightType.DEADLINE -> deadlineColor
                }
                builder.addStyle(
                    SpanStyle(
                        color = color,
                        fontWeight = FontWeight.Bold
                    ),
                    start,
                    end
                )
            }
        }
        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }
}
