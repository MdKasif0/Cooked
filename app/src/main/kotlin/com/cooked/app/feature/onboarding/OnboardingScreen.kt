package com.cooked.app.feature.onboarding

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.cooked.app.core.designsystem.components.CookedAppLogo
import com.cooked.app.core.designsystem.components.EggMascot
import com.cooked.app.core.designsystem.HeatLevel
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import com.cooked.app.R
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.components.CookedCard
import com.cooked.app.core.designsystem.components.SteamPuffCelebration
import com.cooked.app.domain.model.Personality
import com.cooked.app.feature.settings.components.PersonalityPreviewCard
import kotlin.math.roundToInt

@Composable
fun OnboardingScreen(
    uiState: OnboardingUiState,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    onPersonalitySelected: (Personality) -> Unit,
    onFirstWinInputChanged: (String) -> Unit,
    onSaveFirstWin: (String) -> Unit,
    onRefreshPermissions: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) {
            onComplete()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onRefreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CookedTheme.colors.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Progress dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(uiState.totalSteps) { index ->
                        val isActive = index == uiState.currentStep
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (isActive) 24.dp else 6.dp)
                                .background(
                                    if (isActive) CookedTheme.colors.primary else CookedTheme.colors.outlineVariant,
                                    CircleShape
                                )
                        )
                    }
                }

                // Skip CTA
                if (!uiState.isLastStep) {
                    TextButton(onClick = onSkip) {
                        Text(
                            text = "Skip",
                            style = CookedTheme.typography.labelMedium,
                            color = CookedTheme.colors.onSurfaceVariant
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            when (uiState.currentStep) {
                0 -> HookStep(onNext = onNext)
                1 -> PersonalityStep(
                    selectedPersonality = uiState.selectedPersonality,
                    onSelect = onPersonalitySelected,
                    onNext = onNext
                )
                2 -> FirstWinStep(
                    input = uiState.firstWinInput,
                    isCreated = uiState.firstWinCreated,
                    onInputChanged = onFirstWinInputChanged,
                    onSave = onSaveFirstWin,
                    onNext = onNext
                )
                3 -> PermissionsStep(
                    notificationsGranted = uiState.notificationsGranted,
                    exactAlarmsGranted = uiState.exactAlarmsGranted,
                    batteryUnrestricted = uiState.batteryUnrestricted,
                    onRefreshPermissions = onRefreshPermissions,
                    onNext = onNext
                )
                4 -> PaywallSoftSellStep(
                    onComplete = onNext,
                    onMaybeLater = onSkip
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// STEP 1: HOOK ("Reminders that won't shut up")
// -----------------------------------------------------------------------------
@Composable
private fun HookStep(onNext: () -> Unit) {
    val bounceY = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        bounceY.animateTo(
            targetValue = -16f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Animated Cooked App Logo Emblem
            Box(
                modifier = Modifier
                    .offset { IntOffset(0, bounceY.value.roundToInt()) }
                    .size(96.dp),
                contentAlignment = Alignment.Center
            ) {
                CookedAppLogo(
                    size = 96.dp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Reminders that\nwon't shut up.",
                style = CookedTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    lineHeight = 42.sp
                ),
                color = CookedTheme.colors.onBackground,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Cooked nag-alerts you until you actually tap Done. Swiping it away does nothing. So you're never cooked.",
                style = CookedTheme.typography.bodyLarge,
                color = CookedTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CookedTheme.colors.primary,
                contentColor = CookedTheme.colors.onPrimary
            )
        ) {
            Text(
                text = "Let's not be cooked →",
                style = CookedTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

// -----------------------------------------------------------------------------
// STEP 2: PICK YOUR VIBE (MILD / SPICY / UNHINGED)
// -----------------------------------------------------------------------------
@Composable
private fun PersonalityStep(
    selectedPersonality: Personality,
    onSelect: (Personality) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Pick your vibe.",
                style = CookedTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                color = CookedTheme.colors.onBackground
            )
            Text(
                text = "How do you want Cooked to talk to you when nagging?",
                style = CookedTheme.typography.bodyLarge,
                color = CookedTheme.colors.onSurfaceVariant
            )

            // Personality Picker Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Personality.values().forEach { personality ->
                    val isSelected = personality == selectedPersonality
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable { onSelect(personality) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) CookedTheme.colors.primary else CookedTheme.colors.surfaceContainerHigh,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) CookedTheme.colors.primary else CookedTheme.colors.outlineVariant
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = personality.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = CookedTheme.typography.labelLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) CookedTheme.colors.onPrimary else CookedTheme.colors.onSurface
                            )
                        }
                    }
                }
            }

            // Live Notification Preview Card
            PersonalityPreviewCard(
                personality = selectedPersonality,
                sampleTitle = "Submit final project",
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CookedTheme.colors.primary,
                contentColor = CookedTheme.colors.onPrimary
            )
        ) {
            Text(
                text = "Sounds like me →",
                style = CookedTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

// -----------------------------------------------------------------------------
// STEP 3: FIRST WIN (Natural language inline add)
// -----------------------------------------------------------------------------
@Composable
private fun FirstWinStep(
    input: String,
    isCreated: Boolean,
    onInputChanged: (String) -> Unit,
    onSave: (String) -> Unit,
    onNext: () -> Unit
) {
    val samplePrompts = listOf(
        "Submit assignment Friday 5pm",
        "Pay rent 1st of month",
        "Drink water in 20m"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "What are you forgetting right now?",
                style = CookedTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                color = CookedTheme.colors.onBackground
            )
            Text(
                text = "Type it naturally or tap an example below to lock in your first win.",
                style = CookedTheme.typography.bodyLarge,
                color = CookedTheme.colors.onSurfaceVariant
            )

            OutlinedTextField(
                value = input,
                onValueChange = onInputChanged,
                placeholder = { Text("e.g. Call dentist tomorrow 11am") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (input.isNotBlank()) onSave(input) }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CookedTheme.colors.primary,
                    unfocusedBorderColor = CookedTheme.colors.outlineVariant
                )
            )

            // Suggestion chips
            Text(
                text = "Quick examples:",
                style = CookedTheme.typography.labelSmall,
                color = CookedTheme.colors.onSurfaceVariant
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                samplePrompts.forEach { prompt ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onInputChanged(prompt)
                                onSave(prompt)
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = CookedTheme.colors.surfaceContainerLow,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CookedTheme.colors.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_lightbulb_hint),
                                contentDescription = null,
                                tint = CookedTheme.colors.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = prompt,
                                style = CookedTheme.typography.bodyMedium,
                                color = CookedTheme.colors.onSurface
                            )
                        }
                    }
                }
            }

            if (isCreated) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = CookedTheme.colors.surfaceContainerHigh,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CookedTheme.colors.primary)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_sparkle_celebration),
                            contentDescription = null,
                            tint = CookedTheme.colors.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "First win locked in! You're 1% less cooked.",
                            style = CookedTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = CookedTheme.colors.onSurface
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                if (!isCreated && input.isNotBlank()) {
                    onSave(input)
                }
                onNext()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CookedTheme.colors.primary,
                contentColor = CookedTheme.colors.onPrimary
            )
        ) {
            Text(
                text = if (isCreated) "Continue →" else if (input.isNotBlank()) "Lock it in" else "I'll add later →",
                style = CookedTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

// -----------------------------------------------------------------------------
// STEP 4: MAKE IT RELIABLE (Permission explanations & requests)
// -----------------------------------------------------------------------------
@Composable
private fun PermissionsStep(
    notificationsGranted: Boolean,
    exactAlarmsGranted: Boolean,
    batteryUnrestricted: Boolean,
    onRefreshPermissions: () -> Unit,
    onNext: () -> Unit
) {
    val context = LocalContext.current

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        onRefreshPermissions()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Make it reliable.",
                style = CookedTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                color = CookedTheme.colors.onBackground
            )
            Text(
                text = "Android aggressively silences background apps. Here is why each permission matters:",
                style = CookedTheme.typography.bodyLarge,
                color = CookedTheme.colors.onSurfaceVariant
            )

            // Permission 1: Notifications
            PermissionExplainRow(
                title = "Notifications",
                reason = "So Cooked can alert you with nag banners when it's time.",
                isGranted = notificationsGranted,
                actionLabel = "Allow",
                onRequest = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            )

            // Permission 2: Exact Alarms
            PermissionExplainRow(
                title = "Exact Alarms",
                reason = "Fires on the exact minute, not whenever the OS feels like waking up.",
                isGranted = exactAlarmsGranted,
                actionLabel = "Enable",
                onRequest = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        try {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                    Uri.parse("package:${context.packageName}")
                                )
                            )
                        } catch (_: Exception) {}
                    }
                }
            )

            // Permission 3: Battery Optimization Exemption
            PermissionExplainRow(
                title = "Battery Unrestricted",
                reason = "Stops aggressive battery savers from killing alarms while screen is off.",
                isGranted = batteryUnrestricted,
                actionLabel = "Set Unrestricted",
                onRequest = {
                    try {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:${context.packageName}")
                            )
                        )
                    } catch (_: Exception) {}
                }
            )
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CookedTheme.colors.primary,
                contentColor = CookedTheme.colors.onPrimary
            )
        ) {
            Text(
                text = "Lock in reliability →",
                style = CookedTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun PermissionExplainRow(
    title: String,
    reason: String,
    isGranted: Boolean,
    actionLabel: String,
    onRequest: () -> Unit
) {
    CookedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = CookedTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = CookedTheme.colors.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = reason,
                    style = CookedTheme.typography.bodySmall,
                    color = CookedTheme.colors.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            if (isGranted) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(CookedTheme.colors.surfaceContainerHigh, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = CookedTheme.colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onRequest,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(text = actionLabel, fontSize = 12.sp)
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// STEP 5: PAYWALL SOFT-SELL ("Never block the app")
// -----------------------------------------------------------------------------
@Composable
private fun PaywallSoftSellStep(
    onComplete: () -> Unit,
    onMaybeLater: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(CookedTheme.colors.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_premium_crown),
                    contentDescription = null,
                    tint = CookedTheme.colors.primary,
                    modifier = Modifier.size(44.dp)
                )
            }

            Text(
                text = "Become Uncookable.",
                style = CookedTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                color = CookedTheme.colors.onBackground,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Cooked Pro is for serious students & freelancers who cannot afford to miss a thing.",
                style = CookedTheme.typography.bodyLarge,
                color = CookedTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            CookedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ProPerkRow(R.drawable.ic_emergency_siren, "Escalating alarms (nag every 5m until done)")
                    ProPerkRow(R.drawable.ic_chart_analytics, "12-week historical Cooked Score analysis")
                    ProPerkRow(R.drawable.ic_cat_lightning, "Full-screen emergency alarm over lock screen")
                    ProPerkRow(R.drawable.ic_cat_creative, "Custom app icons and unhinged voice packs")
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CookedTheme.colors.primary,
                    contentColor = CookedTheme.colors.onPrimary
                )
            ) {
                Text(
                    text = "Start with Pro Free Trial →",
                    style = CookedTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            TextButton(
                onClick = onMaybeLater,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Maybe later (Start free)",
                    style = CookedTheme.typography.labelLarge,
                    color = CookedTheme.colors.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProPerkRow(
    @androidx.annotation.DrawableRes iconRes: Int,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = CookedTheme.colors.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = CookedTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = CookedTheme.colors.onSurface
        )
    }
}
