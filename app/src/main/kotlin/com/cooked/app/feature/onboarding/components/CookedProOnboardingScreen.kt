package com.cooked.app.feature.onboarding.components

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cooked.app.R
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.PillShape
import com.cooked.app.core.designsystem.components.softShadow
import com.cooked.app.domain.billing.PlanPeriod
import com.cooked.app.domain.billing.SubscriptionPlan
import kotlin.math.roundToInt

// -----------------------------------------------------------------------------
// COOKED PRO BRAND DESIGN TOKENS
// -----------------------------------------------------------------------------
private object ProDesignTokens {
    val Background = Color(0xFF0B0C0A)
    val PrimarySurface = Color(0xFF141512)
    val SecondarySurface = Color(0xFF1B1D18)
    val ElevatedSurface = Color(0xFF22241E)
    val BorderSubtle = Color(0xFF292B25)
    val BorderSelected = Color(0xFFC6FF3D)

    val TextPrimary = Color(0xFFF5F5F0)
    val TextSecondary = Color(0xFFA2A49B)
    val TextMuted = Color(0xFF777A70)

    val AccentVolt = Color(0xFFC6FF3D)
    val AccentVoltPressed = Color(0xFFAEEA29)
    val SuccessGreen = Color(0xFF83D65A)

    val RadiusCard = 18.dp
    val RadiusPill = 999.dp
    val RadiusBadge = 8.dp
}

// -----------------------------------------------------------------------------
// VALUE PROPOSITION & FEATURE DEFINITIONS
// -----------------------------------------------------------------------------
data class ProPillar(
    val iconRes: Int,
    val title: String
)

data class ProFeatureItem(
    val id: String,
    val title: String,
    val description: String,
    val iconRes: Int
)

data class ProComparisonRow(
    val title: String,
    val freeText: String,
    val proText: String,
    val isProHighlight: Boolean = true
)

private val DefaultPillars = listOf(
    ProPillar(R.drawable.ic_cat_lightning, "More accountability"),
    ProPillar(R.drawable.ic_cat_target, "Hit your deadlines"),
    ProPillar(R.drawable.ic_chart_analytics, "Real insights"),
    ProPillar(R.drawable.ic_pro_crown, "A more personal Cooked")
)

private val DefaultFeatures = listOf(
    ProFeatureItem(
        id = "escalating",
        title = "Escalating Accountability",
        description = "Configurable reminder escalation until you take action.",
        iconRes = R.drawable.ic_cat_bell
    ),
    ProFeatureItem(
        id = "insights",
        title = "12-Week Productivity Insights",
        description = "Understand your consistency, procrastination patterns and deadline performance.",
        iconRes = R.drawable.ic_chart_analytics
    ),
    ProFeatureItem(
        id = "alerts",
        title = "Advanced Deadline Alerts",
        description = "Enhanced reminder experiences for your most important deadlines.",
        iconRes = R.drawable.ic_cat_lightning
    ),
    ProFeatureItem(
        id = "personalities",
        title = "Exclusive Personalities",
        description = "Unlock additional reminder personalities, custom voices and premium app customization.",
        iconRes = R.drawable.ic_cat_creative
    )
)

private val DefaultComparisons = listOf(
    ProComparisonRow("Reminders", "Basic reminders", "Escalating persistent nags"),
    ProComparisonRow("Deadlines", "Standard tracking", "Advanced multi-alert countdowns"),
    ProComparisonRow("Analytics", "Basic summary", "12-week historical Cooked Score"),
    ProComparisonRow("Personalities", "Standard vibe", "Exclusive unhinged voices & icons")
)

