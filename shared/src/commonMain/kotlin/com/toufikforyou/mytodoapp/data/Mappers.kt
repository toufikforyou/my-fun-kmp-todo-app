package com.toufikforyou.mytodoapp.data

import com.toufikforyou.mytodoapp.data.local.CategoryEntity
import com.toufikforyou.mytodoapp.data.local.SETTINGS_ROW_ID
import com.toufikforyou.mytodoapp.data.local.SettingsEntity
import com.toufikforyou.mytodoapp.data.local.TodoEntity
import com.toufikforyou.mytodoapp.domain.model.AppSettings
import com.toufikforyou.mytodoapp.domain.model.Category
import com.toufikforyou.mytodoapp.domain.model.Priority
import com.toufikforyou.mytodoapp.domain.model.SortOrder
import com.toufikforyou.mytodoapp.domain.model.ThemeMode
import com.toufikforyou.mytodoapp.domain.model.Todo
import kotlin.time.Instant

internal fun TodoEntity.toDomain(): Todo = Todo(
    id = id,
    title = title,
    notes = notes,
    isCompleted = isCompleted,
    priority = priority.toPriority(),
    dueAt = dueAt?.toInstant(),
    reminderAt = reminderAt?.toInstant(),
    categoryId = categoryId,
    createdAt = createdAt.toInstant(),
    updatedAt = updatedAt.toInstant(),
    completedAt = completedAt?.toInstant(),
)

internal fun Todo.toEntity(): TodoEntity = TodoEntity(
    id = id,
    title = title,
    notes = notes,
    isCompleted = isCompleted,
    priority = priority.name,
    dueAt = dueAt?.toEpochMillis(),
    reminderAt = reminderAt?.toEpochMillis(),
    categoryId = categoryId,
    createdAt = createdAt.toEpochMillis(),
    updatedAt = updatedAt.toEpochMillis(),
    completedAt = completedAt?.toEpochMillis(),
)

internal fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    colorArgb = colorArgb,
    sortIndex = sortIndex,
)

internal fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    colorArgb = colorArgb,
    sortIndex = sortIndex,
)

internal fun SettingsEntity.toDomain(): AppSettings = AppSettings(
    themeMode = themeMode.toEnum(ThemeMode.SYSTEM),
    dynamicColor = dynamicColor,
    sortOrder = sortOrder.toEnum(SortOrder.DUE_DATE),
    showCompleted = showCompleted,
    defaultReminderLeadMinutes = defaultReminderLeadMinutes,
)

internal fun AppSettings.toEntity(): SettingsEntity = SettingsEntity(
    id = SETTINGS_ROW_ID,
    themeMode = themeMode.name,
    dynamicColor = dynamicColor,
    sortOrder = sortOrder.name,
    showCompleted = showCompleted,
    defaultReminderLeadMinutes = defaultReminderLeadMinutes,
)

/** Unknown names fall back to the default so an older row can never crash the app. */
private inline fun <reified T : Enum<T>> String.toEnum(fallback: T): T =
    enumValues<T>().firstOrNull { it.name == this } ?: fallback

private fun String.toPriority(): Priority = toEnum(Priority.NONE)

private fun Long.toInstant(): Instant = Instant.fromEpochMilliseconds(this)

private fun Instant.toEpochMillis(): Long = toEpochMilliseconds()
