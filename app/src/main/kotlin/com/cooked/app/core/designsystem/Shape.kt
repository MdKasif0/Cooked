package com.cooked.app.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object CookedRadius {
    val Chip = 10.dp
    val Button = 14.dp
    val Card = 20.dp
    val Sheet = 28.dp
}

val CookedShapes = Shapes(
    extraSmall = RoundedCornerShape(CookedRadius.Chip),
    small = RoundedCornerShape(CookedRadius.Button),
    medium = RoundedCornerShape(CookedRadius.Card),
    large = RoundedCornerShape(CookedRadius.Sheet),
    extraLarge = RoundedCornerShape(CookedRadius.Sheet)
)
