package com.dafyomi.pro.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// SAND, STONE & SKY PALETTE - Minimalist Flat Design with Gradient Backgrounds
// ============================================================================

// Light Mode - Subtle pastel gradient (sky blue to sand)
val SkyPastel = Color(0xFFE8F4F8)        // Lightest sky - top gradient
val SandFaint = Color(0xFFF5F0E8)       // Faint sand - bottom gradient

// Dark Mode - Deep gradient (near-black to deepest blue)
val NightNearBlack = Color(0xFF0A0A10)   // Deepest - top gradient
val NightDeepBlue = Color(0xFF0D1B2A)    // Deep blue - bottom gradient

// Light Mode Colors
val SandLight = Color(0xFFF5F0E8)        // Primary background - warm pale sand
val SandLight2 = Color(0xFFE8DFD0)      // Secondary sand - warm beige
val SandLight3 = Color(0xFFECD8B8)       // Accent sand - raffia

val StoneLight = Color(0xFF8B8680)       // Primary text - warm gray
val StoneMuted = Color(0xFFA69F96)       // Secondary text - taupe

val SkyLight = Color(0xFFA9D4E3)         // Primary accent - soft blue
val SkyLight2 = Color(0xFFB8E4EC)         // Secondary sky - celeste
val SkyDeep = Color(0xFF6F8FA3)           // Deep accent - steel blue

val OffWhite = Color(0xFFFAF8F5)         // Card/surface alternative
val Charcoal = Color(0xFF2D2926)          // Primary text

// Dark Mode Colors
val SandDark = Color(0xFF3D3830)          // Darker sand
val StoneDark = Color(0xFF2A2727)        // Darker slate
val StoneMutedDark = Color(0xFF4A4540)   // Muted stone

val SkyDark = Color(0xFF7A9BAB)           // Dusty blue accent
val SkyNight = Color(0xFF3D5A6B)          // Night blue accent

val CharcoalDark = Color(0xFF1E1C1A)      // Deep charcoal - background
val CharcoalDark2 = Color(0xFF2A2724)     // Slightly lighter - secondary bg
val Cream = Color(0xFFF0EAE0)             // Primary text - cream

// Semantic Colors (work for both modes via context)
object SemanticColors {
    val ProgressTrack = Color(0xFFE0D8CC)
    val ProgressTrackDark = Color(0xFF3A3632)
    val Divider = Color(0x26798680)
    val DividerDark = Color(0x40F0EAE0)
}
