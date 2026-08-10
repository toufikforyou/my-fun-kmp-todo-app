package com.toufikforyou.mytodoapp.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import com.toufikforyou.mytodoapp.domain.model.Priority

/**
 * A violet-forward expressive palette. These are the fallback colours used whenever Android
 * dynamic colour is unavailable or switched off, and on iOS always.
 */
internal val LightColors = lightColorScheme(
    primary = Color(0xFF6247AA),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE7DEFF),
    onPrimaryContainer = Color(0xFF1D0060),
    secondary = Color(0xFF625B71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1E192B),
    tertiary = Color(0xFF00696D),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF9CF1F4),
    onTertiaryContainer = Color(0xFF002021),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFDF7FF),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFDF7FF),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EB),
    onSurfaceVariant = Color(0xFF49454E),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F2FA),
    surfaceContainer = Color(0xFFF2ECF4),
    surfaceContainerHigh = Color(0xFFECE6EE),
    surfaceContainerHighest = Color(0xFFE6E0E9),
    outline = Color(0xFF7A757F),
    outlineVariant = Color(0xFFCAC4CF),
)

internal val DarkColors = darkColorScheme(
    primary = Color(0xFFCFBCFF),
    onPrimary = Color(0xFF331D82),
    primaryContainer = Color(0xFF4A3591),
    onPrimaryContainer = Color(0xFFE7DEFF),
    secondary = Color(0xFFCBC2DB),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFF80D4D8),
    onTertiary = Color(0xFF003739),
    tertiaryContainer = Color(0xFF004F52),
    onTertiaryContainer = Color(0xFF9CF1F4),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    background = Color(0xFF141218),
    onBackground = Color(0xFFE6E0E9),
    surface = Color(0xFF141218),
    onSurface = Color(0xFFE6E0E9),
    surfaceVariant = Color(0xFF49454E),
    onSurfaceVariant = Color(0xFFCAC4CF),
    surfaceContainerLowest = Color(0xFF0F0D13),
    surfaceContainerLow = Color(0xFF1D1B20),
    surfaceContainer = Color(0xFF211F26),
    surfaceContainerHigh = Color(0xFF2B2930),
    surfaceContainerHighest = Color(0xFF36343B),
    outline = Color(0xFF948F99),
    outlineVariant = Color(0xFF49454E),
)

/**
 * Priority colours live outside the Material scheme because they must stay recognisable even when
 * Android dynamic colour reshapes everything else.
 */
object PriorityColors {

    @Composable
    @ReadOnlyComposable
    fun accent(priority: Priority): Color {
        val dark = MaterialTheme.colorScheme.isDark
        return when (priority) {
            Priority.HIGH -> if (dark) Color(0xFFFF8A80) else Color(0xFFD32F2F)
            Priority.MEDIUM -> if (dark) Color(0xFFFFB74D) else Color(0xFFE07B00)
            Priority.LOW -> if (dark) Color(0xFF81C784) else Color(0xFF2E7D32)
            Priority.NONE -> MaterialTheme.colorScheme.outline
        }
    }

    @Composable
    @ReadOnlyComposable
    fun container(priority: Priority): Color = accent(priority).copy(alpha = 0.14f)
}

/** Luminance test on the surface color; cheaper than threading the dark flag through the tree. */
internal val androidx.compose.material3.ColorScheme.isDark: Boolean
    get() = surface.luminance() < 0.5f

private fun Color.luminance(): Float = 0.299f * red + 0.587f * green + 0.114f * blue
