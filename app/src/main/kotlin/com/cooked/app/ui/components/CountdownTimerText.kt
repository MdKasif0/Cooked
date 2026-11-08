package com.cooked.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.cooked.app.core.time.TimeUtils
import kotlinx.coroutines.delay

@Composable
fun CountdownTimerText(
    targetUtcMillis: Long,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelMedium,
    color: Color = MaterialTheme.colorScheme.primary
) {
    var nowMillis by remember { mutableLongStateOf(TimeUtils.nowUtcMillis()) }

    LaunchedEffect(targetUtcMillis) {
        while (true) {
            nowMillis = TimeUtils.nowUtcMillis()
            delay(1000L)
        }
    }

    val countdown = TimeUtils.formatCountdown(targetUtcMillis, nowMillis)
    val text = if (targetUtcMillis <= nowMillis) "🔥 Overdue!" else countdown

    Text(
        text = text,
        modifier = modifier,
        style = style,
        color = color
    )
}

