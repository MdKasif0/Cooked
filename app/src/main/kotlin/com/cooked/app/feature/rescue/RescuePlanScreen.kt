package com.cooked.app.feature.rescue

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cooked.app.core.designsystem.CookedRadius
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.HeatLevel
import com.cooked.app.core.designsystem.components.CookedCard
import com.cooked.app.core.designsystem.components.EggMascot
import com.cooked.app.core.designsystem.components.PrimaryButton
import com.cooked.app.domain.rescue.RescuePlan
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RescuePlanScreen(
    plan: RescuePlan,
    onLockItIn: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val zone = remember { ZoneId.systemDefault() }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("EEE, dd MMM · h:mm a") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CookedTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Rescue Plan",
                        style = CookedTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = CookedTheme.colors.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = CookedTheme.colors.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CookedTheme.colors.background
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CookedTheme.colors.surface)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                PrimaryButton(
                    text = "Lock It In (${plan.sessions.size} Sessions) 🔒",
                    onClick = onLockItIn,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header summary
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EggMascot(
                        level = HeatLevel.COOKED,
                        size = 56.dp,
                        animated = true
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Save: ${plan.targetItem.title}",
                            style = CookedTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = CookedTheme.colors.onSurface
                        )
                        Text(
                            text = "${plan.totalMinutesPlanned}m of work scheduled across ${plan.sessions.size} focused sessions.",
                            style = CookedTheme.typography.bodySmall,
                            color = CookedTheme.colors.onSurfaceVariant
                        )
                    }
                }
            }

            // Overload warning & push recommendations if applicable
            if (plan.isOverloaded || plan.pushRecommendations.isNotEmpty()) {
                item {
                    val colors = CookedTheme.colors.heatFor(HeatLevel.COOKED)
                    CookedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.container)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Warning,
                                    contentDescription = null,
                                    tint = colors.textColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (plan.isOverloaded) "Workload > 1.0 (Critical Overload)" else "Tight Schedule Notice",
                                    style = CookedTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textColor
                                )
                            }

                            Text(
                                text = "To avoid getting cooked, consider pushing other deadlines:",
                                style = CookedTheme.typography.bodySmall,
                                color = colors.textColor
                            )

                            plan.pushRecommendations.forEach { push ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(CookedRadius.Chip))
                                        .background(CookedTheme.colors.surface)
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "👉 ${push.reason}",
                                        style = CookedTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = CookedTheme.colors.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Timeline Header
            item {
                Text(
                    text = "SCHEDULED TIMELINE",
                    style = CookedTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = CookedTheme.colors.primary,
                        letterSpacing = 1.sp
                    )
                )
            }

            // Vertical Timeline of Sessions
            itemsIndexed(plan.sessions, key = { _, s -> s.id }) { index, session ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Numbered Timeline Marker
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(36.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(CookedTheme.colors.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = CookedTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = CookedTheme.colors.onPrimary
                            )
                        }

                        if (index < plan.sessions.size - 1) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(48.dp)
                                    .background(CookedTheme.colors.outlineVariant)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Session Card
                    CookedCard(
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = session.title,
                                    style = CookedTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = CookedTheme.colors.onSurface
                                )
                                Text(
                                    text = "${session.durationMinutes}m",
                                    style = CookedTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = CookedTheme.colors.primary
                                )
                            }

                            Text(
                                text = session.stepDescription,
                                style = CookedTheme.typography.bodySmall,
                                color = CookedTheme.colors.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = CookedTheme.colors.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = timeFormatter.format(session.scheduledStart.atZone(zone)),
                                    style = CookedTheme.typography.labelSmall,
                                    color = CookedTheme.colors.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

