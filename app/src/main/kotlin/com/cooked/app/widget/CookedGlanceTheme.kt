package com.cooked.app.widget

import androidx.compose.ui.graphics.Color
import androidx.glance.color.ColorProvider
import com.cooked.app.core.designsystem.DarkBackground
import com.cooked.app.core.designsystem.DarkOnBackground
import com.cooked.app.core.designsystem.DarkOnPrimary
import com.cooked.app.core.designsystem.DarkOnSurface
import com.cooked.app.core.designsystem.DarkOnSurfaceVariant
import com.cooked.app.core.designsystem.DarkOutline
import com.cooked.app.core.designsystem.DarkPrimary
import com.cooked.app.core.designsystem.DarkPrimaryContainer
import com.cooked.app.core.designsystem.DarkSurface
import com.cooked.app.core.designsystem.DarkSurfaceContainer
import com.cooked.app.core.designsystem.HeatChillDark
import com.cooked.app.core.designsystem.HeatChillLight
import com.cooked.app.core.designsystem.HeatCookedDark
import com.cooked.app.core.designsystem.HeatCookedLight
import com.cooked.app.core.designsystem.HeatToastyDark
import com.cooked.app.core.designsystem.HeatToastyLight
import com.cooked.app.core.designsystem.HeatWarmDark
import com.cooked.app.core.designsystem.HeatWarmLight
import com.cooked.app.core.designsystem.LightBackground
import com.cooked.app.core.designsystem.LightOnBackground
import com.cooked.app.core.designsystem.LightOnPrimary
import com.cooked.app.core.designsystem.LightOnSurface
import com.cooked.app.core.designsystem.LightOnSurfaceVariant
import com.cooked.app.core.designsystem.LightOutline
import com.cooked.app.core.designsystem.LightPrimary
import com.cooked.app.core.designsystem.LightPrimaryContainer
import com.cooked.app.core.designsystem.LightSurface
import com.cooked.app.core.designsystem.LightSurfaceContainer

object CookedGlanceColors {
    val Background = ColorProvider(day = LightBackground, night = DarkBackground)
    val Surface = ColorProvider(day = LightSurface, night = DarkSurface)
    val SurfaceContainer = ColorProvider(day = LightSurfaceContainer, night = DarkSurfaceContainer)
    val OnSurface = ColorProvider(day = LightOnSurface, night = DarkOnSurface)
    val OnSurfaceVariant = ColorProvider(day = LightOnSurfaceVariant, night = DarkOnSurfaceVariant)
    val Primary = ColorProvider(day = LightPrimary, night = DarkPrimary)
    val OnPrimary = ColorProvider(day = LightOnPrimary, night = DarkOnPrimary)
    val PrimaryContainer = ColorProvider(day = LightPrimaryContainer, night = DarkPrimaryContainer)
    val Outline = ColorProvider(day = LightOutline, night = DarkOutline)

    // Heat tokens
    val Chill = ColorProvider(day = HeatChillLight, night = HeatChillDark)
    val Warm = ColorProvider(day = HeatWarmLight, night = HeatWarmDark)
    val Toasty = ColorProvider(day = HeatToastyLight, night = HeatToastyDark)
    val Cooked = ColorProvider(day = HeatCookedLight, night = HeatCookedDark)
}

