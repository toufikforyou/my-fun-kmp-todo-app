package com.toufikforyou.mytodoapp.domain.model

/**
 * Ordered from lowest to highest so `compareByDescending { it.priority }` sorts urgent work first.
 */
enum class Priority(val label: String) {
    NONE("None"),
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    ;

    companion object {
        /** Values a user picks between in the editor, most urgent first. */
        val selectable: List<Priority> = listOf(HIGH, MEDIUM, LOW, NONE)
    }
}
