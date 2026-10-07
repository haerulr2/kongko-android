package com.kongko.app.core.designsystem.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Immutable
data class ExtendedColors(
    val primaryGradient: Brush,
    val surfaceSubtle: Color,
    val inkFaint: Color,
    val inkMuted: Color,
    val statusOnline: Color,
    val statusOffline: Color,
    val raspberryAccent: Color,
    val raspberryTint: Color,
    val tealTint: Color,
    val isDark: Boolean
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        primaryGradient = PrimaryGradient,
        surfaceSubtle = SurfaceSubtleLight,
        inkFaint = InkFaintLight,
        inkMuted = InkMutedLight,
        statusOnline = StatusOnline,
        statusOffline = StatusOffline,
        raspberryAccent = RaspberryAccent,
        raspberryTint = RaspberryTint,
        tealTint = TealTint,
        isDark = false
    )
}

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = TealTint,
    onPrimaryContainer = TealDark,
    secondary = TealLight,
    onSecondary = Color.White,
    background = BackgroundLight,
    onBackground = InkLight,
    surface = SurfaceLight,
    onSurface = InkLight,
    surfaceVariant = SurfaceSubtleLight,
    onSurfaceVariant = InkMutedLight,
    outline = BorderLight
)

private val DarkColorScheme = darkColorScheme(
    primary = TealLight,
    onPrimary = BackgroundDark,
    primaryContainer = TealDark,
    onPrimaryContainer = TealTint,
    secondary = TealPrimary,
    onSecondary = Color.White,
    background = BackgroundDark,
    onBackground = InkDark,
    surface = SurfaceDark,
    onSurface = InkDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = InkMutedDark,
    outline = BorderDark
)

@Composable
fun KongkoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val extendedColors = ExtendedColors(
        primaryGradient = PrimaryGradient,
        surfaceSubtle = if (darkTheme) SurfaceVariantDark else SurfaceSubtleLight,
        inkFaint = if (darkTheme) InkFaintDark else InkFaintLight,
        inkMuted = if (darkTheme) InkMutedDark else InkMutedLight,
        statusOnline = StatusOnline,
        statusOffline = StatusOffline,
        raspberryAccent = RaspberryAccent,
        raspberryTint = RaspberryTint,
        tealTint = TealTint,
        isDark = darkTheme
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

object KongkoDesign {
    val colors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}
