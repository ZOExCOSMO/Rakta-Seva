package com.raktaseva.connect.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        color = TextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        color = TextPrimary
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        color = TextPrimary
    ),
    bodyLarge = TextStyle(
        fontSize = 14.sp,
        color = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontSize = 13.sp,
        color = TextSecondary
    ),
    bodySmall = TextStyle(
        fontSize = 11.sp,
        color = TextTertiary
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 0.06.sp
    )
)