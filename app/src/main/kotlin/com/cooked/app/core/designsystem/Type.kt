package com.cooked.app.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.cooked.app.R

val BricolageGrotesque = FontFamily(
    Font(R.font.bricolage_grotesque_regular, FontWeight.Normal),
    Font(R.font.bricolage_grotesque_semibold, FontWeight.SemiBold),
    Font(R.font.bricolage_grotesque_bold, FontWeight.Bold),
    Font(R.font.bricolage_grotesque_extrabold, FontWeight.ExtraBold)
)

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold)
)

const val FontFeatureSettingsTabular = "tnum"

/**
 * Editorial type scale. Hierarchy comes from size and tracking rather than heavy weight:
 * large numerals and headlines use Bricolage Grotesque at regular/semibold with tight
 * negative tracking; everything functional is Inter.
 */
val CookedTypography = Typography(
    // Hero numerals (score, countdown, totals)
    displayLarge = TextStyle(
        fontFamily = BricolageGrotesque,
        fontWeight = FontWeight.Normal,
        fontSize = 60.sp,
        lineHeight = 62.sp,
        letterSpacing = (-2).sp,
        fontFeatureSettings = FontFeatureSettingsTabular
    ),
    displayMedium = TextStyle(
        fontFamily = BricolageGrotesque,
        fontWeight = FontWeight.Normal,
        fontSize = 46.sp,
        lineHeight = 50.sp,
        letterSpacing = (-1.4).sp,
        fontFeatureSettings = FontFeatureSettingsTabular
    ),
    displaySmall = TextStyle(
        fontFamily = BricolageGrotesque,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 40.sp,
        letterSpacing = (-1).sp,
        fontFeatureSettings = FontFeatureSettingsTabular
    ),
    // Screen titles
    headlineLarge = TextStyle(
        fontFamily = BricolageGrotesque,
        fontWeight = FontWeight.SemiBold,
        fontSize = 34.sp,
        lineHeight = 38.sp,
        letterSpacing = (-1).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = BricolageGrotesque,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.7).sp,
        fontFeatureSettings = FontFeatureSettingsTabular
    ),
    headlineSmall = TextStyle(
        fontFamily = BricolageGrotesque,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.5).sp
    ),
    titleLarge = TextStyle(
        fontFamily = BricolageGrotesque,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.3).sp
    ),
    titleMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.15).sp
    ),
    titleSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.1).sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.1).sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.1.sp
    ),
    // Overlines / micro labels
    labelSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
)
