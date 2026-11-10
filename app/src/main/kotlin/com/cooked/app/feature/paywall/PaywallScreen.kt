package com.cooked.app.feature.paywall

import com.cooked.app.R
import com.cooked.app.core.designsystem.components.softShadow

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import com.cooked.app.core.designsystem.components.EggMascot
import com.cooked.app.core.designsystem.HeatLevel
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.components.CookedCard
import com.cooked.app.domain.billing.SubscriptionPlan

@Composable
fun PaywallScreen(
    uiState: PaywallUiState,
    onNavigateBack: () -> Unit,
    onPlanSelected: (String) -> Unit,
    onPurchase: (Activity) -> Unit,
    onRestore: () -> Unit,
    onClearMessages: () -> Unit,
    onSetTermsVisible: (Boolean) -> Unit,
    onSetPrivacyVisible: (Boolean) -> Unit,
    onClaimRewardedProPass: ((Activity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        val error = uiState.errorMessage
        val success = uiState.successMessage
        if (error != null) {
            snackbarHostState.showSnackbar(error)
            onClearMessages()
        } else if (success != null) {
            snackbarHostState.showSnackbar(success)
            onClearMessages()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CookedTheme.colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = CookedTheme.colors.onBackground
                    )
                }

                TextButton(onClick = onRestore, enabled = !uiState.isRestoring) {
                    if (uiState.isRestoring) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = CookedTheme.colors.primary
                        )
                    } else {
                        Text(
                            text = "Restore",
                            style = CookedTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = CookedTheme.colors.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. HERO HEADER
            item {
                com.cooked.app.core.designsystem.components.HeroCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    contentPadding = 24.dp
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        com.cooked.app.core.designsystem.components.AccentBadge(text = "Cooked Pro")
                        Spacer(modifier = Modifier.weight(1f))
                        EggMascot(level = HeatLevel.COOKED, size = 64.dp)
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = uiState.headline,
                        style = CookedTheme.typography.headlineLarge,
                        color = CookedTheme.colors.onHero
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = uiState.subheadline,
                        style = CookedTheme.typography.bodyMedium,
                        color = CookedTheme.colors.onHeroMuted
                    )
                }
            }

            // 2. 4 PRO BENEFIT ROWS
            item {
                Text(
                    text = "What's included",
                    style = CookedTheme.typography.titleLarge,
                    color = CookedTheme.colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            items(uiState.benefits) { benefit ->
                BenefitCard(benefit = benefit)
            }

            // 3. PLAN PICKER
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Choose your plan",
                    style = CookedTheme.typography.titleLarge,
                    color = CookedTheme.colors.onSurfaceVariant
                )
            }

            items(uiState.availablePlans) { plan ->
                val isSelected = plan.productId == uiState.selectedPlanId
                PlanSelectionCard(
                    plan = plan,
                    isSelected = isSelected,
                    onSelect = { onPlanSelected(plan.productId) }
                )
            }

            // 4. CALL TO ACTION & TRANSPARENCY
            item {
                val selectedPlan = uiState.availablePlans.firstOrNull { it.productId == uiState.selectedPlanId }
                val ctaText = if (selectedPlan?.freeTrialDays ?: 0 > 0) {
                    "Start ${selectedPlan?.freeTrialDays}-Day Free Trial"
                } else if (selectedPlan?.isLifetime == true) {
                    "Unlock Cooked Pro Forever"
                } else {
                    "Upgrade to Cooked Pro"
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = { activity?.let { onPurchase(it) } },
                        enabled = !uiState.isPurchasing && activity != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CookedTheme.colors.accent,
                            contentColor = CookedTheme.colors.onAccent
                        ),
                        shape = com.cooked.app.core.designsystem.PillShape
                    ) {
                        if (uiState.isPurchasing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = CookedTheme.colors.onAccent,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = ctaText,
                                style = CookedTheme.typography.titleMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (!uiState.isPro && onClaimRewardedProPass != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(CookedTheme.colors.surfaceContainer)
                                .border(1.dp, CookedTheme.colors.outlineVariant, RoundedCornerShape(16.dp))
                                .clickable { activity?.let { onClaimRewardedProPass(it) } }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(CookedTheme.colors.surfaceContainerHigh),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_gift_box),
                                        contentDescription = null,
                                        tint = CookedTheme.colors.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Claim 24-Hour Pro Pass",
                                        style = CookedTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = CookedTheme.colors.onSurface
                                    )
                                    Text(
                                        text = "Watch a quick ad to unlock all Pro features free",
                                        style = CookedTheme.typography.bodySmall,
                                        color = CookedTheme.colors.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(CookedTheme.colors.primary)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Watch Ad",
                                    style = CookedTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = CookedTheme.colors.onPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    Text(
                        text = "Cancel anytime in Google Play Subscriptions. No charge if canceled during trial.",
                        style = CookedTheme.typography.bodySmall,
                        color = CookedTheme.colors.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Terms of Service",
                            style = CookedTheme.typography.labelMedium,
                            color = CookedTheme.colors.onSurfaceVariant,
                            modifier = Modifier.clickable { onSetTermsVisible(true) }
                        )

                        Text(
                            text = "•",
                            style = CookedTheme.typography.labelMedium,
                            color = CookedTheme.colors.outline
                        )

                        Text(
                            text = "Privacy Policy",
                            style = CookedTheme.typography.labelMedium,
                            color = CookedTheme.colors.onSurfaceVariant,
                            modifier = Modifier.clickable { onSetPrivacyVisible(true) }
                        )
                    }
                }
            }
        }
    }

    // Terms of Service Dialog
    if (uiState.showTermsDialog) {
        AlertDialog(
            onDismissRequest = { onSetTermsVisible(false) },
            title = { Text("Terms of Service") },
            text = {
                Text(
                    text = "1. Subscriptions renew automatically unless canceled at least 24 hours before the end of the current billing cycle.\n\n" +
                        "2. Payment is charged directly via your Google Play account.\n\n" +
                        "3. Subscriptions can be managed or canceled anytime in Google Play > Subscriptions.\n\n" +
                        "4. Lifetime purchases grant perpetual access to all current and future Cooked Pro features."
                )
            },
            confirmButton = {
                TextButton(onClick = { onSetTermsVisible(false) }) {
                    Text("Close")
                }
            }
        )
    }

    // Privacy Policy Dialog
    if (uiState.showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { onSetPrivacyVisible(false) },
            title = { Text("Privacy Policy") },
            text = {
                Text(
                    text = "Cooked is completely offline-first.\n\n" +
                        "• All reminders, deadlines, and notes stay strictly on your device.\n" +
                        "• Billing is managed entirely by Google Play Billing via RevenueCat.\n" +
                        "• Zero task titles or personal notes are ever transmitted to any remote servers."
                )
            },
            confirmButton = {
                TextButton(onClick = { onSetPrivacyVisible(false) }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun BenefitCard(
    benefit: PaywallBenefit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (benefit.isHighlighted) CookedTheme.colors.primary else Color.Transparent
    val borderWidth = if (benefit.isHighlighted) 2.dp else 0.dp

    CookedCard(
        modifier = modifier
            .fillMaxWidth()
            .border(borderWidth, borderColor, RoundedCornerShape(24.dp))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(CookedTheme.colors.surfaceContainerHigh, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = benefit.iconRes),
                    contentDescription = null,
                    tint = CookedTheme.colors.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = benefit.title,
                        style = CookedTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = CookedTheme.colors.onSurface
                    )

                    if (benefit.isHighlighted) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CookedTheme.colors.primary)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "LOCKED",
                                style = CookedTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                ),
                                color = CookedTheme.colors.onPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = benefit.description,
                    style = CookedTheme.typography.bodySmall,
                    color = CookedTheme.colors.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PlanSelectionCard(
    plan: SubscriptionPlan,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = CookedTheme.colors
    val containerColor = if (isSelected) colors.hero else if (colors.isDark) colors.surfaceContainer else colors.surface
    val titleColor = if (isSelected) colors.onHero else colors.onSurface
    val mutedColor = if (isSelected) colors.onHeroMuted else colors.onSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(if (isSelected) Modifier else Modifier.softShadow(RoundedCornerShape(24.dp), 14.dp, 0.06f))
            .clip(RoundedCornerShape(24.dp))
            .background(containerColor)
            .clickable(onClick = onSelect)
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isSelected) colors.accent else colors.outline,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = plan.title,
                        style = CookedTheme.typography.titleMedium,
                        color = titleColor
                    )

                    if (plan.isBestValue) {
                        com.cooked.app.core.designsystem.components.AccentBadge(text = "Best value")
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = plan.subtitle,
                    style = CookedTheme.typography.bodySmall,
                    color = mutedColor
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = plan.formattedPrice,
                style = CookedTheme.typography.titleLarge,
                color = titleColor
            )
        }
    }
}
