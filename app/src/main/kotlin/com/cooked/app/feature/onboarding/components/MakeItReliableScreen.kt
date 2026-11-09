package com.cooked.app.feature.onboarding.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cooked.app.R
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.rememberAreAnimationsDisabled

// ─────────────────────────────────────────────────────────────────────────────
// COMPOSABLE: RELIABILITY STATUS OVERVIEW
// Displays "Your reliability setup: X of 3 configured" with mini segmented bars
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ReliabilityOverviewCard(
    configuredCount: Int,
    totalCount: Int = 3,
    modifier: Modifier = Modifier
) {
    val animationsDisabled = rememberAreAnimationsDisabled()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(VibeTokens.CardCornerRadius),
        color = VibeTokens.SecondarySurface,
        border = BorderStroke(1.dp, VibeTokens.Border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Your reliability setup",
                    style = CookedTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = (-0.2).sp
                    ),
                    color = VibeTokens.PrimaryText
                )

                Text(
                    text = "$configuredCount of $totalCount configured",
                    style = CookedTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = VibeTokens.SecondaryText
                )
            }

            // Mini 3-segment progress indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(totalCount) { index ->
                    val isSegmentFilled = index < configuredCount
                    val segmentColor by animateColorAsState(
                        targetValue = if (isSegmentFilled) VibeTokens.PrimaryAccent else VibeTokens.Border,
                        animationSpec = if (animationsDisabled) tween(0) else tween(250),
                        label = "overviewSegmentColor_$index"
                    )

                    Box(
                        modifier = Modifier
                            .height(5.dp)
                            .width(22.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(segmentColor)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPOSABLE: REUSABLE PERMISSION STATUS BADGE ([Enabled])
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun PermissionStatusBadge(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF222B0F))
            .border(BorderStroke(1.dp, Color(0xFF334215)), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = null,
            tint = VibeTokens.PrimaryAccent,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = "Enabled",
            style = CookedTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            ),
            color = VibeTokens.PrimaryAccent
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPOSABLE: REUSABLE PERMISSION STATUS CARD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun PermissionStatusCard(
    @DrawableRes iconRes: Int,
    title: String,
    description: String,
    isGranted: Boolean,
    actionLabel: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    isOptional: Boolean = false,
    actionButtonTonal: Boolean = false
) {
    val haptic = LocalHapticFeedback.current
    val animationsDisabled = rememberAreAnimationsDisabled()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed && !isGranted) 0.98f else 1.0f,
        animationSpec = if (animationsDisabled) tween(0) else tween(150),
        label = "permCardScale_$title"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(VibeTokens.CardCornerRadius),
        color = VibeTokens.SecondarySurface,
        border = BorderStroke(1.dp, VibeTokens.Border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Permission Icon Squircle
            val iconContainerColor = if (isGranted) Color(0xFF202A0D) else VibeTokens.PrimarySurface
            val iconTintColor = if (isGranted) VibeTokens.PrimaryAccent else VibeTokens.SecondaryText

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(iconContainerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = iconTintColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title, Optional Badge, and Description
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        style = CookedTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            letterSpacing = (-0.2).sp
                        ),
                        color = VibeTokens.PrimaryText
                    )

                    if (isOptional) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(VibeTokens.ElevatedSurface)
                                .border(BorderStroke(1.dp, VibeTokens.Border), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Optional",
                                style = CookedTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = VibeTokens.MutedText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = description,
                    style = CookedTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 16.5.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = VibeTokens.SecondaryText
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Right Action: either Enabled badge or Action button
            if (isGranted) {
                PermissionStatusBadge()
            } else {
                Box(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 76.dp, minHeight = 36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (actionButtonTonal) VibeTokens.ElevatedSurface else VibeTokens.PrimaryAccent
                        )
                        .border(
                            if (actionButtonTonal) BorderStroke(1.dp, VibeTokens.Border) else BorderStroke(0.dp, Color.Transparent),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            role = Role.Button,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onActionClick()
                            }
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = actionLabel,
                        style = CookedTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        ),
                        color = if (actionButtonTonal) VibeTokens.PrimaryText else Color(0xFF0B0C0A)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPOSABLE: RELIABILITY EXPLANATION ("Why this matters")
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ReliabilityInfoSection(
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
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(VibeTokens.SecondarySurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = null,
                    tint = VibeTokens.MutedText,
                    modifier = Modifier.size(15.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Why this matters",
                    style = CookedTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    ),
                    color = VibeTokens.PrimaryText
                )

                Text(
                    text = "Cooked works around Android’s background scheduling limits to make your reminders as dependable as possible.",
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
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPOSABLE: REDESIGNED COMPLETE "MAKE IT RELIABLE" CONTENT
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun MakeItReliableContent(
    notificationsGranted: Boolean,
    exactAlarmsGranted: Boolean,
    batteryUnrestricted: Boolean,
    onRefreshPermissions: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    // Activity launcher for Android 13+ POST_NOTIFICATIONS
    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        onRefreshPermissions()
    }

    // Dynamic count of configured permissions
    val configuredCount = listOf(notificationsGranted, exactAlarmsGranted, batteryUnrestricted).count { it }
    val allConfigured = notificationsGranted && exactAlarmsGranted && batteryUnrestricted

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
                .padding(bottom = 100.dp), // Clear bottom CTA bar
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // Hero Section with Title & Graphic
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append("Make it\n")
                            withStyle(SpanStyle(color = VibeTokens.PrimaryAccent)) {
                                append("reliable.")
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
                        text = "Give Cooked the permissions it needs to keep your reminders on time.",
                        style = CookedTheme.typography.bodyLarge.copy(
                            fontSize = 14.5.sp,
                            lineHeight = 21.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = VibeTokens.SecondaryText
                    )
                }

                // Decorative Calendar / Emblem graphic with radiant rays
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E280C)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_calendar_sparkle),
                        contentDescription = null,
                        tint = VibeTokens.PrimaryAccent,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            // Reliability Setup Progress Overview
            ReliabilityOverviewCard(
                configuredCount = configuredCount,
                totalCount = 3
            )

            // Permission 1: Notifications
            PermissionStatusCard(
                iconRes = R.drawable.ic_cat_bell,
                title = "Notifications",
                description = "Let Cooked send reminders and deadline alerts when you need them.",
                isGranted = notificationsGranted,
                actionLabel = "Enable",
                onActionClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        try {
                            context.startActivity(
                                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                            )
                        } catch (_: Exception) {
                            try {
                                context.startActivity(
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                )
                            } catch (_: Exception) {}
                        }
                    }
                }
            )

            // Permission 2: Exact Alarms
            PermissionStatusCard(
                iconRes = R.drawable.ic_alarm_clock,
                title = "Exact Alarms",
                description = "Helps Cooked trigger important reminders close to their scheduled time.",
                isGranted = exactAlarmsGranted,
                actionLabel = "Enable",
                onActionClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        try {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                    Uri.parse("package:${context.packageName}")
                                ).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                            )
                        } catch (_: Exception) {
                            try {
                                context.startActivity(
                                    Intent(Settings.ACTION_SETTINGS).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                )
                            } catch (_: Exception) {}
                        }
                    }
                }
            )

            // Permission 3: Background Reliability (Battery Optimization)
            PermissionStatusCard(
                iconRes = R.drawable.ic_battery_bolt,
                title = "Background Reliability",
                description = "Some battery-saving features can delay background reminders. Check your device’s battery settings for the most reliable experience.",
                isGranted = batteryUnrestricted,
                actionLabel = "Review Settings",
                isOptional = true,
                actionButtonTonal = true,
                onActionClick = {
                    try {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:${context.packageName}")
                            ).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                        )
                    } catch (_: Exception) {
                        try {
                            context.startActivity(
                                Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                            )
                        } catch (_: Exception) {}
                    }
                }
            )

            // Reliability Info Section ("Why this matters")
            ReliabilityInfoSection()
        }

        // Docked Bottom CTA Bar with gradient scrim
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
                    onNext()
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
                        text = if (allConfigured) "You’re all set" else "Lock in reliability",
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
// PREVIEWS
// ─────────────────────────────────────────────────────────────────────────────
@Preview(name = "Make It Reliable - Initial (1 of 3 configured)", widthDp = 393, heightDp = 840)
@Composable
private fun MakeItReliablePreviewInitial() {
    CookedTheme(darkTheme = true) {
        Column(modifier = Modifier.fillMaxSize().background(VibeTokens.Background)) {
            OnboardingProgressHeader(
                currentStep = 3,
                totalSteps = 5,
                onSkip = {}
            )
            MakeItReliableContent(
                notificationsGranted = true,
                exactAlarmsGranted = false,
                batteryUnrestricted = false,
                onRefreshPermissions = {},
                onNext = {}
            )
        }
    }
}

