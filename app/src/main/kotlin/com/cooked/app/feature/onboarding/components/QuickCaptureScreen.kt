package com.cooked.app.feature.onboarding.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cooked.app.R
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.rememberAreAnimationsDisabled
import com.cooked.app.domain.model.Recurrence
import com.cooked.app.domain.parse.ParseResult
import com.cooked.app.domain.parse.QuickParser
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// DATA MODELS FOR QUICK SUGGESTIONS
// ─────────────────────────────────────────────────────────────────────────────
data class QuickSuggestionItem(
    @field:DrawableRes val iconRes: Int,
    val promptText: String
)

val QUICK_SUGGESTIONS = listOf(
    QuickSuggestionItem(
        iconRes = R.drawable.ic_cat_exam,
        promptText = "Submit assignment Friday 5pm"
    ),
    QuickSuggestionItem(
        iconRes = R.drawable.ic_cat_bill,
        promptText = "Pay rent on the 1st"
    ),
    QuickSuggestionItem(
        iconRes = R.drawable.ic_cat_droplet,
        promptText = "Drink water in 20 minutes"
    )
)

// ─────────────────────────────────────────────────────────────────────────────
// PARSING & DATE FORMATTING HELPERS
// ─────────────────────────────────────────────────────────────────────────────
fun formatParsedDateLabel(dueAt: Instant, zone: ZoneId, now: Instant = Instant.now()): String {
    val zdt = dueAt.atZone(zone)
    val today = now.atZone(zone).toLocalDate()
    val taskDate = zdt.toLocalDate()
    return when {
        taskDate == today -> "Today"
        taskDate == today.plusDays(1) -> "Tomorrow"
        taskDate.isAfter(today) && taskDate.isBefore(today.plusDays(7)) -> {
            taskDate.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        }
        else -> {
            val formatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
            taskDate.format(formatter)
        }
    }
}

fun formatParsedTimeLabel(dueAt: Instant, zone: ZoneId): String {
    val zdt = dueAt.atZone(zone)
    val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    return zdt.toLocalTime().format(formatter)
}

