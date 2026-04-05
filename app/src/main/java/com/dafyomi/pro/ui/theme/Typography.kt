package com.dafyomi.pro.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ============================================================================
// TYPOGRAPHY PHILOSOPHY - Swiss/International Style
// ============================================================================
//
// Core Principles:
// 1. Hierarchy through weight and scale ONLY (no color, shadows, or decoration)
// 2. Grid-based precision with mathematical spacing
// 3. Content speaks through clarity - white space is structural
// 4. Typography must function in grayscale alone
//
// Type Scale (Perfect Fourth - 1.333 ratio):
// Base: 16px → 12, 16, 21, 28, 38, 50
//
// Line Height:
// - Body text: 1.5-1.6 (Swiss standard for readability)
// - Headlines: 1.1-1.3 (tighter for impact)
// - Hebrew script: 1.65 (higher due to script density)
//
// Letter Spacing:
// - Body: 0 (default)
// - Uppercase labels: 0.05em (breathability)
// - Headlines: -0.02em (slight tightening for elegance)
// ============================================================================

val Typography = Typography(
    // Display - Hebrew Daf name (large, serif, light weight)
    // Swiss style: generous size, light weight, tight tracking
    displayLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Light,
        fontSize = 60.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.02).sp
    ),

    // Headline 1 - Major section titles
    // Swiss hierarchy: medium weight, clean sans-serif
    headlineLarge = TextStyle(
        fontWeight = FontWeight.W500,  // Medium, not bold - Swiss restraint
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),

    // Headline 2 - Secondary headers
    headlineMedium = TextStyle(
        fontWeight = FontWeight.W500,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),

    // Headline 3 - Minor headers
    headlineSmall = TextStyle(
        fontWeight = FontWeight.W500,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // Title - Navigation and UI labels
    titleLarge = TextStyle(
        fontWeight = FontWeight.W600,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),

    titleMedium = TextStyle(
        fontWeight = FontWeight.W600,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),

    titleSmall = TextStyle(
        fontWeight = FontWeight.W500,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // Body - Extended reading text
    // Swiss standard: generous line-height for screen readability
    // Hebrew body text gets higher line-height (1.65) for script clarity
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 28.sp,           // 1.65 ratio - generous for Hebrew
        letterSpacing = 0.15.sp
    ),

    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 24.sp,           // 1.6 ratio
        letterSpacing = 0.15.sp
    ),

    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.2.sp
    ),

    // Labels - Metadata, timestamps, section headers
    // Uppercase with letter-spacing for breathability (Swiss technique)
    labelLarge = TextStyle(
        fontWeight = FontWeight.W500,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.08.sp
    ),

    labelMedium = TextStyle(
        fontWeight = FontWeight.W500,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,        // Uppercase breathability
        // textTransform = TextTransform.Uppercase // Compose doesn't have this
    ),

    labelSmall = TextStyle(
        fontWeight = FontWeight.W500,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.6.sp         // Maximum breathability for small caps
    )
)
