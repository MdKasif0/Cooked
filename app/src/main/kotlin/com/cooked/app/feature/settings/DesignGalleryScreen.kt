package com.cooked.app.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.GridOff
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.cooked.app.core.designsystem.CookedRadius
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.FontFeatureSettingsTabular
import com.cooked.app.core.designsystem.HeatLevel
import com.cooked.app.core.designsystem.ThemeMode
import com.cooked.app.core.designsystem.components.AvatarBubble
import com.cooked.app.core.designsystem.components.CookedBottomSheet
import com.cooked.app.core.designsystem.components.CookedCard
import com.cooked.app.core.designsystem.components.CookedSnackbar
import com.cooked.app.core.designsystem.components.CookedSwitch
import com.cooked.app.core.designsystem.components.CookedTopBar
import com.cooked.app.core.designsystem.components.CountdownText
import com.cooked.app.core.designsystem.components.EggMascot
import com.cooked.app.core.designsystem.components.EmptyState
import com.cooked.app.core.designsystem.components.GhostButton
import com.cooked.app.core.designsystem.components.HeatBar
import com.cooked.app.core.designsystem.components.HeatChip
import com.cooked.app.core.designsystem.components.PrimaryButton
import com.cooked.app.core.designsystem.components.ReminderRow
import com.cooked.app.core.designsystem.components.SecondaryButton
import com.cooked.app.core.designsystem.components.SectionHeader
import com.cooked.app.core.designsystem.components.SegmentedControl
import com.cooked.app.core.designsystem.components.ShimmerPlaceholder
import com.cooked.app.core.designsystem.components.StatTile
import com.cooked.app.feature.settings.components.DebugGridOverlay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignGalleryScreen(
    currentThemeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onNavigateBack: () -> Unit,
    onSeedData: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var switchState by remember { mutableStateOf(true) }
    var interactiveSegment by remember { mutableStateOf("Today") }
    var showSheet by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CookedTheme.colors.background,
        topBar = {
            CookedTopBar(
                title = "Design Gallery",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = CookedTheme.colors.onBackground
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
            verticalArrangement = Arrangement.spacedBy(CookedTheme.spacing.xl24),
            contentPadding = PaddingValues(bottom = 48.dp)
        ) {
            // Theme Mode Selector
            item {
                SectionHeader(title = "Theme Mode (Instant Switch)")
                SegmentedControl(
                    items = listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK),
                    selectedItem = currentThemeMode,
                    onItemSelected = onThemeModeChange,
                    labelProvider = { it.displayName },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Brand & Urgency Heat Tokens
            item {
                SectionHeader(title = "Heat Urgency Scale")
                Text(
                    text = "Semantic scale: Chill -> Warm -> Toasty -> Cooked + Done",
                    style = CookedTheme.typography.bodyMedium,
                    color = CookedTheme.colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(CookedTheme.spacing.s8))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HeatLevel.values().forEach { level ->
                        val colors = CookedTheme.colors.heatFor(level)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(CookedRadius.Chip))
                                .background(colors.container)
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(colors.color)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = level.displayName,
                                style = CookedTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.textColor
                            )
                        }
                    }
                }
            }

            // Core Color Palette Tokens
            item {
                SectionHeader(title = "Warm-Neutral Tokens")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ColorSwatchRow(
                        name = "Primary (Ember)",
                        color = CookedTheme.colors.primary,
                        textColor = CookedTheme.colors.onPrimary
                    )
                    ColorSwatchRow(
                        name = "Primary Container",
                        color = CookedTheme.colors.primaryContainer,
                        textColor = CookedTheme.colors.onPrimaryContainer
                    )
                    ColorSwatchRow(
                        name = "Background",
                        color = CookedTheme.colors.background,
                        textColor = CookedTheme.colors.onBackground,
                        showBorder = true
                    )
                    ColorSwatchRow(
                        name = "Surface",
                        color = CookedTheme.colors.surface,
                        textColor = CookedTheme.colors.onSurface,
                        showBorder = true
                    )
                    ColorSwatchRow(
                        name = "Surface Container",
                        color = CookedTheme.colors.surfaceContainer,
                        textColor = CookedTheme.colors.onSurface
                    )
                    ColorSwatchRow(
                        name = "Outline Variant",
                        color = CookedTheme.colors.outlineVariant,
                        textColor = CookedTheme.colors.onSurfaceVariant
                    )
                }
            }

            // Typography Scale
            item {
                SectionHeader(title = "Typography (Bricolage + Inter)")
                CookedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "displayLarge 48/52 • 02:45:10",
                            style = CookedTheme.typography.displayLarge
                        )
                        Text(
                            text = "headlineMedium 28/34 Headline",
                            style = CookedTheme.typography.headlineMedium
                        )
                        Text(
                            text = "titleLarge 22/28 Title Large",
                            style = CookedTheme.typography.titleLarge
                        )
                        Text(
                            text = "titleMedium 17/24 Title Medium Semibold",
                            style = CookedTheme.typography.titleMedium
                        )
                        Text(
                            text = "bodyLarge 16/24 Body Large. The quick brown fox jumps over the lazy dog.",
                            style = CookedTheme.typography.bodyLarge
                        )
                        Text(
                            text = "bodyMedium 14/20 Body Medium. Tabular figures keep countdown timers aligned.",
                            style = CookedTheme.typography.bodyMedium
                        )
                        Text(
                            text = "labelSmall 11/16 OVERLINE TRACKING",
                            style = CookedTheme.typography.labelSmall
                        )
                    }
                }
            }

            // Buttons
            item {
                SectionHeader(title = "Buttons (56dp, 14dp Radius, 0.97 Spring)")
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PrimaryButton(
                        text = "Primary Button (Ember)",
                        onClick = { snackbarMessage = "Tapped Primary Button!" },
                        modifier = Modifier.fillMaxWidth()
                    )
                    SecondaryButton(
                        text = "Secondary Button (Tonal / Outlined)",
                        onClick = { snackbarMessage = "Tapped Secondary Button!" },
                        modifier = Modifier.fillMaxWidth()
                    )
                    GhostButton(
                        text = "Ghost Button",
                        onClick = { snackbarMessage = "Tapped Ghost Button!" },
                        modifier = Modifier.fillMaxWidth()
                    )
                    PrimaryButton(
                        text = "Loading State",
                        onClick = {},
                        isLoading = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Heat Chips & Progress Bar
            item {
                SectionHeader(title = "Heat Chips & Progress")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HeatChip(level = HeatLevel.CHILL)
                    HeatChip(level = HeatLevel.WARM)
                    HeatChip(level = HeatLevel.TOASTY)
                    HeatChip(level = HeatLevel.COOKED)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HeatBar(progress = 0.25f, level = HeatLevel.CHILL)
                    HeatBar(progress = 0.50f, level = HeatLevel.WARM)
                    HeatBar(progress = 0.75f, level = HeatLevel.TOASTY)
                    HeatBar(progress = 0.95f, level = HeatLevel.COOKED)
                    HeatBar(progress = 1.0f, level = HeatLevel.DONE)
                }
            }

            // Countdown Texts & Stat Tiles
            item {
                SectionHeader(title = "Countdowns & Stat Tiles")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatTile(
                        value = "84",
                        label = "COOKED SCORE",
                        delta = "+12% this week",
                        deltaHeatLevel = HeatLevel.COOKED,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        value = "18",
                        label = "DONE TODAY",
                        delta = "Chill",
                        deltaHeatLevel = HeatLevel.DONE,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                CookedCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Next Deadline Countdown:",
                            style = CookedTheme.typography.titleMedium,
                            color = CookedTheme.colors.onSurface
                        )
                        CountdownText(
                            targetEpochMillis = System.currentTimeMillis() + 3_600_000L * 3 + 180_000L,
                            style = CookedTheme.typography.headlineMedium,
                            color = CookedTheme.colors.toasty.textColor
                        )
                    }
                }
            }

            // Switch & Segmented Control
            item {
                SectionHeader(title = "Controls & Switches")
                CookedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SegmentedControl(
                            items = listOf("Today", "Deadlines", "Insights"),
                            selectedItem = interactiveSegment,
                            onItemSelected = { interactiveSegment = it },
                            labelProvider = { it },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Nag until done notifications",
                                style = CookedTheme.typography.titleMedium,
                                color = CookedTheme.colors.onSurface
                            )
                            CookedSwitch(
                                checked = switchState,
                                onCheckedChange = { switchState = it }
                            )
                        }
                    }
                }
            }

            // Reminder Rows (Swipe right to Done, Swipe left to Snooze)
            item {
                SectionHeader(title = "Interactive Reminder Rows (Swipe L/R)")
                Text(
                    text = "Swipe right ➔ Done (green reveal) | Swipe left ➔ Snooze (warm reveal)",
                    style = CookedTheme.typography.bodySmall,
                    color = CookedTheme.colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ReminderRow(
                        title = "Linear Algebra Problem Set 3",
                        timeText = "Due in 42m",
                        heatLevel = HeatLevel.COOKED,
                        onDone = { snackbarMessage = "Completed Problem Set 3! 🔥" },
                        onSnooze = { snackbarMessage = "Snoozed for 10 minutes ⏰" }
                    )
                    ReminderRow(
                        title = "Tax Filing Declaration",
                        timeText = "Due in 2d 6h",
                        heatLevel = HeatLevel.TOASTY,
                        onDone = { snackbarMessage = "Done with Taxes!" },
                        onSnooze = { snackbarMessage = "Snoozed Taxes ⏰" }
                    )
                    ReminderRow(
                        title = "Send draft to editor",
                        timeText = "Due Friday 5:00 PM",
                        heatLevel = HeatLevel.WARM,
                        onDone = { snackbarMessage = "Draft sent!" },
                        onSnooze = { snackbarMessage = "Snoozed ⏰" }
                    )
                }
            }

            // Avatar Bubbles & Shimmer
            item {
                SectionHeader(title = "Avatar Bubbles & Skeletons")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvatarBubble(initials = "MK")
                    AvatarBubble(initials = "JD")
                    AvatarBubble(initials = null)
                    AvatarBubble(initials = "C", size = 48.dp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(0.5f), height = 20.dp)
                    ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(), height = 48.dp, shape = CookedTheme.shapes.medium)
                }
            }

            // Empty State Illustration
            item {
                SectionHeader(title = "Empty State")
                CookedCard(modifier = Modifier.fillMaxWidth()) {
                    EmptyState(
                        title = "Nothing due. Suspiciously calm.",
                        message = "Enjoy the quiet before the heat ramps up.",
                        actionButtonText = "+ Quick Reminder",
                        onActionClick = { showSheet = true }
                    )
                }
            }

            // Interactive Bottom Sheet Trigger
            item {
                SectionHeader(title = "Modal Bottom Sheet (28dp Radius)")
                PrimaryButton(
                    text = "Open Sample Bottom Sheet",
                    onClick = { showSheet = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Debug Data Seeder Section
            item {
                SectionHeader(title = "Debug Seed Data")
                Text(
                    text = "Seed 30 realistic items across past 60 days and next 30 days with events, checklists, and stats for charts.",
                    style = CookedTheme.typography.bodySmall,
                    color = CookedTheme.colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                PrimaryButton(
                    text = "Seed 30 Realistic Items 🔥",
                    onClick = {
                        onSeedData?.invoke()
                        snackbarMessage = "Successfully seeded 30 items with events!"
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Floating Snackbar
    if (snackbarMessage != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            CookedSnackbar(
                message = snackbarMessage ?: "",
                actionLabel = "DISMISS",
                onActionClick = { snackbarMessage = null }
            )
        }
    }

    // Sample Bottom Sheet
    if (showSheet) {
        CookedBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(CookedTheme.spacing.xl24),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Cooked Bottom Sheet",
                    style = CookedTheme.typography.titleLarge,
                    color = CookedTheme.colors.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Styled with 28dp top corner radius, warm-neutral container, and custom pill drag handle.",
                    style = CookedTheme.typography.bodyMedium,
                    color = CookedTheme.colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                PrimaryButton(
                    text = "Looks Clean",
                    onClick = {
                        coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                            showSheet = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ColorSwatchRow(
    name: String,
    color: Color,
    textColor: Color,
    showBorder: Boolean = false
) {
    val borderModifier = if (showBorder) {
        Modifier.border(1.dp, CookedTheme.colors.outlineVariant, RoundedCornerShape(8.dp))
    } else {
        Modifier
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .then(borderModifier)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = CookedTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = textColor
        )
        Text(
            text = "#${Integer.toHexString(color.value.toInt()).uppercase().takeLast(6)}",
            style = CookedTheme.typography.labelSmall.copy(
                fontFeatureSettings = FontFeatureSettingsTabular
            ),
            color = textColor
        )
    }
}

@PreviewLightDark
@Preview(fontScale = 1.5f)
@Composable
private fun DesignGalleryScreenPreview() {
    CookedTheme {
        DesignGalleryScreen(
            currentThemeMode = ThemeMode.SYSTEM,
            onThemeModeChange = {},
            onNavigateBack = {}
        )
    }
}