fun formatRecurrenceLabel(recurrence: Recurrence?): String? {
    return when (recurrence) {
        is Recurrence.Daily -> "Every day"
        is Recurrence.Weekdays -> "Weekdays"
        is Recurrence.Weekly -> "Every week"
        is Recurrence.EveryNDays -> "Every ${recurrence.n} days"
        is Recurrence.Monthly -> "Monthly"
        is Recurrence.Yearly -> "Yearly"
        null -> null
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPOSABLE: SMART QUICK CAPTURE INPUT FIELD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun QuickCaptureInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "e.g. Call dentist tomorrow at 11am",
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val animationsDisabled = rememberAreAnimationsDisabled()

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) VibeTokens.PrimaryAccent else VibeTokens.Border,
        animationSpec = if (animationsDisabled) tween(0) else tween(200),
        label = "inputBorder"
    )

    val iconTint by animateColorAsState(
        targetValue = if (isFocused) VibeTokens.PrimaryAccent else VibeTokens.SecondaryText,
        animationSpec = if (animationsDisabled) tween(0) else tween(200),
        label = "inputIconTint"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(VibeTokens.CardCornerRadius))
            .background(VibeTokens.SecondarySurface)
            .border(
                BorderStroke(if (isFocused) 1.5.dp else 1.dp, borderColor),
                RoundedCornerShape(VibeTokens.CardCornerRadius)
            )
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Contextual quick capture icon
            Icon(
                painter = painterResource(id = R.drawable.ic_calendar_sparkle),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Main text input
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = CookedTheme.typography.bodyLarge.copy(
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = VibeTokens.MutedText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    textStyle = CookedTheme.typography.bodyLarge.copy(
                        color = VibeTokens.PrimaryText,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    cursorBrush = SolidColor(VibeTokens.PrimaryAccent),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { onDone() }),
                    singleLine = true,
                    interactionSource = interactionSource
                )
            }

            // Clear button ("X") when text is present
            if (value.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(VibeTokens.ElevatedSurface)
                        .clickable { onValueChange("") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Clear input",
                        tint = VibeTokens.SecondaryText,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPOSABLE: SMART INTERPRETATION PREVIEW CARD
// Displays real-time NLP understanding of date, time, and recurrence
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SmartInterpretationPreviewCard(
    parsed: ParseResult,
    zone: ZoneId,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateLabel = parsed.dueAt?.let { formatParsedDateLabel(it, zone) }
    val timeLabel = parsed.dueAt?.let { formatParsedTimeLabel(it, zone) }
    val recurrenceLabel = formatRecurrenceLabel(parsed.recurrence)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(VibeTokens.NotificationCornerRadius),
        color = VibeTokens.SecondarySurface,
        border = BorderStroke(1.dp, VibeTokens.Border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Sparkle + "Cooked understands:" + Checkmark badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_sparkle_celebration),
                        contentDescription = null,
                        tint = VibeTokens.PrimaryAccent,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Cooked understands:",
                        style = CookedTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        ),
                        color = VibeTokens.SecondaryText
                    )
                }

                // Green confirmation indicator
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(VibeTokens.PrimaryAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color(0xFF0B0C0A),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            // Interpreted Task Title
            Text(
                text = parsed.title.ifBlank { "New Reminder" },
                style = CookedTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.5.sp,
                    letterSpacing = (-0.2).sp
                ),
                color = VibeTokens.PrimaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Extracted Details Row (Date Chip, Time Chip, Recurrence, Edit Action)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Date Chip
                    if (dateLabel != null) {
                        ExtractedChip(text = dateLabel, iconRes = R.drawable.ic_chip_calendar)
                    }

                    // Separator dot
                    if (dateLabel != null && timeLabel != null) {
                        Text(
                            text = "•",
                            style = CookedTheme.typography.labelSmall,
                            color = VibeTokens.MutedText
                        )
                    }

                    // Time Chip
                    if (timeLabel != null && !parsed.allDay) {
                        ExtractedChip(text = timeLabel, iconRes = R.drawable.ic_chip_clock)
                    }

                    // Recurrence Chip
                    if (recurrenceLabel != null) {
                        ExtractedChip(text = recurrenceLabel, iconRes = R.drawable.ic_recurrence_repeat)
                    }
                }

                // Optional Edit Action Button
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(VibeTokens.ElevatedSurface)
                        .border(BorderStroke(1.dp, VibeTokens.Border), CircleShape)
                        .clickable(onClick = onEditClick)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit interpreted details",
                        tint = VibeTokens.SecondaryText,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Edit",
                        style = CookedTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        ),
                        color = VibeTokens.PrimaryText
                    )
                }
            }
        }
    }
}

@Composable
private fun ExtractedChip(
    text: String,
    modifier: Modifier = Modifier,
    @androidx.annotation.DrawableRes iconRes: Int? = null
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF202A0E))
            .border(BorderStroke(1.dp, Color(0xFF2E3D12)), RoundedCornerShape(8.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (iconRes != null) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = VibeTokens.PrimaryAccent,
                modifier = Modifier.size(12.dp)
            )
        }
        Text(
            text = text,
            style = CookedTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.5.sp
            ),
            color = VibeTokens.PrimaryAccent
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPOSABLE: QUICK SUGGESTIONS SECTION
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun QuickSuggestionsSection(
    onSelectSuggestion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Section Header
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Need a starting point?",
                style = CookedTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.5.sp,
                    letterSpacing = (-0.2).sp
                ),
                color = VibeTokens.PrimaryText
            )
            Text(
                text = "Try one of these",
                style = CookedTheme.typography.bodySmall.copy(
                    fontSize = 12.sp
                ),
                color = VibeTokens.SecondaryText
            )
        }

        // 3 Selectable Rows
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            QUICK_SUGGESTIONS.forEach { item ->
                QuickSuggestionRow(
                    item = item,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSelectSuggestion(item.promptText)
                    }
                )
            }
        }
    }
}

