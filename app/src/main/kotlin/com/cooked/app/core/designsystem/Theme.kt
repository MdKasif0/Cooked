package com.cooked.app.core.designsystem

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightMaterialColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = Ink,
    onSecondary = LightOnPrimary,
    secondaryContainer = LightSurfaceContainer,
    onSecondaryContainer = Ink,
    tertiary = VoltDeep,
    onTertiary = LightOnPrimary,
    surfaceTint = Color.Transparent,
    surfaceContainerLowest = LightSurface,
    surfaceContainerHighest = LightSurfaceContainerHigh,
    inverseSurface = Ink,
    inverseOnSurface = LightOnPrimary,
    inversePrimary = Volt,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceVariant = LightSurfaceContainer,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant
)

private val DarkMaterialColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = Volt,
    onSecondary = DarkOnPrimary,
    secondaryContainer = DarkSurfaceContainerHigh,
    onSecondaryContainer = DarkOnSurface,
    tertiary = Volt,
    onTertiary = DarkOnPrimary,
    surfaceTint = Color.Transparent,
    surfaceContainerLowest = DarkBackground,
    surfaceContainerHighest = DarkSurfaceContainerHigh,
    inverseSurface = DarkOnSurface,
    inverseOnSurface = DarkBackground,
    inversePrimary = Ink,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceVariant = DarkSurfaceContainer,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookedTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // OFF by default (brand-first Volt palette)
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val cookedColors = if (darkTheme) DarkCookedColors else LightCookedColors

    val materialColorScheme: ColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkMaterialColorScheme
        else -> LightMaterialColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    val rippleConfiguration = RippleConfiguration(color = cookedColors.onSurface)

    CompositionLocalProvider(
        LocalCookedColors provides cookedColors,
        LocalCookedSpacing provides CookedSpacing(),
        LocalRippleConfiguration provides rippleConfiguration
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = CookedTypography,
            shapes = CookedShapes,
            content = content
        )
    }
}

/**
 * Direct accessor object for Cooked design system tokens.
 */
object CookedTheme {
    val colors: CookedColors
        @Composable
        get() = LocalCookedColors.current

    val spacing: CookedSpacing
        @Composable
        get() = LocalCookedSpacing.current

    val shapes: Shapes
        @Composable
        get() = MaterialTheme.shapes

    val typography: Typography
        @Composable
        get() = MaterialTheme.typography
}
