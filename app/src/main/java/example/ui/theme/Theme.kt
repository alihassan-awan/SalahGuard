package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SalahGuardColorScheme = darkColorScheme(
    primary = SalahEmerald,
    onPrimary = SalahTextWhite,
    primaryContainer = SalahEmeraldDark,
    onPrimaryContainer = SalahTextWhite,
    secondary = SalahGold,
    onSecondary = SalahDarkBg,
    secondaryContainer = SalahCardBg,
    onSecondaryContainer = SalahGoldLight,
    tertiary = SalahOrange,
    onTertiary = SalahTextWhite,
    error = SalahRed,
    onError = SalahTextWhite,
    background = SalahDarkBg,
    onBackground = SalahTextWhite,
    surface = SalahDarkSurface,
    onSurface = SalahTextWhite,
    surfaceVariant = SalahCardBg,
    onSurfaceVariant = SalahTextSecondary,
    outline = SalahCardBorder,
    outlineVariant = SalahCardBorder
)

@Composable
fun SalahGuardTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = SalahDarkBg.toArgb()
                it.navigationBarColor = SalahDarkBg.toArgb()
                WindowCompat.getInsetsController(it, view).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = SalahGuardColorScheme,
        typography = Typography,
        content = content
    )
}
