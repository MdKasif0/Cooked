package com.cooked.app.feature.deadlines.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.HeatLevel

@Composable
fun WorkloadWarningBanner(
    warningMessage: String,
    onStartOneClick: () -> Unit,
    onPushDeadlineClick: () -> Unit,
    onLowerEffortClick: () -> Unit,
    onSaveMeClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val toastyColor = CookedTheme.colors.heatFor(HeatLevel.TOASTY).color

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = CookedTheme.colors.surfaceContainerHigh,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.WarningAmber,
                contentDescription = null,
                tint = toastyColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "OVERLOAD WARNING",
                style = CookedTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = toastyColor,
                letterSpacing = 1.sp
            )
        }

        Text(
            text = warningMessage,
            style = CookedTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = CookedTheme.colors.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onStartOneClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = CookedTheme.colors.primary
                )
            ) {
                Text("Start one now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onPushDeadlineClick,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Push a deadline", fontSize = 12.sp)
            }
            if (onSaveMeClick != null) {
                OutlinedButton(
                    onClick = onSaveMeClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CookedTheme.colors.heatFor(HeatLevel.COOKED).color
                    )
                ) {
                    Text("Save me 🆘", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

