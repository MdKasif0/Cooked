package com.cooked.app.core.designsystem.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.cooked.app.core.designsystem.FontFeatureSettingsTabular

/**
 * Tabular rolling digit animation for numbers (Cooked Score, StatTiles, metrics).
 * Changes roll smoothly vertically with fixed-width tabular spacing.
 */
@Composable
fun RollingNumberText(
    number: Int,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    suffix: String = ""
) {
    var previousNumber by remember { mutableIntStateOf(number) }
    val isIncreasing = number >= previousNumber

    SideEffect {
        previousNumber = number
    }

    val numberString = number.toString()
    val tabularStyle = style.copy(fontFeatureSettings = FontFeatureSettingsTabular)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        numberString.forEachIndexed { index, char ->
            AnimatedContent(
                targetState = char,
                transitionSpec = {
                    if (isIncreasing) {
                        slideInVertically { height -> height } togetherWith
                                slideOutVertically { height -> -height }
                    } else {
                        slideInVertically { height -> -height } togetherWith
                                slideOutVertically { height -> height }
                    }
                },
                label = "RollingDigit_$index"
            ) { targetChar ->
                Text(
                    text = targetChar.toString(),
                    style = tabularStyle,
                    color = color
                )
            }
        }

        if (suffix.isNotEmpty()) {
            Text(
                text = suffix,
                style = tabularStyle,
                color = color
            )
        }
    }
}

