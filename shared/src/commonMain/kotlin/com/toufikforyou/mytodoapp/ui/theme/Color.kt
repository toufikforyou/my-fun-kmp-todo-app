package com.toufikforyou.mytodoapp.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.toufikforyou.mytodoapp.domain.model.Priority
import com.toufikforyou.mytodoapp.domain.model.TodoFilter

/**
 * A bright, saturated palette. These are the fallback colors used whenever Android dynamic color
 * is unavailable or switched off, and on iOS always.
 */
internal val LightColors = lightColorScheme(
    primary = Color(0xFF6D4AFF),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE9E1FF),
    onPrimaryContainer = Color(0xFF200070),
    secondary = Color(0xFFFF4D8D),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFD9E4),
    onSecondaryContainer = Color(0xFF4A0021),
    tertiary = Color(0xFF00B39F),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFA8F5E9),
    onTertiaryContainer = Color(0xFF00201C),
    error = Color(0xFFE11D48),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFE0E5),
    onErrorContainer = Color(0xFF4A0512),
    background = Color(0xFFFCFAFF),
    onBackground = Color(0xFF1A1626),
    surface = Color(0xFFFCFAFF),
    onSurface = Color(0xFF1A1626),
    surfaceVariant = Color(0xFFE9E3F5),
    onSurfaceVariant = Color(0xFF554F62),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8F4FF),
    surfaceContainer = Color(0xFFF2EDFB),
    surfaceContainerHigh = Color(0xFFEDE6F8),
    surfaceContainerHighest = Color(0xFFE6DEF4),
    outline = Color(0xFF837D91),
    outlineVariant = Color(0xFFD5CEE3),
)

internal val DarkColors = darkColorScheme(
    primary = Color(0xFFC9B6FF),
    onPrimary = Color(0xFF350C9E),
    primaryContainer = Color(0xFF4E28C7),
    onPrimaryContainer = Color(0xFFE9E1FF),
    secondary = Color(0xFFFFB0C8),
    onSecondary = Color(0xFF64002B),
    secondaryContainer = Color(0xFF8E184D),
    onSecondaryContainer = Color(0xFFFFD9E4),
    tertiary = Color(0xFF5EE9D3),
    onTertiary = Color(0xFF003730),
    tertiaryContainer = Color(0xFF00564A),
    onTertiaryContainer = Color(0xFFA8F5E9),
    error = Color(0xFFFF8095),
    onError = Color(0xFF66001B),
    errorContainer = Color(0xFF8F0B2C),
    onErrorContainer = Color(0xFFFFE0E5),
    background = Color(0xFF121019),
    onBackground = Color(0xFFE9E2F3),
    surface = Color(0xFF121019),
    onSurface = Color(0xFFE9E2F3),
    surfaceVariant = Color(0xFF4A4458),
    onSurfaceVariant = Color(0xFFCEC6DC),
    surfaceContainerLowest = Color(0xFF0C0A12),
    surfaceContainerLow = Color(0xFF1B1825),
    surfaceContainer = Color(0xFF211D2C),
    surfaceContainerHigh = Color(0xFF2B2637),
    surfaceContainerHighest = Color(0xFF363042),
    outline = Color(0xFF9A92A6),
    outlineVariant = Color(0xFF4A4458),
)

/**
 * Vivid accents used to give each filter, priority and list its own identity. They sit outside the
 * Material scheme deliberately: they must stay recognisable even when Android dynamic color
 * reshapes everything else.
 */
object Accents {
    val Violet = Color(0xFF6D4AFF) to Color(0xFFC9B6FF)
    val Blue = Color(0xFF2563EB) to Color(0xFF8FB4FF)
    val Orange = Color(0xFFE8730C) to Color(0xFFFFBC7A)
    val Teal = Color(0xFF0D9488) to Color(0xFF5EE9D3)
    val Rose = Color(0xFFE11D48) to Color(0xFFFF8095)
    val Green = Color(0xFF16A34A) to Color(0xFF6EE7A0)

    /** Picks the light or dark member of a pair to match the current theme. */
    @Composable
    @ReadOnlyComposable
    fun pick(pair: Pair<Color, Color>): Color =
        if (MaterialTheme.colorScheme.isDark) pair.second else pair.first

    @Composable
    @ReadOnlyComposable
    fun of(filter: TodoFilter): Color = pick(
        when (filter) {
            TodoFilter.ALL -> Violet
            TodoFilter.ACTIVE -> Blue
            TodoFilter.TODAY -> Orange
            TodoFilter.UPCOMING -> Teal
            TodoFilter.OVERDUE -> Rose
            TodoFilter.COMPLETED -> Green
        },
    )

    @Composable
    @ReadOnlyComposable
    fun of(priority: Priority): Color = when (priority) {
        Priority.HIGH -> pick(Rose)
        Priority.MEDIUM -> pick(Orange)
        Priority.LOW -> pick(Green)
        Priority.NONE -> MaterialTheme.colorScheme.outline
    }

    /** Colors the section headers in the task list by what the section means. */
    @Composable
    @ReadOnlyComposable
    fun ofSection(title: String): Color = pick(
        when (title) {
            "Overdue" -> Rose
            "Today" -> Orange
            "Tomorrow" -> Blue
            "This week" -> Teal
            "Completed" -> Green
            else -> Violet
        },
    )
}

/**
 * Blends [color] into the card surface so a task or chip can carry its list's color without the
 * text on top losing contrast. Dark themes need a stronger blend to read at all.
 */
@Composable
@ReadOnlyComposable
fun softTint(color: Color, strength: Float = 1f): Color {
    val base = MaterialTheme.colorScheme.surfaceContainerLow
    val amount = if (MaterialTheme.colorScheme.isDark) 0.22f else 0.12f
    return lerp(base, color, amount * strength)
}

/**
 * The gradient used by the progress ring and the empty-state badge. Built from the fixed accents
 * rather than the Material scheme, because Android dynamic color collapses primary, secondary and
 * tertiary onto a single wallpaper hue — which would leave the gradient looking flat.
 */
@Composable
@ReadOnlyComposable
fun brandGradient(): Brush = Brush.sweepGradient(
    listOf(
        Accents.pick(Accents.Violet),
        Accents.pick(Accents.Blue),
        Accents.pick(Accents.Teal),
        Accents.pick(Accents.Green),
        Accents.pick(Accents.Orange),
        Accents.pick(Accents.Rose),
        Accents.pick(Accents.Violet),
    ),
)

/** Luminance test on the surface color; cheaper than threading the dark flag through the tree. */
internal val ColorScheme.isDark: Boolean
    get() = surface.luminance() < 0.5f

private fun Color.luminance(): Float = 0.299f * red + 0.587f * green + 0.114f * blue
