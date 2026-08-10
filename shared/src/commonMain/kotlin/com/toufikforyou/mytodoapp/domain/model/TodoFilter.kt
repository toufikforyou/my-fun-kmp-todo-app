package com.toufikforyou.mytodoapp.domain.model

enum class TodoFilter(val label: String) {
    ALL("All"),
    ACTIVE("Active"),
    TODAY("Today"),
    UPCOMING("Upcoming"),
    OVERDUE("Overdue"),
    COMPLETED("Completed"),
}

enum class SortOrder(val label: String) {
    DUE_DATE("Due date"),
    PRIORITY("Priority"),
    CREATED("Recently added"),
    ALPHABETICAL("A to Z"),
}

enum class ThemeMode(val label: String) {
    SYSTEM("Follow system"),
    LIGHT("Light"),
    DARK("Dark"),
}
