package com.cooked.app.feature.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.cooked.app.domain.scan.ScannedDeadlineCandidate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatesheetReviewScreen(
    candidates: List<ScannedDeadlineCandidate>,
    onConfirmAdd: (List<ScannedDeadlineCandidate>) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val items = remember { mutableStateListOf(*candidates.toTypedArray()) }
    val zone = remember { ZoneId.systemDefault() }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEE, dd MMM yyyy · hh:mm a") }

    val selectedCount = items.count { it.isSelected }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CookedTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Review Detected Deadlines",
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
                    text = if (selectedCount > 0) "Add $selectedCount Deadlines 🎯" else "Select at least 1 deadline",
                    enabled = selectedCount > 0,
                    onClick = {
                        val chosen = items.filter { it.isSelected }
                        onConfirmAdd(chosen)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EggMascot(level = HeatLevel.WARM, size = 48.dp, animated = false)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${items.size} exams/deadlines detected",
                            style = CookedTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = CookedTheme.colors.onSurface
                        )
                        Text(
                            text = "Check and edit below before adding. Low-confidence rows are outlined in amber.",
                            style = CookedTheme.typography.bodySmall,
                            color = CookedTheme.colors.onSurfaceVariant
                        )
                    }
                }
            }

            itemsIndexed(items, key = { _, item -> item.id }) { index, candidate ->
                val warmBorder = if (candidate.isLowConfidence) {
                    Modifier.border(1.5.dp, CookedTheme.colors.heatFor(HeatLevel.WARM).color, RoundedCornerShape(CookedRadius.Card))
                } else Modifier

                CookedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(warmBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = candidate.isSelected,
                                onCheckedChange = { isChecked ->
                                    items[index] = candidate.copy(isSelected = isChecked)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = CookedTheme.colors.primary,
                                    uncheckedColor = CookedTheme.colors.outlineVariant
                                )
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            OutlinedTextField(
                                value = candidate.title,
                                onValueChange = { newTitle ->
                                    items[index] = candidate.copy(title = newTitle)
                                },
                                label = { Text("Title") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CookedTheme.colors.primary,
                                    unfocusedBorderColor = CookedTheme.colors.outlineVariant
                                )
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 48.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = CookedTheme.colors.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = dateFormatter.format(candidate.dueAt.atZone(zone)),
                                    style = CookedTheme.typography.bodySmall,
                                    color = CookedTheme.colors.onSurface
                                )
                            }

                            if (candidate.isLowConfidence) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(CookedRadius.Chip))
                                        .background(CookedTheme.colors.heatFor(HeatLevel.WARM).container)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Check Date",
                                        style = CookedTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = CookedTheme.colors.heatFor(HeatLevel.WARM).textColor,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
