package com.cooked.app.feature.onboarding.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cooked.app.R
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.rememberAreAnimationsDisabled
import com.cooked.app.domain.model.Personality

// ─────────────────────────────────────────────────────────────────────────────
// DESIGN SYSTEM TOKENS FOR "PICK YOUR VIBE"
// Modern, dark-first consumer technology aesthetic (Obsidian & Volt)
// ─────────────────────────────────────────────────────────────────────────────
object VibeTokens {
    val Background = Color(0xFF0B0C0A)
    val PrimarySurface = Color(0xFF141512)
    val SecondarySurface = Color(0xFF1B1D18)
    val ElevatedSurface = Color(0xFF22241E)
    val PrimaryText = Color(0xFFF5F5F0)
    val SecondaryText = Color(0xFFA2A49B)
    val MutedText = Color(0xFF777A70)
    val PrimaryAccent = Color(0xFFC6FF3D)
    val AccentPressed = Color(0xFFAEEA29)
    val Border = Color(0xFF292B25)
    val BorderSubtle = Color(0xFF1F211B)
    val Success = Color(0xFF83D65A)

    val CardCornerRadius = 18.dp
    val NotificationCornerRadius = 16.dp
    val ButtonCornerRadius = 20.dp
}

// ─────────────────────────────────────────────────────────────────────────────
// DATA MODELS
// ─────────────────────────────────────────────────────────────────────────────
data class PersonalityOption(
    val personality: Personality,
    val title: String,
    val description: String,
    @field:DrawableRes val iconRes: Int
)

val PERSONALITY_OPTIONS = listOf(
    PersonalityOption(
        personality = Personality.MILD,
        title = "Mild",
        description = "Friendly, gentle and supportive.",
        iconRes = R.drawable.ic_vibe_mild
    ),
    PersonalityOption(
        personality = Personality.SPICY,
        title = "Spicy",
        description = "Sarcastic, playful and slightly aggressive.",
        iconRes = R.drawable.ic_vibe_spicy
    ),
    PersonalityOption(
        personality = Personality.UNHINGED,
        title = "Unhinged",
        description = "Maximum accountability with chaotic reminders.",
        iconRes = R.drawable.ic_vibe_unhinged
    )
)

data class NotificationPreviewItem(
    val stage: String,
    val timeAgo: String,
    val taskTitle: String = "Submit final project",
    val message: String
)

val MILD_NOTIFICATIONS = listOf(
    NotificationPreviewItem(
        stage = "Friendly reminder",
        timeAgo = "now",
        message = "Hey! Don’t forget your final project. You’ve got this."
    ),
    NotificationPreviewItem(
        stage = "Gentle nudge",
        timeAgo = "15m ago",
        message = "Just checking in. Your project is still waiting."
    ),
    NotificationPreviewItem(
        stage = "Last reminder",
        timeAgo = "45m ago",
        message = "Time to wrap this up. Let’s get it done!"
    )
)

val SPICY_NOTIFICATIONS = listOf(
    NotificationPreviewItem(
        stage = "First reminder",
        timeAgo = "now",
        message = "Heat is on for Submit final project."
    ),
    NotificationPreviewItem(
        stage = "Nudge #2",
        timeAgo = "15m ago",
        message = "Friendly reminder that Submit final project isn’t doing itself."
    ),
    NotificationPreviewItem(
        stage = "Final call",
        timeAgo = "45m ago",
        message = "Stop negotiating with your deadline. Get it done."
    )
)

val UNHINGED_NOTIFICATIONS = listOf(
    NotificationPreviewItem(
        stage = "Incoming",
        timeAgo = "now",
        message = "BESTIE. THE PROJECT."
    ),
    NotificationPreviewItem(
        stage = "Emergency",
        timeAgo = "15m ago",
        message = "Your deadline is approaching and you’re just existing?"
    ),
    NotificationPreviewItem(
        stage = "FINAL WARNING",
        timeAgo = "45m ago",
        message = "THIS IS NOT A DRILL. GO FINISH IT."
    )
)

