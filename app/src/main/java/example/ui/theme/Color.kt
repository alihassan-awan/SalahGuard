package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Backgrounds
val SalahDarkBg = Color(0xFF081316)
val SalahDarkSurface = Color(0xFF0E2026)
val SalahCardBg = Color(0xFF11262D)
val SalahCardBorder = Color(0xFF1B3842)
val SalahCardHover = Color(0xFF16323B)

// Accents & Brand
val SalahEmerald = Color(0xFF00B37E)
val SalahEmeraldDark = Color(0xFF047857)
val SalahEmeraldLight = Color(0xFF10B981)
val SalahGold = Color(0xFFF59E0B)
val SalahGoldLight = Color(0xFFFBBF24)
val SalahOrange = Color(0xFFF97316)
val SalahRed = Color(0xFFEF4444)
val SalahRedDark = Color(0xFFDC2626)

// Text
val SalahTextWhite = Color(0xFFFFFFFF)
val SalahTextSecondary = Color(0xFF8FA7AF)
val SalahTextMuted = Color(0xFF5A727A)
val SalahTextGreen = Color(0xFF10B981)
val SalahTextGold = Color(0xFFFBBF24)

// Status colors
val SalahStatusPrayed = Color(0xFF10B981)
val SalahStatusPending = Color(0xFFF59E0B)
val SalahStatusMissed = Color(0xFFEF4444)
val SalahStatusInactive = Color(0xFF3D535A)

// Gradients
val EmeraldButtonBrush = Brush.horizontalGradient(
    colors = listOf(Color(0xFF00B37E), Color(0xFF059669))
)

val HeroCardBrush = Brush.linearGradient(
    colors = listOf(Color(0xFF0E2C33), Color(0xFF091C22))
)

val AlarmHeroBrush = Brush.radialGradient(
    colors = listOf(Color(0x66F59E0B), Color(0x00000000))
)

val GlowCircleBrush = Brush.radialGradient(
    colors = listOf(Color(0xFF00B37E).copy(alpha = 0.35f), Color(0x00000000))
)
