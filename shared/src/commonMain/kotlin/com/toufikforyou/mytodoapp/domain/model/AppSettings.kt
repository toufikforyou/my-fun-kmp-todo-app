package com.toufikforyou.mytodoapp.domain.model

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val sortOrder: SortOrder = SortOrder.DUE_DATE,
    val showCompleted: Boolean = true,
    /** Minutes before the due time that a reminder defaults to when the user turns one on. */
    val defaultReminderLeadMinutes: Int = 30,
)
