package com.dafyomi.pro.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import java.time.LocalTime

// ============================================================================
// COMPOSITION LOCALS - For theme-aware values
// ============================================================================

data class DafColors(
    // Gradient backgrounds
    val gradientBrush: Brush,
    val isDark: Boolean,

    // Base colors
    val background: Color,
    val backgroundSecondary: Color,

    // Semantic colors
    val sand: Color,
    val stone: Color,
    val stoneMuted: Color,
    val sky: Color,
    val skyDeep: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val progressTrack: Color,
    val divider: Color
)

val LocalDafColors = staticCompositionLocalOf {
    DafColors(
        gradientBrush = Brush.verticalGradient(listOf(SkyPastel, SandFaint)),
        isDark = false,
        background = SandLight,
        backgroundSecondary = SandLight2,
        sand = SandLight3,
        stone = StoneLight,
        stoneMuted = StoneMuted,
        sky = SkyLight,
        skyDeep = SkyDeep,
        textPrimary = Charcoal,
        textSecondary = StoneMuted,
        progressTrack = SemanticColors.ProgressTrack,
        divider = SemanticColors.Divider
    )
}

// ============================================================================
// LIGHT COLOR SCHEME
// ============================================================================

private val LightColorScheme = lightColorScheme(
    primary = SkyDeep,
    onPrimary = Color.White,
    secondary = SandLight3,
    onSecondary = Charcoal,
    tertiary = SkyLight,
    onTertiary = Charcoal,
    background = SandLight,
    onBackground = Charcoal,
    surface = SandLight,
    onSurface = Charcoal,
    surfaceVariant = SandLight2,
    onSurfaceVariant = StoneMuted,
    outline = StoneLight,
    outlineVariant = SemanticColors.Divider
)

// ============================================================================
// DARK COLOR SCHEME
// ============================================================================

private val DarkColorScheme = darkColorScheme(
    primary = SkyLight,
    onPrimary = CharcoalDark,
    secondary = SandDark,
    onSecondary = Cream,
    tertiary = SkyDark,
    onTertiary = Cream,
    background = CharcoalDark,
    onBackground = Cream,
    surface = CharcoalDark,
    onSurface = Cream,
    surfaceVariant = CharcoalDark2,
    onSurfaceVariant = StoneMutedDark,
    outline = StoneMutedDark,
    outlineVariant = SemanticColors.DividerDark
)

// ============================================================================
// THEME PROVIDER
// ============================================================================

enum class ThemeMode {
    OFF,        // Always light mode
    ON,         // Always dark mode
    AUTO        // Based on time: 6am-6pm light, 6pm-6am dark
}

@Composable
fun DafYomiProTheme(
    themeMode: ThemeMode = ThemeMode.AUTO,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.OFF -> false
        ThemeMode.ON -> true
        ThemeMode.AUTO -> {
            val hour = LocalTime.now().hour
            hour < 6 || hour >= 18
        }
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    val dafColors = if (isDark) {
        DafColors(
            gradientBrush = Brush.verticalGradient(
                colors = listOf(NightNearBlack, NightDeepBlue)
            ),
            isDark = true,
            background = CharcoalDark,
            backgroundSecondary = CharcoalDark2,
            sand = SandDark,
            stone = StoneDark,
            stoneMuted = StoneMutedDark,
            sky = SkyDark,
            skyDeep = SkyNight,
            textPrimary = Cream,
            textSecondary = StoneMutedDark,
            progressTrack = SemanticColors.ProgressTrackDark,
            divider = SemanticColors.DividerDark
        )
    } else {
        DafColors(
            gradientBrush = Brush.verticalGradient(
                colors = listOf(SkyPastel, SandFaint)
            ),
            isDark = false,
            background = SandLight,
            backgroundSecondary = SandLight2,
            sand = SandLight3,
            stone = StoneLight,
            stoneMuted = StoneMuted,
            sky = SkyLight,
            skyDeep = SkyDeep,
            textPrimary = Charcoal,
            textSecondary = StoneMuted,
            progressTrack = SemanticColors.ProgressTrack,
            divider = SemanticColors.Divider
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = dafColors.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    CompositionLocalProvider(LocalDafColors provides dafColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
