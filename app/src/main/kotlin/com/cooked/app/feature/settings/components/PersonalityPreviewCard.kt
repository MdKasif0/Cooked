package com.cooked.app.feature.settings.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.components.CookedCategoryIcon
import com.cooked.app.domain.model.Personality
import com.cooked.app.scheduling.notification.NagStage
import com.cooked.app.scheduling.notification.NotificationCopy

@Composable
fun PersonalityPreviewCard(
    personality: Personality,
    sampleTitle: String = "Submit tax forms",
    modifier: Modifier = Modifier
) {
    val samples = listOf(
        Pair(NagStage.FIRST, "First reminder"),
        Pair(NagStage.NAG_EARLY, "Nudge #2"),
        Pair(NagStage.FINAL_CALL, "Final call")
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = CookedTheme.colors.surfaceContainerLow,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${personality.name.lowercase().replaceFirstChar { it.uppercase() }} voice",
                style = CookedTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = CookedTheme.colors.onSurface
            )
            Text(
                text = "Live preview",
                style = CookedTheme.typography.labelSmall,
                color = CookedTheme.colors.primary
            )
        }

        samples.forEach { (stage, stageLabel) ->
            val copy = NotificationCopy.getCopy(
                personality = personality,
                stage = stage,
                title = sampleTitle,
                nagCount = if (stage == NagStage.FINAL_CALL) 5 else if (stage == NagStage.NAG_EARLY) 2 else 0
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = CookedTheme.colors.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CookedTheme.colors.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .background(CookedTheme.colors.primaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                CookedCategoryIcon(
                                    keyOrEmoji = "flame",
                                    size = 12.dp,
                                    tint = CookedTheme.colors.primary
                                )
                            }
                            Text(
                                text = "Cooked · $stageLabel",
                                style = CookedTheme.typography.labelSmall,
                                color = CookedTheme.colors.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "now",
                            style = CookedTheme.typography.labelSmall,
                            color = CookedTheme.colors.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = sampleTitle,
                        style = CookedTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = CookedTheme.colors.onSurface
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = copy,
                        style = CookedTheme.typography.bodySmall,
                        color = CookedTheme.colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}

