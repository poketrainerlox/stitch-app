package com.stitch.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = StitchPrimary,
    onPrimary = StitchDarkBg,
    primaryContainer = StitchPrimaryDim,
    onPrimaryContainer = StitchTextPrimary,
    secondary = StitchSecondary,
    onSecondary = StitchDarkBg,
    secondaryContainer = StitchSecondaryDim,
    onSecondaryContainer = StitchTextPrimary,
    background = StitchDarkBg,
    onBackground = StitchTextPrimary,
    surface = StitchSurface,
    onSurface = StitchTextPrimary,
    surfaceVariant = StitchSurfaceVariant,
    onSurfaceVariant = StitchTextSecondary,
    error = StitchError,
    onError = StitchDarkBg,
    outline = StitchBorder,
    outlineVariant = StitchBorderActive
)

@Composable
fun StitchTheme(
    darkTheme: Boolean = true, // Default to sleek focus dark mode
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = colorScheme.background.toArgb()
                it.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(it, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
