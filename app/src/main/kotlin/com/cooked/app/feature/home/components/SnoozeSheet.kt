package com.cooked.app.feature.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.EditCalendar
import androidx.compose.material.icons.rounded.MoreTime
import androidx.compose.material.icons.rounded.Snooze
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cooked.app.core.designsystem.CookedTheme
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnoozeSheet(
    onDismissRequest: () -> Unit,
    onSnoozeSelected: (Instant) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDatePicker by remember { mutableStateOf(false) }

    val now = Instant.now()
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)

    val snoozeOptions = listOf(
        SnoozeOption("10 minutes", "Quick breather", Icons.Rounded.Timer, now.plus(Duration.ofMinutes(10))),
        SnoozeOption("1 hour", "Focus session first", Icons.Rounded.MoreTime, now.plus(Duration.ofHours(1))),
        SnoozeOption("Tonight (8:00 pm)", "Wind-down hours", Icons.Rounded.Bedtime, today.atTime(20, 0).atZone(zone).toInstant()),
        SnoozeOption("Tomorrow morning (9:00 am)", "Fresh start", Icons.Rounded.CalendarMonth, today.plusDays(1).atTime(9, 0).atZone(zone).toInstant())
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = CookedTheme.colors.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Snooze,
                    contentDescription = null,
                    tint = CookedTheme.colors.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Snooze Reminder",
                    style = CookedTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = CookedTheme.colors.onSurface
                )
            }

            snoozeOptions.forEach { option ->
                SnoozeRowItem(
                    title = option.title,
                    subtitle = option.subtitle,
                    icon = option.icon,
                    onClick = {
                        onSnoozeSelected(option.targetInstant)
                        onDismissRequest()
                    }
                )
            }

            // Custom Pick Date/Time
            SnoozeRowItem(
                title = "Pick date & time...",
                subtitle = "Custom postpone",
                icon = Icons.Rounded.EditCalendar,
                onClick = { showDatePicker = true }
            )
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val pickedDate = Instant.ofEpochMilli(selectedMillis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                            val pickedInstant = pickedDate.atTime(18, 0).atZone(zone).toInstant()
                            onSnoozeSelected(pickedInstant)
                            onDismissRequest()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", color = CookedTheme.colors.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = CookedTheme.colors.onSurfaceVariant)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private data class SnoozeOption(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val targetInstant: Instant
)

@Composable
private fun SnoozeRowItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = CookedTheme.colors.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CookedTheme.colors.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = CookedTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = CookedTheme.colors.onSurface
                )
                Text(
                    text = subtitle,
                    style = CookedTheme.typography.bodySmall,
                    color = CookedTheme.colors.onSurfaceVariant
                )
            }
        }
    }
}
