package com.cooked.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cooked.app.R
import com.cooked.app.core.designsystem.PressureChill
import com.cooked.app.core.designsystem.PressureCooked
import com.cooked.app.core.designsystem.PressureCrispy
import com.cooked.app.core.designsystem.PressureSimmer
import com.cooked.app.domain.model.PressureLevel

@Composable
fun PressureBadge(
    level: PressureLevel,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, textRes) = when (level) {
        PressureLevel.CHILL -> Triple(PressureChill.copy(alpha = 0.15f), PressureChill, R.string.pressure_low)
        PressureLevel.HEATING_UP -> Triple(PressureSimmer.copy(alpha = 0.18f), PressureSimmer, R.string.pressure_medium)
        PressureLevel.HIGH_HEAT -> Triple(PressureCooked.copy(alpha = 0.20f), PressureCooked, R.string.pressure_high)
        PressureLevel.CRISPY -> Triple(PressureCrispy.copy(alpha = 0.22f), PressureCrispy, R.string.pressure_extreme)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = stringResource(textRes),
            color = textColor,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
