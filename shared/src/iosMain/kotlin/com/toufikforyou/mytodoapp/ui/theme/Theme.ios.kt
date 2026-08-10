package com.toufikforyou.mytodoapp.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/** iOS has no wallpaper-derived palette, so the app always uses its own colours. */
@Composable
actual fun platformDynamicColorScheme(dark: Boolean): ColorScheme? = null

actual fun supportsDynamicColor(): Boolean = false
