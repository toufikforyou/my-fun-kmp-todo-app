package com.toufikforyou.mytodoapp.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

const val DATABASE_NAME = "todos.db"

@Database(
    entities = [TodoEntity::class, CategoryEntity::class, SettingsEntity::class],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(TodoDatabaseConstructor::class)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
    abstract fun categoryDao(): CategoryDao
    abstract fun settingsDao(): SettingsDao
}

/** Room's KSP processor generates the `actual` for each platform. */
@Suppress("KotlinNoActualForExpect", "NO_ACTUAL_FOR_EXPECT")
expect object TodoDatabaseConstructor : RoomDatabaseConstructor<TodoDatabase> {
    override fun initialize(): TodoDatabase
}