// -----------------------------------------------------------------------------
// MAIN COMPOSABLE: CookedProOnboardingContent
// -----------------------------------------------------------------------------
@Composable
fun CookedProOnboardingContent(
    availablePlans: List<SubscriptionPlan>,
    selectedPlanId: String,
    isPurchasing: Boolean,
    isRestoring: Boolean,
    isPro: Boolean,
    errorMessage: String?,
    successMessage: String?,
    showTermsDialog: Boolean,
    showPrivacyDialog: Boolean,
    onPlanSelected: (String) -> Unit,
    onPurchase: (Activity) -> Unit,
    onRestore: () -> Unit,
    onClearMessages: () -> Unit,
    onSetTermsVisible: (Boolean) -> Unit,
    onSetPrivacyVisible: (Boolean) -> Unit,
    onMaybeLater: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage, successMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
            onClearMessages()
        } else if (successMessage != null) {
            snackbarHostState.showSnackbar(successMessage)
            onClearMessages()
        }
    }

    // Determine selected plan or best-value default
    val effectivePlans = remember(availablePlans) {
        if (availablePlans.isNotEmpty()) availablePlans else defaultMockPlans()
    }
    val currentSelectedId = remember(selectedPlanId, effectivePlans) {
        if (selectedPlanId.isNotEmpty() && effectivePlans.any { it.productId == selectedPlanId }) {
            selectedPlanId
        } else {
            effectivePlans.firstOrNull { it.isBestValue }?.productId
                ?: effectivePlans.firstOrNull()?.productId
                ?: ""
        }
    }
    val selectedPlan = effectivePlans.firstOrNull { it.productId == currentSelectedId }

    // Monthly & Yearly plan calculation
    val monthlyPlan = effectivePlans.firstOrNull { it.period == PlanPeriod.MONTHLY }
    val yearlyPlan = effectivePlans.firstOrNull { it.period == PlanPeriod.YEARLY }

    // Dynamic CTA Label
    val hasTrial = (selectedPlan?.freeTrialDays ?: 0) > 0
    val ctaLabel = when {
        isPro -> "You're already Cooked Pro →"
        hasTrial -> "Start ${selectedPlan?.freeTrialDays}-Day Free Trial →"
        selectedPlan?.isLifetime == true -> "Unlock Cooked Pro Forever →"
        else -> "Unlock Cooked Pro →"
    }

    // Dynamic Billing Disclosure
    val disclosureText = when {
        hasTrial && selectedPlan != null -> {
            val priceUnit = if (selectedPlan.period == PlanPeriod.YEARLY) "year" else "month"
            "${selectedPlan.freeTrialDays} days free, then ${selectedPlan.formattedPrice.replace(" / year", "").replace("/year", "")}/$priceUnit. Cancel anytime on Google Play."
        }
        selectedPlan?.isLifetime == true -> "One-time payment. Lifetime access. Cancel anytime."
        else -> "Auto-renews until cancelled. Manage anytime on Google Play."
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(ProDesignTokens.Background),
        containerColor = ProDesignTokens.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            OnboardingProgressHeader(
                currentStep = 3,
                totalSteps = 4,
                onSkip = onMaybeLater,
                showSkip = true
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // 1. HERO SECTION
                ProHeroSection()

                Spacer(modifier = Modifier.height(24.dp))

                // 2. 4 VALUE PROPOSITION PILLARS
                ProPillarsRow(pillars = DefaultPillars)

                Spacer(modifier = Modifier.height(28.dp))

                // 3. 4 PREVIEW FEATURE CARDS
                ProFeatureListSection(features = DefaultFeatures)

                Spacer(modifier = Modifier.height(28.dp))

                // 4. WHY GO PRO? (FREE VS PRO COMPARISON)
                ProComparisonSection(comparisons = DefaultComparisons)

                Spacer(modifier = Modifier.height(28.dp))

                // 5. SUBSCRIPTION PLAN SELECTION
                ProPlanSelectorSection(
                    plans = effectivePlans,
                    selectedPlanId = currentSelectedId,
                    onPlanSelected = onPlanSelected,
                    monthlyPlan = monthlyPlan,
                    yearlyPlan = yearlyPlan
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 6. WHY COOKED VALUE SUMMARY
                ProValueBenefitCard()

                // Space for floating bottom CTA bar
                Spacer(modifier = Modifier.height(200.dp))
            }

            // DOCKED BOTTOM CTA BAR WITH GRADIENT SCRIM
            ProDockedCtaBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                ctaLabel = ctaLabel,
                disclosureText = disclosureText,
                isPurchasing = isPurchasing,
                isRestoring = isRestoring,
                onPrimaryClick = {
                    if (activity != null && !isPurchasing) {
                        onPurchase(activity)
                    }
                },
                onMaybeLater = onMaybeLater,
                onRestore = onRestore,
                onTermsClick = { onSetTermsVisible(true) },
                onPrivacyClick = { onSetPrivacyVisible(true) }
            )
        }
    }

    // TERMS OF SERVICE DIALOG
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { onSetTermsVisible(false) },
            containerColor = ProDesignTokens.PrimarySurface,
            titleContentColor = ProDesignTokens.TextPrimary,
            textContentColor = ProDesignTokens.TextSecondary,
            title = {
                Text(
                    text = "Terms of Service",
                    style = CookedTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "1. Subscriptions renew automatically unless cancelled at least 24 hours before the end of the billing cycle.",
                        style = CookedTheme.typography.bodyMedium
                    )
                    Text(
                        text = "2. All payments are securely processed and managed by Google Play Billing.",
                        style = CookedTheme.typography.bodyMedium
                    )
                    Text(
                        text = "3. Free trial offers automatically convert to paid subscriptions at the stated price unless cancelled prior to trial expiration.",
                        style = CookedTheme.typography.bodyMedium
                    )
                    Text(
                        text = "4. You can manage or cancel your subscription at any time via Google Play > Payments & subscriptions.",
                        style = CookedTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { onSetTermsVisible(false) }) {
                    Text(
                        text = "Done",
                        color = ProDesignTokens.AccentVolt,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    // PRIVACY POLICY DIALOG
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { onSetPrivacyVisible(false) },
            containerColor = ProDesignTokens.PrimarySurface,
            titleContentColor = ProDesignTokens.TextPrimary,
            textContentColor = ProDesignTokens.TextSecondary,
            title = {
                Text(
                    text = "Privacy Policy",
                    style = CookedTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "• Offline-First: All task titles, reminders, and deadlines remain strictly stored on your device.",
                        style = CookedTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• Zero Personal Tracking: No personal notes, tasks, or calendar entries are transmitted to remote servers.",
                        style = CookedTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• Google Play Billing: In-app purchases are securely handled through standard Google Play APIs with zero stored payment details.",
                        style = CookedTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { onSetPrivacyVisible(false) }) {
                    Text(
                        text = "Done",
                        color = ProDesignTokens.AccentVolt,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

// -----------------------------------------------------------------------------
// 1. HERO SECTION: Crown Emblem, Glowing Halo, & Headline
// -----------------------------------------------------------------------------
@Composable
private fun ProHeroSection() {
    val floatOffset = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        floatOffset.animateTo(
            targetValue = -6f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Glowing Halo + Geometric Crown Badge
        Box(
            modifier = Modifier
                .size(130.dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                ProDesignTokens.AccentVolt.copy(alpha = 0.35f),
                                ProDesignTokens.AccentVolt.copy(alpha = 0.12f),
                                Color.Transparent
                            )
                        ),
                        radius = size.minDimension / 1.4f
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Little decorative sparkle in top-right
            Text(
                text = "✦",
                color = ProDesignTokens.AccentVolt,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-12).dp, y = 8.dp)
            )

            // Crown Badge Container
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .offset { IntOffset(0, floatOffset.value.roundToInt()) }
                    .size(92.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(ProDesignTokens.PrimarySurface)
                    .border(1.2.dp, ProDesignTokens.BorderSubtle, RoundedCornerShape(26.dp))
                    .padding(8.dp)
            ) {
                // Crown Vector Icon
                Icon(
                    painter = painterResource(id = R.drawable.ic_pro_crown),
                    contentDescription = "Cooked Pro Crown",
                    tint = ProDesignTokens.AccentVolt,
                    modifier = Modifier.size(38.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // "COOKED PRO" Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ProDesignTokens.SecondarySurface)
                        .border(0.8.dp, ProDesignTokens.BorderSubtle, RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "COOKED PRO",
                        style = CookedTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 8.5.sp,
                            letterSpacing = 1.1.sp
                        ),
                        color = ProDesignTokens.AccentVolt
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Headline with custom lime underline on "Uncookable."
        Text(
            text = buildAnnotatedString {
                append("Become ")
                withStyle(
                    SpanStyle(
                        color = ProDesignTokens.AccentVolt,
                        fontWeight = FontWeight.ExtraBold
                    )
                ) {
                    append("Uncookable.")
                }
            },
            style = CookedTheme.typography.headlineLarge.copy(
                fontSize = 32.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.Bold
            ),
            color = ProDesignTokens.TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.drawBehind {
                // Draw a sleek accent swoosh under "Uncookable."
                val textLength = size.width
                val startX = textLength * 0.44f
                val endX = textLength * 0.98f
                val strokeY = size.height + 4.dp.toPx()

                val path = Path().apply {
                    moveTo(startX, strokeY)
                    quadraticTo(
                        (startX + endX) / 2f,
                        strokeY + 3.dp.toPx(),
                        endX,
                        strokeY
                    )
                }
                drawPath(
                    path = path,
                    color = ProDesignTokens.AccentVolt,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Subtitle
        Text(
            text = "Cooked Pro is for people who are done making excuses and ready to get things done.",
            style = CookedTheme.typography.bodyMedium.copy(
                lineHeight = 20.sp
            ),
            color = ProDesignTokens.TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

// -----------------------------------------------------------------------------
// 2. 4 VALUE PROPOSITION PILLARS
// -----------------------------------------------------------------------------
@Composable
private fun ProPillarsRow(pillars: List<ProPillar>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ProDesignTokens.RadiusCard))
            .background(ProDesignTokens.PrimarySurface)
            .border(1.dp, ProDesignTokens.BorderSubtle, RoundedCornerShape(ProDesignTokens.RadiusCard))
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top
    ) {
        pillars.forEach { pillar ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(ProDesignTokens.SecondarySurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = pillar.iconRes),
                        contentDescription = null,
                        tint = ProDesignTokens.AccentVolt,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = pillar.title,
                    style = CookedTheme.typography.labelSmall.copy(
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 13.sp
                    ),
                    color = ProDesignTokens.TextSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 3. PREVIEW FEATURE ROWS
// -----------------------------------------------------------------------------
@Composable
private fun ProFeatureListSection(features: List<ProFeatureItem>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        features.forEach { feature ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ProDesignTokens.PrimarySurface)
                    .border(1.dp, ProDesignTokens.BorderSubtle, RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon Box
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ProDesignTokens.SecondarySurface)
                        .border(0.8.dp, ProDesignTokens.BorderSubtle, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = feature.iconRes),
                        contentDescription = null,
                        tint = ProDesignTokens.AccentVolt,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Text
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = feature.title,
                        style = CookedTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp
                        ),
                        color = ProDesignTokens.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = feature.description,
                        style = CookedTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        ),
                        color = ProDesignTokens.TextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Chevron
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = ProDesignTokens.TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 4. WHY GO PRO? (COMPARISON SECTION)
// -----------------------------------------------------------------------------
@Composable
private fun ProComparisonSection(comparisons: List<ProComparisonRow>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ProDesignTokens.RadiusCard))
            .background(ProDesignTokens.PrimarySurface)
            .border(1.dp, ProDesignTokens.BorderSubtle, RoundedCornerShape(ProDesignTokens.RadiusCard))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Why go Pro?",
                style = CookedTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = ProDesignTokens.TextPrimary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Free",
                    style = CookedTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = ProDesignTokens.TextMuted
                )
                Text(
                    text = "Pro",
                    style = CookedTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = ProDesignTokens.AccentVolt
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        comparisons.forEachIndexed { index, row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = row.title,
                    style = CookedTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = ProDesignTokens.TextPrimary,
                    modifier = Modifier.weight(1.1f)
                )

                // Free indicator
                Text(
                    text = row.freeText,
                    style = CookedTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = ProDesignTokens.TextMuted,
                    modifier = Modifier.weight(1.3f)
                )

                // Pro indicator
                Row(
                    modifier = Modifier.weight(1.6f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = ProDesignTokens.AccentVolt,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = row.proText,
                        style = CookedTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = ProDesignTokens.TextPrimary
                    )
                }
            }

            if (index < comparisons.size - 1) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.8.dp)
                        .background(ProDesignTokens.BorderSubtle.copy(alpha = 0.6f))
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 5. SUBSCRIPTION PLAN SELECTOR ("Choose your plan")
// -----------------------------------------------------------------------------
@Composable
private fun ProPlanSelectorSection(
    plans: List<SubscriptionPlan>,
    selectedPlanId: String,
    onPlanSelected: (String) -> Unit,
    monthlyPlan: SubscriptionPlan?,
    yearlyPlan: SubscriptionPlan?
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Section Header with Free Trial Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Choose your plan",
                style = CookedTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = ProDesignTokens.TextPrimary
            )

            val trialDays = yearlyPlan?.freeTrialDays ?: 0
            if (trialDays > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(ProDesignTokens.RadiusPill))
                        .background(ProDesignTokens.SecondarySurface)
                        .border(1.dp, ProDesignTokens.BorderSubtle, RoundedCornerShape(ProDesignTokens.RadiusPill))
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎁 ", fontSize = 11.sp)
                        Text(
                            text = "$trialDays-day free trial",
                            style = CookedTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = ProDesignTokens.AccentVolt
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Side-by-side or stacked plan cards
        val isNarrow = LocalConfiguration.current.screenWidthDp < 350
        if (isNarrow) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                plans.forEach { plan ->
                    PlanCardItem(
                        plan = plan,
                        isSelected = plan.productId == selectedPlanId,
                        onSelect = { onPlanSelected(plan.productId) },
                        monthlyPlan = monthlyPlan,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                plans.filter { !it.isLifetime }.forEach { plan ->
                    PlanCardItem(
                        plan = plan,
                        isSelected = plan.productId == selectedPlanId,
                        onSelect = { onPlanSelected(plan.productId) },
                        monthlyPlan = monthlyPlan,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Optional Lifetime purchase card if configured
            val lifetimePlan = plans.firstOrNull { it.isLifetime }
            if (lifetimePlan != null) {
                Spacer(modifier = Modifier.height(10.dp))
                PlanCardItem(
                    plan = lifetimePlan,
                    isSelected = lifetimePlan.productId == selectedPlanId,
                    onSelect = { onPlanSelected(lifetimePlan.productId) },
                    monthlyPlan = null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun PlanCardItem(
    plan: SubscriptionPlan,
    isSelected: Boolean,
    onSelect: () -> Unit,
    monthlyPlan: SubscriptionPlan?,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) ProDesignTokens.AccentVolt else ProDesignTokens.BorderSubtle,
        animationSpec = tween(200),
        label = "borderColor"
    )
    val borderWidth by animateFloatAsState(
        targetValue = if (isSelected) 1.8f else 1f,
        animationSpec = tween(200),
        label = "borderWidth"
    )

    // Compute monthly breakdown & savings for Yearly plan
    val monthlyEquivalent = remember(plan, monthlyPlan) {
        if (plan.period == PlanPeriod.YEARLY && plan.priceMicros > 0) {
            val monthlyMicros = plan.priceMicros / 12
            formatPriceFromMicros(monthlyMicros, plan.formattedPrice)
        } else null
    }

    val savingsPercent = remember(plan, monthlyPlan) {
        if (plan.period == PlanPeriod.YEARLY && monthlyPlan != null && monthlyPlan.priceMicros > 0) {
            val annualFromMonthly = monthlyPlan.priceMicros * 12
            if (annualFromMonthly > plan.priceMicros) {
                ((annualFromMonthly - plan.priceMicros) * 100 / annualFromMonthly).toInt()
            } else 0
        } else 0
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(ProDesignTokens.RadiusCard))
            .background(if (isSelected) ProDesignTokens.SecondarySurface else ProDesignTokens.PrimarySurface)
            .border(borderWidth.dp, borderColor, RoundedCornerShape(ProDesignTokens.RadiusCard))
            .clickable(onClick = onSelect)
            .padding(14.dp)
    ) {
        // "BEST VALUE" pill at top-right for Yearly
        if (plan.isBestValue || (plan.period == PlanPeriod.YEARLY && savingsPercent > 0)) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(6.dp))
                    .background(ProDesignTokens.AccentVolt)
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "BEST VALUE",
                    style = CookedTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 9.sp
                    ),
                    color = Color(0xFF0B0C0A)
                )
            }
        }

        Column {
            // Radio circle + Plan title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .border(
                            width = if (isSelected) 5.dp else 1.5.dp,
                            color = if (isSelected) ProDesignTokens.AccentVolt else ProDesignTokens.TextMuted,
                            shape = CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (plan.period) {
                        PlanPeriod.YEARLY -> "Yearly"
                        PlanPeriod.MONTHLY -> "Monthly"
                        PlanPeriod.LIFETIME -> "Lifetime"
                    },
                    style = CookedTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = ProDesignTokens.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Price Display
            Text(
                text = plan.formattedPrice,
                style = CookedTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                ),
                color = ProDesignTokens.TextPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Subtitle / Breakdown
            if (plan.period == PlanPeriod.YEARLY && monthlyEquivalent != null) {
                Text(
                    text = "$monthlyEquivalent / month" + if (savingsPercent > 0) " · $savingsPercent% off" else "",
                    style = CookedTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = ProDesignTokens.AccentVolt
                )
            } else {
                Text(
                    text = plan.subtitle,
                    style = CookedTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = ProDesignTokens.TextMuted
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 6. WHY COOKED VALUE SUMMARY CARD
// -----------------------------------------------------------------------------
@Composable
private fun ProValueBenefitCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ProDesignTokens.RadiusCard))
            .background(ProDesignTokens.PrimarySurface)
            .border(1.dp, ProDesignTokens.BorderSubtle, RoundedCornerShape(ProDesignTokens.RadiusCard))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(ProDesignTokens.SecondarySurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_alarm_clock),
                contentDescription = null,
                tint = ProDesignTokens.AccentVolt,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "More accountability. Less procrastination.",
                style = CookedTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = ProDesignTokens.TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Take control of your daily execution. Never let important tasks quietly disappear.",
                style = CookedTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 15.sp),
                color = ProDesignTokens.TextSecondary
            )
        }
    }
}

// -----------------------------------------------------------------------------
// 7. DOCKED BOTTOM CTA BAR
// -----------------------------------------------------------------------------
@Composable
private fun ProDockedCtaBar(
    ctaLabel: String,
    disclosureText: String,
    isPurchasing: Boolean,
    isRestoring: Boolean,
    onPrimaryClick: () -> Unit,
    onMaybeLater: () -> Unit,
    onRestore: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navInsets = WindowInsets.navigationBars.asPaddingValues()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "scaleCta"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        ProDesignTokens.Background.copy(alpha = 0.85f),
                        ProDesignTokens.Background.copy(alpha = 0.98f),
                        ProDesignTokens.Background
                    )
                )
            )
            .padding(
                top = 18.dp,
                start = 20.dp,
                end = 20.dp,
                bottom = navInsets.calculateBottomPadding() + 8.dp
            )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // PRIMARY ACTION BUTTON
            Button(
                onClick = onPrimaryClick,
                enabled = !isPurchasing,
                interactionSource = interactionSource,
                modifier = Modifier
                    .scale(scale)
                    .fillMaxWidth()
                    .height(56.dp)
                    .softShadow(RoundedCornerShape(28.dp), 16.dp, 0.2f),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ProDesignTokens.AccentVolt,
                    contentColor = Color(0xFF0B0C0A),
                    disabledContainerColor = ProDesignTokens.AccentVolt.copy(alpha = 0.5f),
                    disabledContentColor = Color(0xFF0B0C0A).copy(alpha = 0.6f)
                ),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                if (isPurchasing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color(0xFF0B0C0A),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text = ctaLabel,
                        style = CookedTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subtext Billing Disclosure
            Text(
                text = disclosureText,
                style = CookedTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                color = ProDesignTokens.TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // FREE VERSION OPTION
            Text(
                text = "Maybe later · Continue with Free",
                style = CookedTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                ),
                color = ProDesignTokens.TextSecondary,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onMaybeLater)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // TRUST, RESTORE & LEGAL FOOTER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Google Play Icon Badge
                Icon(
                    painter = painterResource(id = R.drawable.ic_google_play_badge),
                    contentDescription = null,
                    tint = ProDesignTokens.TextMuted,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Billed through Google Play",
                    style = CookedTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                    color = ProDesignTokens.TextMuted
                )

                Text(
                    text = "  |  ",
                    style = CookedTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                    color = ProDesignTokens.BorderSubtle
                )

                Text(
                    text = "Terms",
                    style = CookedTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                    color = ProDesignTokens.TextSecondary,
                    modifier = Modifier.clickable(onClick = onTermsClick)
                )

                Text(
                    text = "  |  ",
                    style = CookedTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                    color = ProDesignTokens.BorderSubtle
                )

                Text(
                    text = "Privacy",
                    style = CookedTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                    color = ProDesignTokens.TextSecondary,
                    modifier = Modifier.clickable(onClick = onPrivacyClick)
                )

                Text(
                    text = "  |  ",
                    style = CookedTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                    color = ProDesignTokens.BorderSubtle
                )

                if (isRestoring) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(10.dp),
                        strokeWidth = 1.5.dp,
                        color = ProDesignTokens.AccentVolt
                    )
                } else {
                    Text(
                        text = "Restore",
                        style = CookedTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                        color = ProDesignTokens.TextSecondary,
                        modifier = Modifier.clickable(onClick = onRestore)
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// HELPER METHODS
// -----------------------------------------------------------------------------
private fun formatPriceFromMicros(micros: Long, sampleFormatted: String): String {
    val currencySymbol = sampleFormatted.find { !it.isDigit() && it != '.' && it != ',' && it != ' ' } ?: '$'
    val amount = micros / 1_000_000.0
    return if (amount % 1.0 == 0.0) {
        "$currencySymbol${amount.toLong()}"
    } else {
        String.format("$currencySymbol%.2f", amount)
    }
}

private fun defaultMockPlans(): List<SubscriptionPlan> = listOf(
    SubscriptionPlan(
        productId = "cooked_pro_yearly",
        title = "Yearly Access",
        subtitle = "7-day free trial, then billed annually",
        formattedPrice = "₹1,499 / year",
        priceMicros = 1499000000L,
        period = PlanPeriod.YEARLY,
        freeTrialDays = 7,
        isBestValue = true,
        isLifetime = false
    ),
    SubscriptionPlan(
        productId = "cooked_pro_monthly",
        title = "Monthly",
        subtitle = "Billed monthly",
        formattedPrice = "₹199 / month",
        priceMicros = 199000000L,
        period = PlanPeriod.MONTHLY,
        freeTrialDays = 0,
        isBestValue = false,
        isLifetime = false
    )
)

// -----------------------------------------------------------------------------
// PREVIEWS
// -----------------------------------------------------------------------------
@Preview(showBackground = true, backgroundColor = 0xFF0B0C0A)
@Composable
private fun CookedProOnboardingPreview() {
    CookedTheme {
        CookedProOnboardingContent(
            availablePlans = defaultMockPlans(),
            selectedPlanId = "cooked_pro_yearly",
            isPurchasing = false,
            isRestoring = false,
            isPro = false,
            errorMessage = null,
            successMessage = null,
            showTermsDialog = false,
            showPrivacyDialog = false,
            onPlanSelected = {},
            onPurchase = {},
            onRestore = {},
            onClearMessages = {},
            onSetTermsVisible = {},
            onSetPrivacyVisible = {},
            onMaybeLater = {}
        )
    }
}