// ─────────────────────────────────────────────────────────────────────────────
// COMPOSABLE: TOP PROGRESS INDICATOR WITH SKIP
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun OnboardingProgressHeader(
    currentStep: Int,
    totalSteps: Int,
    onSkip: () -> Unit,
    showSkip: Boolean = true,
    modifier: Modifier = Modifier
) {
    val animationsDisabled = rememberAreAnimationsDisabled()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thin horizontal segmented progress indicator
        Row(
            modifier = Modifier.weight(1f, fill = false),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(totalSteps) { index ->
                val isActive = index == currentStep
                val isCompleted = index < currentStep

                val segmentColor by animateColorAsState(
                    targetValue = when {
                        isActive -> VibeTokens.PrimaryAccent
                        isCompleted -> VibeTokens.PrimaryAccent.copy(alpha = 0.55f)
                        else -> VibeTokens.Border
                    },
                    animationSpec = if (animationsDisabled) tween(0) else tween(250),
                    label = "segmentColor_$index"
                )

                val segmentWidth by animateFloatAsState(
                    targetValue = if (isActive) 28f else 18f,
                    animationSpec = if (animationsDisabled) tween(0) else tween(250),
                    label = "segmentWidth_$index"
                )

                Box(
                    modifier = Modifier
                        .height(3.5.dp)
                        .width(segmentWidth.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(segmentColor)
                )
            }
        }

        // Top-right Skip action
        if (showSkip) {
            TextButton(
                onClick = onSkip,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Skip",
                    style = CookedTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.2.sp
                    ),
                    color = VibeTokens.SecondaryText
                )
            }
        } else {
            Spacer(modifier = Modifier.width(48.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPOSABLE: PERSONALITY CARD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun PersonalityCard(
    option: PersonalityOption,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val animationsDisabled = rememberAreAnimationsDisabled()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Tactile spring scale: selected slightly elevates; press slightly compresses
    val targetScale = when {
        isPressed -> 0.96f
        isSelected -> 1.025f
        else -> 1.0f
    }
    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = if (animationsDisabled) tween(0) else tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "personalityCardScale_${option.title}"
    )

    // Animated colors between selected (lime fill + dark text) & unselected (dark elevated + subtle border)
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) VibeTokens.PrimaryAccent else VibeTokens.SecondarySurface,
        animationSpec = if (animationsDisabled) tween(0) else tween(200),
        label = "personalityContainer_${option.title}"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) VibeTokens.PrimaryAccent else VibeTokens.Border,
        animationSpec = if (animationsDisabled) tween(0) else tween(200),
        label = "personalityBorder_${option.title}"
    )
    val titleColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF0B0C0A) else VibeTokens.PrimaryText,
        animationSpec = if (animationsDisabled) tween(0) else tween(200),
        label = "personalityTitle_${option.title}"
    )
    val descriptionColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF1D2018) else VibeTokens.MutedText,
        animationSpec = if (animationsDisabled) tween(0) else tween(200),
        label = "personalityDesc_${option.title}"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF0B0C0A) else VibeTokens.SecondaryText,
        animationSpec = if (animationsDisabled) tween(0) else tween(200),
        label = "personalityIcon_${option.title}"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .clip(RoundedCornerShape(VibeTokens.CardCornerRadius))
            .background(containerColor)
            .border(
                BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
                RoundedCornerShape(VibeTokens.CardCornerRadius)
            )
            .selectable(
                selected = isSelected,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSelect()
                },
                role = Role.RadioButton,
                interactionSource = interactionSource,
                indication = null
            )
            .padding(horizontal = 10.dp, vertical = 14.dp)
            .defaultMinSize(minHeight = 152.dp)
            .semantics {
                contentDescription = "${option.title} vibe: ${option.description}. ${if (isSelected) "Selected" else "Not selected"}."
            }
    ) {
        // Selected indicator badge in top-right corner
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0B0C0A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = VibeTokens.PrimaryAccent,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Personality Mascot Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) Color(0xFF0B0C0A).copy(alpha = 0.12f)
                        else VibeTokens.PrimarySurface
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = option.iconRes),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Title
            Text(
                text = option.title,
                style = CookedTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = (-0.2).sp
                ),
                color = titleColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Description
            Text(
                text = option.description,
                style = CookedTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    lineHeight = 14.5.sp,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                ),
                color = descriptionColor,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPOSABLE: PERSONALITY SELECTION ROW
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun PersonalitySelectionRow(
    selectedPersonality: Personality,
    onSelect: (Personality) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PERSONALITY_OPTIONS.forEach { option ->
            PersonalityCard(
                option = option,
                isSelected = option.personality == selectedPersonality,
                onSelect = { onSelect(option.personality) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPOSABLE: NOTIFICATION PREVIEW CARD
// Modeled after native modern Android notifications with refined hierarchy
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun NotificationPreviewCard(
    preview: NotificationPreviewItem,
    personality: Personality,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(VibeTokens.NotificationCornerRadius),
        color = VibeTokens.SecondarySurface,
        border = BorderStroke(1.dp, VibeTokens.Border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.Top
        ) {
            // App Brand squircle icon on the left
            val iconContainerColor = when (personality) {
                Personality.MILD -> Color(0xFF1E2E20)
                Personality.SPICY -> Color(0xFF2E3812)
                Personality.UNHINGED -> Color(0xFF381C1A)
            }
            val iconAccentTint = when (personality) {
                Personality.MILD -> Color(0xFF9DE08D)
                Personality.SPICY -> VibeTokens.PrimaryAccent
                Personality.UNHINGED -> Color(0xFFFF6B6B)
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconContainerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_cat_flame),
                    contentDescription = null,
                    tint = iconAccentTint,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Notification Header, Title & Body
            Column(modifier = Modifier.weight(1f)) {
                // Header line: Cooked · [Stage]  ... [now]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cooked · ${preview.stage}",
                        style = CookedTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.5.sp
                        ),
                        color = VibeTokens.SecondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = preview.timeAgo,
                        style = CookedTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = VibeTokens.MutedText
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Bold Task Title
                Text(
                    text = preview.taskTitle,
                    style = CookedTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp,
                        letterSpacing = (-0.1).sp
                    ),
                    color = VibeTokens.PrimaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Notification Message Body
                Text(
                    text = preview.message,
                    style = CookedTheme.typography.bodySmall.copy(
                        fontSize = 12.5.sp,
                        lineHeight = 16.5.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = VibeTokens.SecondaryText
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPOSABLE: LIVE NOTIFICATION PREVIEW SECTION
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun LiveNotificationPreviewSection(
    personality: Personality,
    modifier: Modifier = Modifier
) {
    val animationsDisabled = rememberAreAnimationsDisabled()

    // Pulsing animation for the "LIVE PREVIEW" dot
    val infiniteTransition = rememberInfiniteTransition(label = "livePulse")
    val liveDotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "liveDotAlpha"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Section Header Row with Live Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Here’s how I’ll keep you accountable.",
                style = CookedTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    letterSpacing = (-0.2).sp
                ),
                color = VibeTokens.PrimaryText
            )

            // LIVE PREVIEW Pill Badge
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(VibeTokens.SecondarySurface)
                    .border(BorderStroke(1.dp, VibeTokens.Border), CircleShape)
                    .padding(horizontal = 9.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            if (animationsDisabled) VibeTokens.PrimaryAccent
                            else VibeTokens.PrimaryAccent.copy(alpha = liveDotAlpha)
                        )
                )

                Text(
                    text = "LIVE PREVIEW",
                    style = CookedTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.5.sp,
                        letterSpacing = 0.8.sp
                    ),
                    color = VibeTokens.PrimaryAccent
                )
            }
        }

        // 3 Escalating Notification Cards with smooth crossfade
        val notifications = when (personality) {
            Personality.MILD -> MILD_NOTIFICATIONS
            Personality.SPICY -> SPICY_NOTIFICATIONS
            Personality.UNHINGED -> UNHINGED_NOTIFICATIONS
        }

        AnimatedContent(
            targetState = notifications,
            transitionSpec = {
                if (animationsDisabled) {
                    fadeIn(tween(0)) togetherWith fadeOut(tween(0))
                } else {
                    (fadeIn(animationSpec = tween(220)) + slideInVertically(animationSpec = tween(220)) { it / 8 })
                        .togetherWith(fadeOut(animationSpec = tween(150)))
                }
            },
            label = "notificationsPreviewContent"
        ) { targetList ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                targetList.forEach { item ->
                    NotificationPreviewCard(
                        preview = item,
                        personality = personality
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPOSABLE: REDESIGNED COMPLETE "PICK YOUR VIBE" CONTENT
// Responsive, scroll-aware, dark-first premium interface
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun PickYourVibeContent(
    selectedPersonality: Personality,
    onSelectPersonality: (Personality) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()
    var isNavigating by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VibeTokens.Background)
    ) {
        // Scrollable content area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(bottom = 96.dp), // Ample bottom space so cards never hide under CTA
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Main Heading Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = buildAnnotatedString {
                        append("Pick your ")
                        withStyle(SpanStyle(color = VibeTokens.PrimaryAccent)) {
                            append("vibe.")
                        }
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
                    text = "How should Cooked talk to you when you’re procrastinating?",
                    style = CookedTheme.typography.bodyLarge.copy(
                        fontSize = 14.5.sp,
                        lineHeight = 21.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = VibeTokens.SecondaryText
                )
            }

            // Personality Selection Cards (Mild / Spicy / Unhinged)
            PersonalitySelectionRow(
                selectedPersonality = selectedPersonality,
                onSelect = onSelectPersonality
            )

            // Live Notification Previews
            LiveNotificationPreviewSection(
                personality = selectedPersonality
            )
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
                    if (!isNavigating) {
                        isNavigating = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSelectPersonality(selectedPersonality)
                        onNext()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(VibeTokens.ButtonCornerRadius),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VibeTokens.PrimaryAccent,
                    contentColor = Color(0xFF0B0C0A)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Sounds like me",
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

// ─────────────────────────────────────────────────────────────────────────────
// PREVIEWS (Light, Dark, Small Device, Font Scaling)
// ─────────────────────────────────────────────────────────────────────────────
@Preview(name = "Pick Your Vibe - Standard (393dp)", widthDp = 393, heightDp = 840)
@Composable
private fun PickYourVibePreviewStandard() {
    CookedTheme(darkTheme = true) {
        var selected by remember { mutableStateOf(Personality.SPICY) }
        Column(modifier = Modifier.fillMaxSize().background(VibeTokens.Background)) {
            OnboardingProgressHeader(
                currentStep = 1,
                totalSteps = 5,
                onSkip = {}
            )
            PickYourVibeContent(
                selectedPersonality = selected,
                onSelectPersonality = { selected = it },
                onNext = {}
            )
        }
    }
}

@Preview(name = "Pick Your Vibe - Compact Device (320dp)", widthDp = 320, heightDp = 640)
@Composable
private fun PickYourVibePreviewCompact() {
    CookedTheme(darkTheme = true) {
        var selected by remember { mutableStateOf(Personality.MILD) }
        Column(modifier = Modifier.fillMaxSize().background(VibeTokens.Background)) {
            OnboardingProgressHeader(
                currentStep = 1,
                totalSteps = 5,
                onSkip = {}
            )
            PickYourVibeContent(
                selectedPersonality = selected,
                onSelectPersonality = { selected = it },
                onNext = {}
            )
        }
    }
}

@Preview(name = "Pick Your Vibe - Large Font Scaling (1.3x)", widthDp = 393, heightDp = 840, fontScale = 1.3f)
@Composable
private fun PickYourVibePreviewLargeFont() {
    CookedTheme(darkTheme = true) {
        var selected by remember { mutableStateOf(Personality.UNHINGED) }
        Column(modifier = Modifier.fillMaxSize().background(VibeTokens.Background)) {
            OnboardingProgressHeader(
                currentStep = 1,
                totalSteps = 5,
                onSkip = {}
            )
            PickYourVibeContent(
                selectedPersonality = selected,
                onSelectPersonality = { selected = it },
                onNext = {}
            )
        }
    }
}
