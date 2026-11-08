package com.cooked.app.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.BatteryAlert
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.cooked.app.R
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.ThemeMode
import com.cooked.app.core.designsystem.components.CookedCard
import com.cooked.app.core.designsystem.components.CookedTopBar
import com.cooked.app.core.designsystem.components.SectionHeader
import com.cooked.app.core.designsystem.components.SegmentedControl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onNavigateBack: () -> Unit,
    onNavigateToOemGuide: () -> Unit,
    onNavigateToDesignGallery: () -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onNagIntervalSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CookedTheme.colors.background,
        topBar = {
            CookedTopBar(
                title = "Settings",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
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
            verticalArrangement = Arrangement.spacedBy(CookedTheme.spacing.l16),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Appearance / Theme Section
            item {
                SectionHeader(title = "Appearance")
                CookedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Theme",
                            style = CookedTheme.typography.titleMedium,
                            color = CookedTheme.colors.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Choose between warm light, deep dark, or follow device settings.",
                            style = CookedTheme.typography.bodyMedium,
                            color = CookedTheme.colors.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        SegmentedControl(
                            items = listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK),
                            selectedItem = uiState.themeMode,
                            onItemSelected = onThemeModeSelected,
                            labelProvider = { it.displayName },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Nag Frequency Section
            item {
                SectionHeader(title = "Notifications & Nagging")
                CookedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.NotificationsActive,
                                contentDescription = null,
                                tint = CookedTheme.colors.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Default Nag Frequency",
                                style = CookedTheme.typography.titleMedium,
                                color = CookedTheme.colors.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Cooked re-alerts you until you mark a reminder Done.",
                            style = CookedTheme.typography.bodyMedium,
                            color = CookedTheme.colors.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(5, 10, 15, 30).forEach { mins ->
                                val selected = uiState.defaultNagIntervalMinutes == mins
                                FilterChip(
                                    selected = selected,
                                    onClick = { onNagIntervalSelected(mins) },
                                    label = { Text("${mins}m") },
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

            // OEM Battery Protection Section
            item {
                SectionHeader(title = "Device Reliability")
                CookedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateToOemGuide
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.BatteryAlert,
                                contentDescription = null,
                                tint = CookedTheme.colors.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "OEM Alarm Defense",
                                    style = CookedTheme.typography.titleMedium,
                                    color = CookedTheme.colors.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Whitelist Cooked from aggressive battery killers",
                                    style = CookedTheme.typography.bodyMedium,
                                    color = CookedTheme.colors.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = CookedTheme.colors.onSurfaceVariant
                        )
                    }
                }
            }

            // Developer / Design Gallery Section
            item {
                SectionHeader(title = "Developer")
                CookedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateToDesignGallery
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Palette,
                                contentDescription = null,
                                tint = CookedTheme.colors.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Design Gallery",
                                    style = CookedTheme.typography.titleMedium,
                                    color = CookedTheme.colors.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Interactive token showcase & design system components",
                                    style = CookedTheme.typography.bodyMedium,
                                    color = CookedTheme.colors.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = CookedTheme.colors.onSurfaceVariant
                        )
                    }
                }
            }

            // App Version Info
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Cooked v${uiState.appVersion} • so you're not cooked.",
                        style = CookedTheme.typography.bodyMedium,
                        color = CookedTheme.colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Preview(fontScale = 1.5f)
@Composable
private fun SettingsScreenPreview() {
    CookedTheme {
        SettingsScreen(
            uiState = SettingsUiState(),
            onNavigateBack = {},
            onNavigateToOemGuide = {},
            onNavigateToDesignGallery = {},
            onThemeModeSelected = {},
            onNagIntervalSelected = {}
        )
    }
}
