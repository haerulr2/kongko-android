package com.kongko.app.core.designsystem.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Teal Brand Palette
val TealLight = Color(0xFF0F9683)
val TealPrimary = Color(0xFF0B6E60)
val TealDark = Color(0xFF073F37)
val TealTint = Color(0xFFDEF2EE)

// Accent / Raspberry
val RaspberryAccent = Color(0xFFFF3F6C)
val RaspberryTint = Color(0xFFFFE3EA)

// Light Theme Neutrals
val BackgroundLight = Color(0xFFF6F9F8)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceSubtleLight = Color(0xFFF0F5F3)
val InkLight = Color(0xFF12201D)
val InkMutedLight = Color(0xFF6C7B77)
val InkFaintLight = Color(0xFF9AA6A2)
val BorderLight = Color(0xFFE4EAE8)
val BorderSubtleLight = Color(0xFFEDF2F0)

// Dark Theme Neutrals
val BackgroundDark = Color(0xFF0F1715)
val SurfaceDark = Color(0xFF162320)
val SurfaceVariantDark = Color(0xFF1D2F2B)
val BorderDark = Color(0xFF263D38)
val InkDark = Color(0xFFE6EDE9)
val InkMutedDark = Color(0xFF9AA6A2)
val InkFaintDark = Color(0xFF6C7B77)

// Status
val StatusOnline = Color(0xFF33C481)
val StatusOffline = Color(0xFF9AA6A2)

// Signature Gradients
val PrimaryGradient = Brush.linearGradient(
    listOf(TealLight, TealPrimary, TealDark)
)

val SurfaceGradientLight = Brush.verticalGradient(
    listOf(SurfaceLight, BackgroundLight)
)
