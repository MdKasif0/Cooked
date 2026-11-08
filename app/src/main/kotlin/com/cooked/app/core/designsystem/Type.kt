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

val CookedTypography = Typography(
    // Display Large: 48sp / 52sp line height, Bricolage Grotesque, tabular numbers for countdowns
    displayLarge = TextStyle(
        fontFamily = BricolageGrotesque,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 48.sp,
        lineHeight = 52.sp,
        fontFeatureSettings = FontFeatureSettingsTabular
    ),
    // Headline Medium: 28sp / 34sp line height, Bricolage Grotesque
    headlineMedium = TextStyle(
        fontFamily = BricolageGrotesque,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        fontFeatureSettings = FontFeatureSettingsTabular
    ),
    // Title Large: 22sp / 28sp line height, Bricolage Grotesque
    titleLarge = TextStyle(
        fontFamily = BricolageGrotesque,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    // Title Medium: 17sp / 24sp semibold, Inter
    titleMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 24.sp
    ),
    // Body Large: 16sp / 24sp, Inter
    bodyLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    // Body Medium: 14sp / 20sp, Inter
    bodyMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    // Label Large: 14sp / 20sp medium, Inter
    labelLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    // Label Small: 11sp / 16sp (+0.4 tracking, uppercase overlines), Inter
    labelSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    )
)
