package com.cooked.app.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.components.EmptyState
import com.cooked.app.domain.model.PressureLevel
import com.cooked.app.ui.components.CookedScoreCard
import com.cooked.app.ui.components.ReminderCard

@Composable
fun TodayScreen(
    uiState: TodayUiState,
    onReminderClick: (String) -> Unit,
    onDoneClick: (String) -> Unit,
    onSnoozeClick: (String) -> Unit,
    onCookedScoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        if (uiState.cookedScore != null) {
            item {
                CookedScoreCard(
                    cookedScore = uiState.cookedScore,
                    onClick = onCookedScoreClick
                )
            }
        }

        item {
            Text(
                text = "Today's Focus",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (uiState.reminders.isEmpty() && !uiState.isLoading) {
            item {
                EmptyState(
                    title = uiState.emptyMessage,
                    message = "Go drink water or do something bold."
                )
            }
        } else {
            items(
                items = uiState.reminders,
                key = { it.id }
            ) { reminder ->
                ReminderCard(
                    reminder = reminder,
                    pressureLevel = if (reminder.isOverdue) PressureLevel.CRISPY else PressureLevel.CHILL,
                    onDoneClick = { onDoneClick(reminder.id) },
                    onSnoozeClick = { onSnoozeClick(reminder.id) },
                    onClick = { onReminderClick(reminder.id) }
                )
            }
        }
    }
}

