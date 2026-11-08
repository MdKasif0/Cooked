package com.cooked.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cooked.app.R
import com.cooked.app.core.designsystem.FlameAmber
import com.cooked.app.core.designsystem.FlameOrange
import com.cooked.app.core.designsystem.PressureChill
import com.cooked.app.core.designsystem.PressureCrispy
import com.cooked.app.domain.model.CookedLevel
import com.cooked.app.domain.model.CookedScore

@Composable
fun CookedScoreCard(
    cookedScore: CookedScore,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = cookedScore.score / 100f,
        animationSpec = tween(durationMillis = 800),
        label = "cooked_progress"
    )

    val (indicatorColor, gradientColors) = when (cookedScore.level) {
        CookedLevel.CHILLING -> Pair(
            PressureChill,
            listOf(PressureChill.copy(alpha = 0.12f), PressureChill.copy(alpha = 0.03f))
        )
        CookedLevel.SIMMERING -> Pair(
            FlameAmber,
            listOf(FlameAmber.copy(alpha = 0.15f), FlameAmber.copy(alpha = 0.04f))
        )
        CookedLevel.COOKED -> Pair(
            FlameOrange,
            listOf(FlameOrange.copy(alpha = 0.18f), FlameOrange.copy(alpha = 0.05f))
        )
        CookedLevel.DEEP_FRIED -> Pair(
            PressureCrispy,
            listOf(PressureCrispy.copy(alpha = 0.22f), PressureCrispy.copy(alpha = 0.06f))
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.verticalGradient(gradientColors))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(28.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(indicatorColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = stringResource(R.string.cd_cooked_meter),
                            tint = indicatorColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.cooked_score_label),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${cookedScore.score}%",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                        color = indicatorColor
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = indicatorColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = cookedScore.headline,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = cookedScore.advice,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