@Preview(name = "Make It Reliable - Fully Configured (3 of 3)", widthDp = 393, heightDp = 840)
@Composable
private fun MakeItReliablePreviewAllSet() {
    CookedTheme(darkTheme = true) {
        Column(modifier = Modifier.fillMaxSize().background(VibeTokens.Background)) {
            OnboardingProgressHeader(
                currentStep = 3,
                totalSteps = 5,
                onSkip = {}
            )
            MakeItReliableContent(
                notificationsGranted = true,
                exactAlarmsGranted = true,
                batteryUnrestricted = true,
                onRefreshPermissions = {},
                onNext = {}
            )
        }
    }
}

@Preview(name = "Make It Reliable - Compact Screen (320dp)", widthDp = 320, heightDp = 640)
@Composable
private fun MakeItReliablePreviewCompact() {
    CookedTheme(darkTheme = true) {
        Column(modifier = Modifier.fillMaxSize().background(VibeTokens.Background)) {
            OnboardingProgressHeader(
                currentStep = 3,
                totalSteps = 5,
                onSkip = {}
            )
            MakeItReliableContent(
                notificationsGranted = false,
                exactAlarmsGranted = false,
                batteryUnrestricted = false,
                onRefreshPermissions = {},
                onNext = {}
            )
        }
    }
}