@Composable
private fun QuickSuggestionRow(
    item: QuickSuggestionItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animationsDisabled = rememberAreAnimationsDisabled()

    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = if (animationsDisabled) tween(0) else tween(150),
        label = "suggestionRowScale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(VibeTokens.CardCornerRadius))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            ),
        shape = RoundedCornerShape(VibeTokens.CardCornerRadius),
        color = VibeTokens.SecondarySurface,
        border = BorderStroke(1.dp, VibeTokens.Border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon in subtle container
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF222B0F)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = item.iconRes),
                    contentDescription = null,
                    tint = VibeTokens.PrimaryAccent,
                    modifier = Modifier.size(17.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Suggestion Text
            Text(
                text = item.promptText,
                style = CookedTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.5.sp
                ),
                color = VibeTokens.PrimaryText,
                modifier = Modifier.weight(1f)
            )

            // Chevron Right
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = VibeTokens.MutedText,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPOSABLE: BRAND PERSONALITY QUOTE BANNER
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun QuickCapturePersonalityBanner(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(VibeTokens.NotificationCornerRadius),
        color = VibeTokens.PrimarySurface,
        border = BorderStroke(1.dp, VibeTokens.BorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Illustrated Calendar Icon with subtle glow
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1E280C)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_calendar_sparkle),
                    contentDescription = null,
                    tint = VibeTokens.PrimaryAccent,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Personality Message
            Text(
                text = "One less thing living rent-free in your head.",
                style = CookedTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 16.5.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = VibeTokens.SecondaryText
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPOSABLE: REDESIGNED COMPLETE "QUICK CAPTURE" SCREEN CONTENT
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun QuickCaptureContent(
    input: String,
    isCreated: Boolean,
    isSaving: Boolean,
    errorMessage: String?,
    onInputChanged: (String) -> Unit,
    onSaveAndNext: (String) -> Unit,
    onSkipLater: () -> Unit,
    modifier: Modifier = Modifier,
    zone: ZoneId = remember { ZoneId.systemDefault() }
) {
    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()

    // Real-time NLP parsing
    val parsedResult = remember(input) {
        if (input.trim().length >= 3) {
            QuickParser.parse(input.trim(), Instant.now(), zone)
        } else {
            null
        }
    }

    val isInputValid = input.isNotBlank()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VibeTokens.Background)
            .imePadding()
    ) {
        // Scrollable content area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(bottom = 100.dp), // Clear bottom CTA area
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // Main Hero Heading
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = buildAnnotatedString {
                        append("What are you\n")
                        withStyle(SpanStyle(color = VibeTokens.PrimaryAccent)) {
                            append("forgetting")
                        }
                        append(" right now?")
                    },
                    style = CookedTheme.typography.headlineLarge.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.6).sp,
                        lineHeight = 36.sp
                    ),
                    color = VibeTokens.PrimaryText
                )

                Text(
                    text = "Get it out of your head. Cooked will make sure it gets done.",
                    style = CookedTheme.typography.bodyLarge.copy(
                        fontSize = 14.5.sp,
                        lineHeight = 21.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = VibeTokens.SecondaryText
                )
            }

            // Natural Language Input Field
            QuickCaptureInputField(
                value = input,
                onValueChange = onInputChanged,
                onDone = {
                    focusManager.clearFocus()
                    if (isInputValid && !isSaving) {
                        onSaveAndNext(input)
                    }
                },
                focusRequester = focusRequester
            )

            // Smart Interpretation Preview (Appears with smooth animation when input is recognized)
            AnimatedVisibility(
                visible = parsedResult != null && parsedResult.title.isNotBlank(),
                enter = fadeIn(tween(200)) + expandVertically(tween(250, easing = FastOutSlowInEasing)),
                exit = fadeOut(tween(150)) + shrinkVertically(tween(200))
            ) {
                if (parsedResult != null) {
                    SmartInterpretationPreviewCard(
                        parsed = parsedResult,
                        zone = zone,
                        onEditClick = {
                            focusRequester.requestFocus()
                        }
                    )
                }
            }

            // Error message banner if saving failed
            if (errorMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF331616),
                    border = BorderStroke(1.dp, Color(0xFF662222))
                ) {
                    Text(
                        text = errorMessage,
                        style = CookedTheme.typography.bodySmall,
                        color = Color(0xFFFF8888),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Quick Suggestions Section ("Need a starting point?")
            QuickSuggestionsSection(
                onSelectSuggestion = { suggestion ->
                    onInputChanged(suggestion)
                }
            )

            // Personality Banner ("One less thing living rent-free in your head.")
            QuickCapturePersonalityBanner()
        }

        // Docked Bottom CTA Bar with smooth gradient scrim
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            VibeTokens.Background.copy(alpha = 0.88f),
                            VibeTokens.Background
                        )
                    )
                )
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (isInputValid) {
                        onSaveAndNext(input)
                    } else {
                        onSkipLater()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(VibeTokens.ButtonCornerRadius),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VibeTokens.PrimaryAccent,
                    contentColor = Color(0xFF0B0C0A),
                    disabledContainerColor = VibeTokens.ElevatedSurface,
                    disabledContentColor = VibeTokens.MutedText
                ),
                enabled = !isSaving,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                if (isSaving) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color(0xFF0B0C0A),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Understanding…",
                            style = CookedTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isInputValid) "Lock it in" else "I'll add later",
                            style = CookedTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                letterSpacing = (-0.1).sp
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PREVIEWS
// ─────────────────────────────────────────────────────────────────────────────
@Preview(name = "Quick Capture - Empty State (393dp)", widthDp = 393, heightDp = 840)
@Composable
private fun QuickCapturePreviewEmpty() {
    CookedTheme(darkTheme = true) {
        Column(modifier = Modifier.fillMaxSize().background(VibeTokens.Background)) {
            OnboardingProgressHeader(
                currentStep = 2,
                totalSteps = 5,
                onSkip = {}
            )
            QuickCaptureContent(
                input = "",
                isCreated = false,
                isSaving = false,
                errorMessage = null,
                onInputChanged = {},
                onSaveAndNext = {},
                onSkipLater = {}
            )
        }
    }
}

