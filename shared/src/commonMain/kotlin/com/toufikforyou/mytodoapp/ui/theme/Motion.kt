package com.toufikforyou.mytodoapp.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.IntOffset

/**
 * One place for the springs used across the app, so a checkbox, a reordering list item and a
 * sheet all settle with the same personality.
 */
object AppMotion {

    /** Snappy but not brittle — used for colour, alpha and size changes. */
    fun <T> quick() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )

    /** The bit of overshoot that makes completing a task feel rewarding. */
    fun <T> bouncy() = spring<T>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow,
    )

    /** List items sliding into place as the ordering changes. */
    fun placement() = spring<IntOffset>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )
}
