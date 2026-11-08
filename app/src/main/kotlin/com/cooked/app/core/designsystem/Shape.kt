package com.cooked.app.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object CookedRadius {
    val Chip = 50.dp       // effectively a pill at chip heights
    val Button = 50.dp     // pill buttons
    val Field = 22.dp      // inputs and compact tiles
    val Card = 28.dp
    val Sheet = 32.dp
}

/** Fully rounded shape for chips, buttons, search fields and the nav bar. */
val PillShape = RoundedCornerShape(percent = 50)

val CookedShapes = Shapes(
    extraSmall = RoundedCornerShape(14.dp),
    small = PillShape,
    medium = RoundedCornerShape(CookedRadius.Card),
    large = RoundedCornerShape(CookedRadius.Sheet),
    extraLarge = RoundedCornerShape(CookedRadius.Sheet)
)
