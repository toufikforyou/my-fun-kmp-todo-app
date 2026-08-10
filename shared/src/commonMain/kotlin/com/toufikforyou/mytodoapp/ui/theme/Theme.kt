package com.toufikforyou.mytodoapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.toufikforyou.mytodoapp.domain.model.ThemeMode

/**
 * Android can derive a scheme from the user's wallpaper; other platforms return null and fall back
 * to the bundled palette.
 */
@Composable
expect fun platformDynamicColorScheme(dark: Boolean): ColorScheme?

/** Whether the settings screen should offer the dynamic color switch at all. */
expect fun supportsDynamicColor(): Boolean

@Composable
fun AppTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val dynamic = if (dynamicColor) platformDynamicColorScheme(dark) else null
    val colorScheme = dynamic ?: if (dark) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
