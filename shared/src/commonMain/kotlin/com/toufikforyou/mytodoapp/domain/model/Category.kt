package com.toufikforyou.mytodoapp.domain.model

/**
 * A user-defined list a task can belong to. [colorArgb] is a packed ARGB value stored as a Long
 * so the domain layer stays free of Compose types.
 */
data class Category(
    val id: Long = 0L,
    val name: String,
    val colorArgb: Long,
    val sortIndex: Int = 0,
) {
    companion object {
        /** Palette offered when creating a category; also used to seed the starter categories. */
        val palette: List<Long> = listOf(
            0xFF6750A4, // violet
            0xFF00696D, // teal
            0xFFB3261E, // red
            0xFF7D5260, // mauve
            0xFF3F6212, // olive
            0xFF9A4A00, // amber
            0xFF1D4ED8, // blue
            0xFF9D174D, // magenta
        )
    }
}
