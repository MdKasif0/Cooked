package com.cooked.app.feature.settings

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.HeatLevel
import com.cooked.app.core.designsystem.components.CookedCard
import com.cooked.app.core.designsystem.components.EggMascot
import com.cooked.app.core.designsystem.components.HeatBar
import com.cooked.app.core.designsystem.components.RollingNumberText
import com.cooked.app.feature.settings.components.DebugGridOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showGrid by remember { mutableStateOf(true) }
    var showGuides by remember { mutableStateOf(true) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Design Grid Audit",
                        style = CookedTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CookedTheme.colors.background
                )
            )
        }
    ) { innerPadding ->
        DebugGridOverlay(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            showGrid = showGrid,
            showGutterGuides = showGuides
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Controls
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = showGrid,
                            onClick = { showGrid = !showGrid },
                            label = { Text("4dp/8dp Grid") }
                        )
                        FilterChip(
                            selected = showGuides,
                            onClick = { showGuides = !showGuides },
                            label = { Text("20dp Guides") }
                        )
                    }
                }

                // Mascot Row
                item {
                    Text(
                        text = "Mascot States (96x96 Vector)",
                        style = CookedTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = CookedTheme.colors.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CookedCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            EggMascot(level = HeatLevel.CHILL, size = 52.dp, animated = false)
                            EggMascot(level = HeatLevel.WARM, size = 52.dp, animated = false)
                            EggMascot(level = HeatLevel.TOASTY, size = 52.dp, animated = false)
                            EggMascot(level = HeatLevel.COOKED, size = 52.dp, animated = false)
                            EggMascot(level = HeatLevel.CHILL, isDone = true, size = 52.dp, animated = false)
                        }
                    }
                }

                // HeatBar & Tabular Figures
                item {
                    Text(
                        text = "HeatBar & Tabular Numbers",
                        style = CookedTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = CookedTheme.colors.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CookedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Toasty (78%)",
                                    style = CookedTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = CookedTheme.colors.onSurface
                                )
                                RollingNumberText(
                                    targetNumber = 78,
                                    textStyle = CookedTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = CookedTheme.colors.primary
                                    )
                                )
                            }
                            HeatBar(progress = 0.78f, level = HeatLevel.TOASTY)
                        }
                    }
                }

                // Spacing Guide Info
                item {
                    Text(
                        text = "Spatial Audit Specifications",
                        style = CookedTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = CookedTheme.colors.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CookedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "• Blue Lines: 4dp and 8dp rhythm alignment.",
                                style = CookedTheme.typography.bodySmall,
                                color = CookedTheme.colors.onSurfaceVariant
                            )
                            Text(
                                text = "• Dashed Ember Lines: 20dp edge screen gutters.",
                                style = CookedTheme.typography.bodySmall,
                                color = CookedTheme.colors.onSurfaceVariant
                            )
                            Text(
                                text = "• Surface: Flat #FAF7F2 (light) / #0E0D0C (dark).",
                                style = CookedTheme.typography.bodySmall,
                                color = CookedTheme.colors.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
