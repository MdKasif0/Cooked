package com.cooked.app.feature.onboarding

import com.cooked.app.core.designsystem.components.softShadow
import com.cooked.app.feature.onboarding.components.MakeItReliableContent
import com.cooked.app.feature.onboarding.components.OnboardingProgressHeader
import com.cooked.app.feature.onboarding.components.PickYourVibeContent
import com.cooked.app.feature.onboarding.components.QuickCaptureContent

import androidx.compose.ui.draw.drawBehind

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
            OnboardingProgressHeader(
                currentStep = uiState.currentStep,
                totalSteps = uiState.totalSteps,
                onSkip = onSkip,
                showSkip = !uiState.isLastStep
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (uiState.currentStep) {
                0 -> Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
                    HookStep(onNext = onNext)
                }
                1 -> PersonalityStep(
                    selectedPersonality = uiState.selectedPersonality,
                    onSelect = onPersonalitySelected,
                    onNext = onNext
                )
                2 -> QuickCaptureContent(
                    input = uiState.firstWinInput,
                    isCreated = uiState.firstWinCreated,
                    isSaving = uiState.isSavingFirstWin,
                    errorMessage = uiState.firstWinError,
                    onInputChanged = onFirstWinInputChanged,
                    onSaveAndNext = { text ->
                        onSaveFirstWin(text)
                        onNext()
                    },
                    onSkipLater = onNext
                )
                3 -> PermissionsStep(
                    notificationsGranted = uiState.notificationsGranted,
                    exactAlarmsGranted = uiState.exactAlarmsGranted,
                    batteryUnrestricted = uiState.batteryUnrestricted,
                    onRefreshPermissions = onRefreshPermissions,
                    onNext = onNext
                )
                4 -> Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
                    PaywallSoftSellStep(
                        onComplete = onNext,
                        onMaybeLater = onSkip
                    )
                }
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
            val glow = CookedTheme.colors.accent
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .drawBehind {
                        // Soft volt halo behind the emblem
                        drawCircle(
                            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                colors = listOf(glow.copy(alpha = 0.45f), glow.copy(alpha = 0.10f), androidx.compose.ui.graphics.Color.Transparent)
                            ),
                            radius = size.minDimension / 2f
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .offset { IntOffset(0, bounceY.value.roundToInt()) }
                        .size(112.dp)
                        .softShadow(androidx.compose.foundation.shape.RoundedCornerShape(26.dp), 24.dp, 0.25f),
                    contentAlignment = Alignment.Center
                ) {
                    CookedAppLogo(size = 112.dp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Reminders that\nwon't shut up.",
                style = CookedTheme.typography.headlineLarge.copy(fontSize = 38.sp, lineHeight = 42.sp),
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
            shape = RoundedCornerShape(24.dp),
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
    PickYourVibeContent(
        selectedPersonality = selectedPersonality,
        onSelectPersonality = onSelect,
        onNext = onNext
    )
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
    MakeItReliableContent(
        notificationsGranted = notificationsGranted,
        exactAlarmsGranted = exactAlarmsGranted,
        batteryUnrestricted = batteryUnrestricted,
        onRefreshPermissions = onRefreshPermissions,
        onNext = onNext
    )
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
                style = CookedTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
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
                shape = RoundedCornerShape(24.dp),
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