@Preview(name = "Quick Capture - Active with Preview (393dp)", widthDp = 393, heightDp = 840)
@Composable
private fun QuickCapturePreviewActive() {
    CookedTheme(darkTheme = true) {
        Column(modifier = Modifier.fillMaxSize().background(VibeTokens.Background)) {
            OnboardingProgressHeader(
                currentStep = 2,
                totalSteps = 5,
                onSkip = {}
            )
            QuickCaptureContent(
                input = "Submit assignment Friday 5pm",
                isCreated = false,
                isSaving = false,
                errorMessage = null,
                onInputChanged = {},
                onSaveAndNext = {},
                onSkipLater = {}
            )
        }
    }
}

@Preview(name = "Quick Capture - Compact Device (320dp)", widthDp = 320, heightDp = 640)
@Composable
private fun QuickCapturePreviewCompact() {
    CookedTheme(darkTheme = true) {
        Column(modifier = Modifier.fillMaxSize().background(VibeTokens.Background)) {
            OnboardingProgressHeader(
                currentStep = 2,
                totalSteps = 5,
                onSkip = {}
            )
            QuickCaptureContent(
                input = "Drink water in 20 minutes",
                isCreated = false,
                isSaving = false,
                errorMessage = null,
                onInputChanged = {},
                onSaveAndNext = {},
                onSkipLater = {}
            )
        }
    }
}
