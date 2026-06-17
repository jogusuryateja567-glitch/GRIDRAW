package com.gridraw.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Use system sans-serif since we can't embed fonts without assets
// Alternatively add Nunito Sans font files to assets and reference them here.
val GridRawFontFamily = FontFamily.Default

val GridRawTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = GridRawFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
        color = TextMain
    ),
    displayMedium = TextStyle(
        fontFamily = GridRawFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp,
        color = TextMain
    ),
    displaySmall = TextStyle(
        fontFamily = GridRawFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
        color = TextMain
    ),
    headlineLarge = TextStyle(
        fontFamily = GridRawFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp,
        color = TextMain
    ),
    headlineMedium = TextStyle(
        fontFamily = GridRawFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.25).sp,
        color = TextMain
    ),
    headlineSmall = TextStyle(
        fontFamily = GridRawFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.1).sp,
        color = TextMain
    ),
    titleLarge = TextStyle(
        fontFamily = GridRawFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.1).sp,
        color = TextMain
    ),
    titleMedium = TextStyle(
        fontFamily = GridRawFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
        color = TextMain
    ),
    titleSmall = TextStyle(
        fontFamily = GridRawFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
        color = TextMain
    ),
    bodyLarge = TextStyle(
        fontFamily = GridRawFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
        color = TextMain
    ),
    bodyMedium = TextStyle(
        fontFamily = GridRawFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
        color = TextMuted
    ),
    bodySmall = TextStyle(
        fontFamily = GridRawFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
        color = TextDim
    ),
    labelLarge = TextStyle(
        fontFamily = GridRawFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
        color = TextMain
    ),
    labelMedium = TextStyle(
        fontFamily = GridRawFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        color = TextMuted
    ),
    labelSmall = TextStyle(
        fontFamily = GridRawFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp,
        color = TextDim
    ),
)
