package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// DIV SONG AI Brand Colors (from Brand Guide)
val DivPurple = Color(0xFF8A2BE2)        // #8A2BE2 Vibrant Purple
val DivPurpleLight = Color(0xFFA855F7)   // #A855F7 Light Purple
val DivPurpleDark = Color(0xFF581C87)    // #581C87 Deep Purple
val DivPink = Color(0xFFFF00CC)          // #FF00CC Neon Pink / Magenta
val DivPinkLight = Color(0xFFFF4FD8)     // #FF4FD8
val DivCyan = Color(0xFF00D4FF)          // #00D4FF Electric Cyan
val DivCyanDark = Color(0xFF0284C7)      // #0284C7 Deep Cyan

// Dark Canvas & Glassmorphic Surfaces
val DivBackground = Color(0xFF0A0A14)    // Deep dark space canvas
val DivSurfaceDark = Color(0xFF121224)   // Surface 1
val DivCard = Color(0xFF18182E)          // Surface 2 / Cards
val DivCardElevated = Color(0xFF22223D)  // Surface 3 / Elevated
val DivBorder = Color(0xFF2E2E52)        // Subtle card border
val DivBorderGlow = Color(0x668A2BE2)    // Neon border glow

// Text & Icons
val DivTextPrimary = Color(0xFFFFFFFF)
val DivTextSecondary = Color(0xFF94A3B8)
val DivTextMuted = Color(0xFF64748B)

// Accent Status Colors
val DivSuccess = Color(0xFF10B981)
val DivWarning = Color(0xFFF59E0B)
val DivError = Color(0xFFEF4444)

// Gradient Brushes
val DivGradientBrand = Brush.linearGradient(
    colors = listOf(DivPurple, DivPink, DivCyan)
)

val DivGradientPurpleCyan = Brush.linearGradient(
    colors = listOf(DivPurple, DivCyan)
)

val DivGradientPinkPurple = Brush.linearGradient(
    colors = listOf(DivPink, DivPurple)
)

val DivGradientSurface = Brush.verticalGradient(
    colors = listOf(Color(0xFF1E1E38), Color(0xFF101024))
)

val DivGradientHeroGlow = Brush.radialGradient(
    colors = listOf(Color(0x338A2BE2), Color(0x1A00D4FF), Color(0x000A0A14))
)
