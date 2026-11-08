package com.cooked.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cooked.app.R
import com.cooked.app.core.designsystem.FlameOrange
import com.cooked.app.core.designsystem.PressureCrispy
import com.cooked.app.core.time.TimeUtils
import com.cooked.app.domain.model.PressureLevel
import com.cooked.app.domain.model.Reminder

@Composable
fun ReminderCard(
    reminder: Reminder,
    pressureLevel: PressureLevel,
    onDoneClick: () -> Unit,
    onSnoozeClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOverdue = reminder.isOverdue
    val borderColor = when {
        reminder.isCompleted -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        reminder.isNaggingActive -> PressureCrispy.copy(alpha = 0.5f)
        isOverdue -> FlameOrange.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = reminder.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            color = if (reminder.isCompleted) {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (!reminder.note.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = reminder.note,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Check button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (reminder.isCompleted) FlameOrange.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable(onClick = onDoneClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.action_done),
                        tint = if (reminder.isCompleted) FlameOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Badges and time row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (reminder.dueInstant != null) {
                        Text(
                            text = TimeUtils.formatRelativeTime(reminder.dueInstant),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                            color = if (isOverdue) PressureCrispy else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (reminder.isNaggingActive && reminder.nagCount > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PressureCrispy.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Nagged ${reminder.nagCount}x",
                                color = PressureCrispy,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    if (reminder.isDeadlineMode) {
                        PressureBadge(level = pressureLevel)
                    }
                }

                if (!reminder.isCompleted) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilledTonalButton(
                            onClick = onSnoozeClick,
                            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Snooze,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.action_snooze_10m),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

