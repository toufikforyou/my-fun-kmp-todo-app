package com.toufikforyou.mytodoapp.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Timestamps are stored as epoch milliseconds and priority as its enum name, so the schema stays
 * plain SQLite types and no Room type converters are needed.
 */
@Entity(
    tableName = "todos",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            // Deleting a list keeps its tasks; they just become unfiled.
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("categoryId"), Index("isCompleted"), Index("dueAt")],
)
data class TodoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val notes: String,
    val isCompleted: Boolean,
    val priority: String,
    val dueAt: Long?,
    val reminderAt: Long?,
    val categoryId: Long?,
    val createdAt: Long,
    val updatedAt: Long,
    val completedAt: Long?,
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val colorArgb: Long,
    val sortIndex: Int,
)

/** Single-row table; [id] is always [SETTINGS_ROW_ID]. */
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Long = SETTINGS_ROW_ID,
    val themeMode: String,
    val dynamicColor: Boolean,
    val sortOrder: String,
    val showCompleted: Boolean,
    val defaultReminderLeadMinutes: Int,
)

const val SETTINGS_ROW_ID: Long = 1L
