package com.cooked.app.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Snooze
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.cooked.app.core.designsystem.CookedHaptics
import com.cooked.app.core.designsystem.CookedShapes
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.HeatLevel

/**
 * ReminderRow:
 * - Swipe right (StartToEnd) = Done with green reveal and confirm haptic
 * - Swipe left (EndToStart) = Snooze with warm reveal and tick haptic
 * - Foreground: title, relative time, urgency HeatChip
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderRow(
    title: String,
    timeText: String,
    heatLevel: HeatLevel,
    onDone: () -> Unit,
    onSnooze: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current
    val haptics = remember(hapticFeedback, view) { CookedHaptics(hapticFeedback, view) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    haptics.confirmDone()
                    onDone()
                    false // Return false so the item animates back or is managed by state
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    haptics.lightTick()
                    onSnooze()
                    false
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier.clip(CookedShapes.medium),
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val backgroundColor by animateColorAsState(
                targetValue = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> CookedTheme.colors.done.color
                    SwipeToDismissBoxValue.EndToStart -> CookedTheme.colors.warm.color
                    SwipeToDismissBoxValue.Settled -> Color.Transparent
                },
                label = "SwipeBackground"
            )

            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                SwipeToDismissBoxValue.Settled -> Alignment.Center
            }

            val icon = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Icons.Rounded.Check
                SwipeToDismissBoxValue.EndToStart -> Icons.Rounded.Snooze
                SwipeToDismissBoxValue.Settled -> null
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(horizontal = 24.dp),
                contentAlignment = alignment
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = if (direction == SwipeToDismissBoxValue.StartToEnd) "Done" else "Snooze",
                        tint = Color.White
                    )
                }
            }
        }
    ) {
        CookedCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = onClick
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = title,
                        style = CookedTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = CookedTheme.colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = timeText,
                        style = CookedTheme.typography.bodyMedium,
                        color = CookedTheme.colors.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                HeatChip(level = heatLevel)
            }
        }
    }
}

@PreviewLightDark
@Preview(fontScale = 1.5f)
@Composable
private fun ReminderRowPreview() {
    CookedTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ReminderRow(
                title = "Study Thermodynamics Ch 4",
                timeText = "Due in 38m",
                heatLevel = HeatLevel.COOKED,
                onDone = {},
                onSnooze = {}
            )
            ReminderRow(
                title = "Renew Freelance Contract",
                timeText = "Tomorrow at 5:00 PM",
                heatLevel = HeatLevel.WARM,
                onDone = {},
                onSnooze = {}
            )
        }
    }
}
